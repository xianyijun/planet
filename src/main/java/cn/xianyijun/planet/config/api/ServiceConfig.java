package cn.xianyijun.planet.config.api;

import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.xianyijun.planet.cluster.api.ConfiguratorFactory;
import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.Version;
import cn.xianyijun.planet.common.bytecode.Wrapper;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.config.annotation.RpcService;
import cn.xianyijun.planet.config.invoker.DelegateProviderMetaDataInvoker;
import cn.xianyijun.planet.rpc.ServiceClassHolder;
import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Protocol;
import cn.xianyijun.planet.rpc.api.proxy.ProxyFactory;
import cn.xianyijun.planet.rpc.rpc.RpcProtocol;
import cn.xianyijun.planet.utils.ClassHelper;
import cn.xianyijun.planet.utils.ConfigUtils;
import cn.xianyijun.planet.utils.NamedThreadFactory;
import cn.xianyijun.planet.utils.NetUtils;
import cn.xianyijun.planet.utils.StringUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


/**
 * The type Service config.
 *
 * @param <T> the type parameter
 * @author xianyijun
 */
@NoArgsConstructor
@Getter
@Setter
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class ServiceConfig<T> extends AbstractServiceConfig {

    private static final Protocol PROTOCOL = ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension();

    private static final ProxyFactory PROXY_FACTORY = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension();

    private static final Map<String, Integer> RANDOM_PORT_MAP = new HashMap<>();

    private static final ScheduledExecutorService delayExportExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("RpcServiceDelayExporter", true));

    private final List<URL> urls = new ArrayList<>();

    private final List<Exporter<?>> exporters = new ArrayList<>();

    private String interfaceName;

    private Class<?> interfaceClass;

    private T ref;

    private String path;

    private List<MethodConfig> methods;

    private ProviderConfig provider;

    private transient volatile boolean exported;

    private transient volatile boolean unExported;

    private volatile String generic;

    public ServiceConfig(RpcService service) {
        appendAnnotation(RpcService.class, service);
    }

    /**
     * Export.
     */
    public synchronized void export() {
        if (provider != null) {
            if (export == null) {
                export = provider.getExport();
            }
            if (delay == null) {
                delay = provider.getDelay();
            }
        }
        if (export != null && !export) {
            return;
        }

        if (delay != null && delay > 0) {
            delayExportExecutor.schedule(this::doExport, delay, TimeUnit.MILLISECONDS);
        } else {
            doExport();
        }
    }

    private void doExport() {
        if (unExported) {
            throw new IllegalArgumentException(String.format("the service %s is already unExported", interfaceClass));
        }

        if (exported) {
            return;
        }
        exported = true;

        if (StringUtils.isBlank(interfaceName)) {
            throw new IllegalArgumentException("the interface name can not be null");
        }
        checkDefault();

        if (provider != null) {
            if (application == null) {
                application = provider.getApplication();
            }
            if (registries == null) {
                registries = provider.getRegistries();
            }

            if (protocols == null) {
                protocols = provider.getProtocols();
            }
        }

        if (application != null) {
            if (registries == null) {
                registries = application.getRegistries();
            }
        }
        try {
            interfaceClass = Class.forName(interfaceName, true, Thread.currentThread()
                    .getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        checkInterfaceAndMethods(interfaceClass, methods);
        checkRef();
        if (local != null) {
            if (Boolean.valueOf(local)) {
                local = interfaceName + "Local";
            }
            Class<?> localClass;
            try {
                localClass = ClassHelper.forNameWithThreadContextClassLoader(local);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            if (!interfaceClass.isAssignableFrom(localClass)) {
                throw new IllegalStateException("The local implementation class " + localClass.getName() + " not implement interface " + interfaceName);
            }
        }
        if (stub != null) {
            if (Boolean.valueOf(stub)) {
                stub = interfaceName + "Stub";
            }
            Class<?> stubClass;
            try {
                stubClass = ClassHelper.forNameWithThreadContextClassLoader(stub);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            if (!interfaceClass.isAssignableFrom(stubClass)) {
                throw new IllegalStateException("The stub implementation class " + stubClass.getName() + " not implement interface " + interfaceName);
            }
        }
        checkApplication();
        checkRegistry();
        checkProtocol();
        appendProperties(this);
        checkStub(interfaceClass);
        if (StringUtils.isBlank(path)) {
            path = interfaceName;
        }
        doExportUrls();
    }

    private void doExportUrls() {
        List<URL> registryURLs = loadRegistries(true);
        for (ProtocolConfig protocolConfig : protocols) {
            doExportUrlsForProtocol(protocolConfig, registryURLs);
        }
    }

    private void doExportUrlsForProtocol(ProtocolConfig protocolConfig, List<URL> registryURLs) {
        log.info(String.format("[doExportUrlsForProtocol] start export, protocolConfig : %s , registryUrl: %s", protocolConfig, registryURLs));
        String name = protocolConfig.getName();
        if (StringUtils.isBlank(name)) {
            name = "rpc";
        }

        Map<String, String> map = new HashMap<>();

        map.put(Constants.SIDE_KEY, Constants.PROVIDER_SIDE);
        map.put(Constants.RPC_VERSION_KEY, Version.getVersion());
        map.put(Constants.TIMESTAMP_KEY, String.valueOf(System.currentTimeMillis()));

        if (ConfigUtils.getPid() > 0) {
            map.put(Constants.PID_KEY, String.valueOf(ConfigUtils.getPid()));
        }

        appendParameters(map, application);
        appendParameters(map, provider, Constants.DEFAULT_KEY);
        appendParameters(map, protocolConfig);
        appendParameters(map, this);

        if (!CollectionUtils.isEmpty(methods)) {
            methods.stream().filter(Objects::nonNull).forEach(method -> {
                appendParameters(map, method, method.getName());
                String retryKey = method.getName() + ".retry";
                log.info(String.format("[doExportUrlsForProtocol] method retry : %s", retryKey));
                if (map.containsKey(retryKey)) {
                    String retryValue = map.remove(retryKey);
                    if (!Boolean.valueOf(retryValue)) {
                        map.put(method.getName() + ".retries", "0");
                    }
                }
            });
        }

        String[] methods = Wrapper.getWrapper(interfaceClass).getMethodNames();
        if (methods.length == 0) {
            map.put("methods", Constants.ANY_VALUE);
        } else {
            map.put("methods", StringUtils.join(new HashSet<>(Arrays.asList(methods)), ","));
        }

        if (!ConfigUtils.isEmpty(token)) {
            if (ConfigUtils.isDefault(token)) {
                map.put("token", UUID.randomUUID().toString());
            } else {
                map.put("token", token);
            }
        }

        if (Constants.INJVM_KEY.equals(protocolConfig.getName())) {
            protocolConfig.setRegister(false);
            map.put("notify", "false");
        }

        String contextPath = protocolConfig.getContextPath();
        if (StringUtils.isBlank(contextPath) && provider != null) {
            contextPath = provider.getContextPath();
        }
        String host = this.findConfiguredHosts(protocolConfig, registryURLs, map);
        Integer port = this.findConfiguredPorts(protocolConfig, name, map);

        URL url = new URL(name, host, port, (contextPath == null || contextPath.length() == 0 ? "" : contextPath + "/") + path, map);
        log.info(String.format("[doExportUrlsForProtocol] url : %s", url.toFullString()));

        if (ExtensionLoader.getExtensionLoader(ConfiguratorFactory.class)
                .hasExtension(url.getProtocol())) {
            url = ExtensionLoader.getExtensionLoader(ConfiguratorFactory.class)
                    .getExtension(url.getProtocol()).getConfigurator(url).configure(url);
        }

        String scope = url.getParameter(Constants.SCOPE_KEY);

        if (!Constants.SCOPE_NONE.equalsIgnoreCase(scope)) {

            //配置不是remote的情况下做本地暴露 (配置为remote，则表示只暴露远程服务)
            if (!Constants.SCOPE_REMOTE.equalsIgnoreCase(scope)) {
                log.info(String.format("[doExportUrlsForProtocol] start exportLocal, url %s", url));
                exportLocal(url);
            }
            //如果配置不是local则暴露为远程服务.(配置为local，则表示只暴露本地服务)
            if (!Constants.SCOPE_LOCAL.equalsIgnoreCase(scope)) {

                if (registryURLs != null && registryURLs.size() > 0 && url.getParameter(Constants.REGISTER_KEY, true)) {

                    for (URL registryURL : registryURLs) {

                        url = url.addParameterIfAbsent(Constants.DYNAMIC_KEY, registryURL.getParameter("dynamic"));

                        Invoker<?> invoker = PROXY_FACTORY.getInvoker(ref, (Class) interfaceClass, registryURL.addParameterAndEncoded(Constants.EXPORT_KEY, url.toFullString()));
                        DelegateProviderMetaDataInvoker wrapperInvoker = new DelegateProviderMetaDataInvoker<>(invoker, this);

                        Exporter<?> exporter = PROTOCOL.export(wrapperInvoker);
                        exporters.add(exporter);
                    }
                } else {
                    Invoker<?> invoker = PROXY_FACTORY.getInvoker(ref, (Class) interfaceClass, url);

                    Exporter<?> exporter = PROTOCOL.export(invoker);
                    exporters.add(exporter);
                }
            }
        }

        this.urls.add(url);
    }

    private Integer findConfiguredPorts(ProtocolConfig protocolConfig, String name, Map<String, String> map) {
        Integer portToBind = null;

        // 解析环境变量配置的bind port
        String port = ConfigUtils.getSystemProperty(Constants.RPC_PORT_TO_BIND);
        portToBind = parsePort(port);

        if (portToBind == null) {
            portToBind = protocolConfig.getPort();

            if (provider != null && (portToBind == null || portToBind == 0)) {
                portToBind = provider.getPort();
            }
            int defaultPort = new RpcProtocol().getDefaultPort();

            if (portToBind == null || portToBind == 0) {
                portToBind = defaultPort;
            }
            if (portToBind == null || portToBind == 0) {
                portToBind = getRandomPort(name);
                if (portToBind == null || portToBind < 0) {
                    portToBind = NetUtils.getAvailablePort(defaultPort);
                    putRandomPort(name, portToBind);
                }
            }
        }
        map.put(Constants.BIND_PORT_KEY, String.valueOf(portToBind));

        String portToRegistryStr = ConfigUtils.getSystemProperty(Constants.RPC_PORT_TO_REGISTRY);

        Integer portToRegistry = parsePort(portToRegistryStr);
        if (portToRegistry == null) {
            portToRegistry = portToBind;
        }
        return portToRegistry;
    }

    private String findConfiguredHosts(ProtocolConfig protocolConfig, List<URL> registryURLs, Map<String, String> map) {
        boolean anyHost = false;

        String host = ConfigUtils.getSystemProperty(Constants.RPC_IP_TO_BIND);

        if (!StringUtils.isBlank(host) && NetUtils.isInvalidLocalHost(host)) {
            throw new IllegalArgumentException(String.format("the System property : %s values is invalid, the values: %s", Constants.RPC_IP_TO_BIND, host));
        }

        if (StringUtils.isBlank(host)) {
            host = protocolConfig.getHost();

            if (provider != null && StringUtils.isBlank(host)) {
                host = provider.getHost();
            }

            if (NetUtils.isInvalidLocalHost(host)) {
                anyHost = true;
                try {
                    host = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    log.warn(e.getMessage(), e);
                }
                if (NetUtils.isInvalidLocalHost(host)) {
                    if (registryURLs != null && !registryURLs.isEmpty()) {
                        for (URL url : registryURLs) {
                            try {
                                Socket socket = new Socket();
                                try {
                                    SocketAddress addr = new InetSocketAddress(url.getHost(), url.getPort());
                                    socket.connect(addr, 1000);
                                    host = socket.getLocalAddress().getHostAddress();
                                    break;
                                } finally {
                                    try {
                                        socket.close();
                                    } catch (Throwable e) {

                                    }
                                }
                            } catch (Exception e) {
                                log.warn(e.getMessage(), e);
                            }
                        }
                    }
                    if (NetUtils.isInvalidLocalHost(host)) {
                        host = NetUtils.getLocalHost();
                    }
                }
            }
        }
        map.put(Constants.BIND_IP_KEY, host);

        String hostToRegistry = ConfigUtils.getSystemProperty(Constants.RPC_IP_TO_REGISTRY);

        if (!StringUtils.isBlank(hostToRegistry) && NetUtils.isInvalidLocalHost(hostToRegistry)) {
            throw new IllegalArgumentException(String.format("the System property : %s values is invalid, the values: %s", Constants.RPC_IP_TO_REGISTRY, hostToRegistry));
        }

        if (StringUtils.isBlank(hostToRegistry)) {
            hostToRegistry = host;
        }
        map.put(Constants.ANY_HOST_KEY, String.valueOf(anyHost));
        return hostToRegistry;
    }

    private void exportLocal(URL url) {
        if (!Constants.LOCAL_PROTOCOL.equalsIgnoreCase(url.getProtocol())) {
            URL local = URL.valueOf(url.toFullString())
                    .setProtocol(Constants.LOCAL_PROTOCOL)
                    .setHost(NetUtils.LOCALHOST)
                    .setPort(0);
            ServiceClassHolder.getInstance().pushServiceClass(getServiceClass(ref));
            Exporter<?> exporter = PROTOCOL.export(
                    PROXY_FACTORY.getInvoker(ref, (Class) interfaceClass, local));
            exporters.add(exporter);
        }
    }


    /**
     * Check interface and methods.
     *
     * @param interfaceClass the interface class
     * @param methods        the methods
     */
    protected void checkInterfaceAndMethods(Class<?> interfaceClass, List<MethodConfig> methods) {
        // 接口不能为空
        if (interfaceClass == null) {
            throw new IllegalStateException("interface not allow null!");
        }
        // 检查接口类型必需为接口
        if (!interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        }
        // 检查方法是否在接口中存在
        if (CollectionUtils.isEmpty(methods)) {
            return;
        }
        for (MethodConfig methodBean : methods) {
            String methodName = methodBean.getName();
            if (StringUtils.isBlank(methodName)) {
                throw new IllegalStateException(String.format("rpc method name attribute is required! Please check: interface = %s method name ", interfaceClass.getName()));
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

    private void checkRef() {
        // 检查引用不为空，并且引用必需实现接口
        if (ref == null) {
            throw new IllegalStateException("ref not allow null!");
        }
        if (!interfaceClass.isInstance(ref)) {
            throw new IllegalStateException("The class " + ref.getClass().getName() + " unimplemented interface "
                    + interfaceClass + "!");
        }
    }

    private void checkDefault() {
        if (provider == null) {
            provider = new ProviderConfig();
        }
        appendProperties(provider);
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
        setInterface(interfaceClass == null ? "" : interfaceClass.getName());
    }

    /**
     * Sets interface.
     *
     * @param interfaceName the interface name
     */
    public void setInterface(String interfaceName) {
        this.interfaceName = interfaceName;
        if (StringUtils.isBlank(id)) {
            id = interfaceName;
        }
    }


    public String getInterface() {
        return interfaceName;
    }

    protected Class getServiceClass(T ref) {
        return ref.getClass();
    }

    private void checkProtocol() {
        if ((CollectionUtils.isEmpty(protocols)) && provider != null) {
            setProtocols(provider.getProtocols());
        }
        for (ProtocolConfig protocolConfig : protocols) {
            if (StringUtils.isEmpty(protocolConfig.getName())) {
                protocolConfig.setName("rpc");
            }
            appendProperties(protocolConfig);
        }
    }

    private Integer getRandomPort(String protocol) {
        protocol = protocol.trim();
        if (RANDOM_PORT_MAP.containsKey(protocol)) {
            return RANDOM_PORT_MAP.get(protocol);
        }
        return Integer.MAX_VALUE;
    }

    private static void putRandomPort(String protocol, Integer port) {
        protocol = protocol.toLowerCase();
        if (!RANDOM_PORT_MAP.containsKey(protocol)) {
            RANDOM_PORT_MAP.put(protocol, port);
        }
    }

    private Integer parsePort(String configPort) {
        Integer port = null;
        if (configPort != null && configPort.length() > 0) {
            try {
                Integer intPort = Integer.parseInt(configPort);
                if (NetUtils.isInvalidPort(intPort)) {
                    throw new IllegalArgumentException("Specified invalid port from env value:" + configPort);
                }
                port = intPort;
            } catch (Exception e) {
                throw new IllegalArgumentException("Specified invalid port from env value:" + configPort);
            }
        }
        return port;
    }
}
