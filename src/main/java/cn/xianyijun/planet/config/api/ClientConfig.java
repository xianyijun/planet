package cn.xianyijun.planet.config.api;

import cn.xianyijun.planet.cluster.api.Cluster;
import cn.xianyijun.planet.cluster.support.FailoverCluster;
import cn.xianyijun.planet.cluster.support.directory.StaticDirectory;
import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.Version;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.remoting.api.Client;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Protocol;
import cn.xianyijun.planet.rpc.api.StaticContext;
import cn.xianyijun.planet.rpc.api.proxy.ProxyFactory;
import cn.xianyijun.planet.rpc.injvm.InJVMProtocol;
import cn.xianyijun.planet.utils.ArrayUtils;
import cn.xianyijun.planet.utils.ClusterUtils;
import cn.xianyijun.planet.utils.ConfigUtils;
import cn.xianyijun.planet.utils.NetUtils;
import cn.xianyijun.planet.utils.ReflectUtils;
import cn.xianyijun.planet.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import cn.xianyijun.planet.utils.CollectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.xianyijun.planet.utils.NetUtils.isInvalidLocalHost;


/**
 * The type Client config.
 *
 * @param <T> the type parameter
 * @author xianyijun
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper=false)
public class ClientConfig<T> extends AbstractClientConfig {

    private final List<URL> urlList = new ArrayList<>();

    private static final Protocol REF_PROTOCOL = ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension();

    private static final ProxyFactory PROXY_FACTORY = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension();

    private static final Cluster CLUSTER = ExtensionLoader.getExtensionLoader(Cluster.class).getAdaptiveExtension();

    private String interfaceName;

    private Class<?> interfaceClass;

    private String client;

    private String url;

    private List<MethodConfig> methodConfigs;

    private ConsumerConfig consumerConfig;

    private List<MethodConfig> methods;

    private String protocol;

    private transient volatile T ref;

    private transient volatile Invoker<?> invoker;

    private transient volatile boolean initialized;

    private transient volatile boolean destroyed;

    public ClientConfig(Client client) {
        appendAnnotation(Client.class, client);
    }
    /**
     * Sets interface.
     *
     * @param interfaceClass the interface class
     */
    public void setInterface(Class<?> interfaceClass) {
        if (interfaceClass != null && !interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
        this.interfaceClass = interfaceClass;
        setInterface(interfaceClass == null ? null : interfaceClass.getName());
    }

    /**
     * Sets interface.
     *
     * @param interfaceName the interface name
     */
    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
        if (id == null || id.length() == 0) {
            id = interfaceName;
        }
    }

    /**
     * Get t.
     *
     * @return the t
     */
    public synchronized T get() {
        log.info("[ClientConfig] start get ");
        if (destroyed) {
            throw new IllegalStateException("Already destroyed!");
        }
        if (ref == null) {
            log.info("[ClientConfig] start invoke init ");
            init();
        }
        return ref;
    }

    private void init() {
        log.info("[ClientConfig] start init ");
        if (initialized) {
            log.info("[ClientConfig] interface :{} had been init , interface  ", interfaceName);
            return;
        }
        initialized = true;
        if (StringUtils.isBlank(interfaceName)) {
            log.error("[ClientConfig] init interfaceName can not be blank");
            throw new IllegalStateException("rpc.reference interface not allow null!");
        }
        checkDefault();
        appendProperties(this);

        try {
            interfaceClass = Class.forName(interfaceName, true, Thread.currentThread().getContextClassLoader());
            log.info("[ClientConfig] interface :{} , interfaceClass is : {}",interfaceName, interfaceClass.getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        checkInterfaceAndMethods(interfaceClass, methods);

        if (consumerConfig != null) {
            if (application == null) {
                application = consumerConfig.getApplication();
            }

            if (registries == null) {
                registries = consumerConfig.getRegistries();
            }
        }

        if (application != null) {
            if (registries == null) {
                registries = application.getRegistries();
            }
        }

        checkApplication();

        Map<String, String> map = new HashMap<>();
        Map<Object, Object> attributes = new HashMap<>();

        map.put(Constants.SIDE_KEY, Constants.CONSUMER_SIDE);
        map.put(Constants.RPC_VERSION_KEY, Version.getVersion());
        map.put(Constants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));
        if (ConfigUtils.getPid() > 0) {
            map.put(Constants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
        }
        map.put(Constants.INTERFACE_KEY, interfaceName);
        appendParameters(map, application);
        appendParameters(map, this);
        String prefix = StringUtils.getServiceKey(map);
        if (!CollectionUtils.isEmpty(methods)) {
            for (MethodConfig method : methods) {
                appendParameters(map, method, method.getName());
                String retryKey = method.getName() + ".retry";
                if (map.containsKey(retryKey)) {
                    String retryValue = map.remove(retryKey);
                    if (Boolean.FALSE.toString().equals(retryValue)) {
                        map.put(method.getName() + ".retries", "0");
                    }
                }
                appendAttributes(attributes, method, prefix + "." + method.getName());
                checkAndConvertImplicitConfig(method, map, attributes);
            }
        }

        String hostToRegistry = ConfigUtils.getSystemProperty(Constants.RPC_IP_TO_REGISTRY);
        if (hostToRegistry == null || hostToRegistry.length() == 0) {
            hostToRegistry = NetUtils.getLocalHost();
        } else if (isInvalidLocalHost(hostToRegistry)) {
            throw new IllegalArgumentException("Specified invalid registry ip from property:" + Constants.RPC_IP_TO_REGISTRY + ", value:" + hostToRegistry);
        }
        map.put(Constants.REGISTER_IP_KEY, hostToRegistry);

        StaticContext.getSystemContext().putAll(attributes);
        ref = createProxy(map);
    }

    private T createProxy(Map<String, String> map) {
        log.info("[ClientConfig] start createProxy, map :{}", JSON.toJSONString(map));
        URL tmpUrl = new URL("temp", "localhost", 0, map);

        final boolean isJVMRefer;

        isJVMRefer = StringUtils.isBlank(url) && InJVMProtocol.getInjvmProtocol().isInJVMRefer(tmpUrl);
        log.info("[ClientConfig] createProxy , interfaceName :{}  isJVMRefer: {}",interfaceName,isJVMRefer);
        if (isJVMRefer) {
            log.info("[ClientConfig] createProxy , isJVMRefer, invoke refProtocol.refer, url : {}",url);
            URL url = new URL(Constants.LOCAL_PROTOCOL, NetUtils.LOCALHOST, 0, interfaceClass.getName()).addParameters(map);
            invoker = REF_PROTOCOL.refer(interfaceClass, url);
        } else {
            if (!StringUtils.isBlank(url)) {
                String[] urls = Constants.SEMICOLON_SPLIT_PATTERN.split(url);
                if (!ArrayUtils.isEmpty(urls)) {
                    for (String u : urls) {
                        URL url = URL.valueOf(u);
                        if (StringUtils.isBlank(url.getPath())) {
                            url = url.setPath(interfaceName);
                        }
                        if (Constants.REGISTRY_PROTOCOL.equalsIgnoreCase(url.getProtocol())) {
                            urlList.add(url.addParameterAndEncoded(Constants.REFER_KEY, StringUtils.toQueryString(map)));
                        } else {
                            urlList.add(ClusterUtils.mergeUrl(url, map));
                        }
                    }
                }
            } else {
                log.info("[ClientConfig] createProxy , interfaceName: {} , invoke loadRegistries ",interfaceName);
                List<URL> urls = loadRegistries(false);
                if (!CollectionUtils.isEmpty(urls)) {
                    for (URL u : urls) {
                        urlList.add(u.addParameterAndEncoded(Constants.REFER_KEY, StringUtils.toQueryString(map)));
                    }
                }

                if (CollectionUtils.isEmpty(urlList)) {
                    throw new IllegalStateException("No such any registry to reference " + interfaceName + " on the consumer " + NetUtils.getLocalHost() + " use rpc version " + Version.getVersion());
                }
            }
        }

        if (urlList.size() == 1) {
            log.info("[ClientConfig] start refProtocol.refer for urlList(size : 1), url :{} ",urlList.get(0));
            invoker = REF_PROTOCOL.refer(interfaceClass, urlList.get(0));
        } else {
            List<Invoker<?>> invokers = new ArrayList<>();
            URL registryURL = null;
            for (URL url : urlList) {
                log.info("[ClientConfig] start refProtocol.refer, interfaceName :{} , url :{}", interfaceName, url);
                invokers.add(REF_PROTOCOL.refer(interfaceClass, url));
                if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
                    registryURL = url;
                }
            }
            log.info("[ClientConfig] createProxy invoke CLUSTER.join, url :{} , clusterName: {} , invokers size: {}, invokes: {} ", url, CLUSTER.getClass().getName(), invokers.size(),invokers);
            if (registryURL != null) {
                URL u = registryURL.addParameter(Constants.CLUSTER_KEY, FailoverCluster.NAME);
                invoker = CLUSTER.join(new StaticDirectory(u, invokers));
            } else {
                invoker = CLUSTER.join(new StaticDirectory(invokers));
            }
        }

        Boolean check = true;
        if (consumerConfig != null) {
            check = consumerConfig.getCheck();
        }
        if (check == null) {
            check = true;
        }
        if (check && !invoker.isAvailable()) {
            throw new IllegalStateException("Failed to check the status of the service " + interfaceName + ". No provider available for the service " + (group == null ? "" : group + "/") + interfaceName + (version == null ? "" : ":" + version) + " from the url " + invoker.getUrl() + " to the consumer " + NetUtils.getLocalHost() + " use rpc version " + Version.getVersion());
        }
        log.info("[ClientConfig] invoke proxyFactory getProxy, proxyFactory :{} ,invoker: {}",PROXY_FACTORY.getClass().getName(), invoker);
        return (T) PROXY_FACTORY.getProxy(invoker);
    }



    private void checkDefault() {
        log.info("[ClientConfig] start checkDefault");
        if (consumerConfig == null) {
            consumerConfig = new ConsumerConfig();
        }
        appendProperties(consumerConfig);
    }

    /**
     * Check interface and methods.
     *
     * @param interfaceClass the interface class
     * @param methods        the methods
     */
    protected void checkInterfaceAndMethods(Class<?> interfaceClass, List<MethodConfig> methods) {
        log.info("[ClientConfig] start checkInterfaceAndMethods");
        if (interfaceClass == null) {
            throw new IllegalStateException("interface not allow null!");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
        if (methods != null && methods.size() > 0) {
            for (MethodConfig methodBean : methods) {
                String methodName = methodBean.getName();
                if (StringUtils.isBlank(methodName)) {
                    throw new IllegalStateException(String.format("[checkInterfaceAndMethods] interface name %s  method can not be null", interfaceClass.getName()));
                }
                boolean hasMethod = false;
                for (Method method : interfaceClass.getMethods()) {
                    if (method.getName().equals(methodName)) {
                        hasMethod = true;
                        break;
                    }
                }
                if (!hasMethod) {
                    throw new IllegalStateException("The interface " + interfaceClass.getName()
                            + " not found method " + methodName);
                }
            }
        }
    }

    private static void checkAndConvertImplicitConfig(MethodConfig method, Map<String, String> map, Map<Object, Object> attributes) {

        if (Boolean.FALSE.equals(method.getIsReturn()) && (method.getOnreturn() != null || method.getOnThrow() != null)) {
            throw new IllegalStateException("method config error : return attribute must be set true when onreturn or onthrow has been setted.");
        }
        String onReturnMethodKey = StaticContext.getKey(map, method.getName(), Constants.ON_RETURN_METHOD_KEY);
        Object onReturnMethod = attributes.get(onReturnMethodKey);
        if (onReturnMethod != null && onReturnMethod instanceof String) {
            attributes.put(onReturnMethodKey, getMethodByName(method.getOnreturn().getClass(), onReturnMethod.toString()));
        }

        String onThrowMethodKey = StaticContext.getKey(map, method.getName(), Constants.ON_THROW_METHOD_KEY);
        Object onThrowMethod = attributes.get(onThrowMethodKey);
        if (onThrowMethod != null && onThrowMethod instanceof String) {
            attributes.put(onThrowMethodKey, getMethodByName(method.getOnThrow().getClass(), onThrowMethod.toString()));
        }

        String onInvokeMethodKey = StaticContext.getKey(map, method.getName(), Constants.ON_INVOKE_METHOD_KEY);
        Object onInvokeMethod = attributes.get(onInvokeMethodKey);
        if (onInvokeMethod != null && onInvokeMethod instanceof String) {
            attributes.put(onInvokeMethodKey, getMethodByName(method.getOnInvoke().getClass(), onInvokeMethod.toString()));
        }
    }


    private static Method getMethodByName(Class<?> clazz, String methodName) {
        try {
            return ReflectUtils.findMethodByMethodName(clazz, methodName);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected void appendAnnotation(Class<?> annotationClass, Object annotation) {
        Method[] methods = annotationClass.getMethods();
        for (Method method : methods) {
            if (method.getDeclaringClass() != Object.class
                    && method.getReturnType() != void.class
                    && method.getParameterTypes().length == 0
                    && Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers())) {
                try {
                    String property = method.getName();
                    if ("interfaceClass".equals(property) || "interfaceName".equals(property)) {
                        property = "interface";
                    }
                    String setter = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
                    Object value = method.invoke(annotation);
                    if (value != null && !value.equals(method.getDefaultValue())) {
                        Class<?> parameterType = ReflectUtils.getBoxedClass(method.getReturnType());
                        if ("filter".equals(property) || "listener".equals(property)) {
                            parameterType = String.class;
                            value = StringUtils.join((String[]) value, ",");
                        } else if ("parameters".equals(property)) {
                            parameterType = Map.class;
                            value = CollectionUtils.toStringMap((String[]) value);
                        }
                        try {
                            Method setterMethod = getClass().getMethod(setter, parameterType);
                            setterMethod.invoke(this, value);
                        } catch (NoSuchMethodException e) {
                            // ignore
                        }
                    }
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}
