package cn.xianyijun.planet.registry.api.integration;

import cn.xianyijun.planet.cluster.api.Cluster;
import cn.xianyijun.planet.cluster.api.Configurator;
import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.registry.api.NotifyListener;
import cn.xianyijun.planet.registry.api.Registry;
import cn.xianyijun.planet.registry.api.RegistryFactory;
import cn.xianyijun.planet.registry.api.RegistryService;
import cn.xianyijun.planet.registry.api.support.ProviderConsumerRegTable;
import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Protocol;
import cn.xianyijun.planet.rpc.api.protocol.InvokerWrapper;
import cn.xianyijun.planet.rpc.api.proxy.ProxyFactory;
import cn.xianyijun.planet.utils.ConfigUtils;
import cn.xianyijun.planet.utils.NamedThreadFactory;
import cn.xianyijun.planet.utils.StringUtils;
import cn.xianyijun.planet.utils.UrlUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The type Registry protocol.
 *
 * @author xianyijun
 */
@Data
@Slf4j
public class RegistryProtocol implements Protocol {

    private static RegistryProtocol INSTANCE;
    private final Map<URL, NotifyListener> overrideListeners = new ConcurrentHashMap<>();

    private final Map<String, ExporterChangeableWrapper<?>> bounds = new ConcurrentHashMap<>();
    private Cluster cluster;
    private Protocol protocol;
    private RegistryFactory registryFactory;
    private ProxyFactory proxyFactory;

    /**
     * Instantiates a new Registry protocol.
     */
    public RegistryProtocol() {
        INSTANCE = this;
    }

    private static RegistryProtocol getRegistryProtocol() {
        if (INSTANCE == null) {
            ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(Constants.REGISTRY_PROTOCOL); // load
        }
        return INSTANCE;
    }

    @Override
    public int getDefaultPort() {
        return 9090;
    }

    /**
     * Register.
     *
     * @param registryUrl         the registry url
     * @param registedProviderUrl the registed provider url
     */
    public void register(URL registryUrl, URL registedProviderUrl) {
        Registry registry = registryFactory.getRegistry(registryUrl);
        registry.register(registedProviderUrl);
    }


    @Override
    public <T> Exporter<T> export(Invoker<T> originInvoker) throws RpcException {
        ExporterChangeableWrapper<T> exporter = doLocalExport(originInvoker);

        URL registryUrl = getRegistryUrl(originInvoker);

        final Registry registry = getRegistry(originInvoker);
        final URL registeredProviderUrl = getRegisteredProviderUrl(originInvoker);

        boolean register = registeredProviderUrl.getParameter("register", true);

        ProviderConsumerRegTable.registerProvider(originInvoker, registryUrl, registeredProviderUrl);

        if (register) {
            register(registryUrl, registeredProviderUrl);
            Objects.requireNonNull(ProviderConsumerRegTable.getProviderWrapper(originInvoker)).setReg(true);
        }
        registry.register(registeredProviderUrl);

        final URL overrideSubscribeUrl = getSubscribedOverrideUrl(registeredProviderUrl);
        final OverrideListener overrideSubscribeListener = new OverrideListener(overrideSubscribeUrl, originInvoker);
        overrideListeners.put(overrideSubscribeUrl, overrideSubscribeListener);
        registry.subscribe(overrideSubscribeUrl, overrideSubscribeListener);

        return new DestroyableExporter<>(exporter, originInvoker, overrideSubscribeUrl, registeredProviderUrl);
    }

    private <T> URL getRegisteredProviderUrl(Invoker<T> originInvoker) {
        URL providerUrl = getProviderUrl(originInvoker);
        //注册中心看到的地址
        final URL registeredProviderUrl = providerUrl.removeParameters(getFilteredKeys(providerUrl))
                .removeParameter(Constants.MONITOR_KEY)
                .removeParameter(Constants.BIND_IP_KEY)
                .removeParameter(Constants.BIND_PORT_KEY);
        return registeredProviderUrl;
    }

    private String[] getFilteredKeys(URL url) {
        Map<String, String> params = url.getParameters();
        if (params != null && !params.isEmpty()) {
            List<String> filteredKeys = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry != null && entry.getKey() != null && entry.getKey().startsWith(Constants.HIDE_KEY_PREFIX)) {
                    filteredKeys.add(entry.getKey());
                }
            }
            return filteredKeys.toArray(new String[filteredKeys.size()]);
        } else {
            return new String[]{};
        }
    }

    private <T> ExporterChangeableWrapper<T> doLocalExport(Invoker<T> originInvoker) {
        String key = getCacheKey(originInvoker);
        ExporterChangeableWrapper<T> exporter = (ExporterChangeableWrapper<T>) bounds.get(key);

        if (exporter == null){
            synchronized (bounds){
                exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
                if (exporter == null){
                    final Invoker<?> invokerDelegate = new InvokerDelegate<>(originInvoker, getProviderUrl(originInvoker));
                    exporter = new ExporterChangeableWrapper<>((Exporter<T>) protocol.export(invokerDelegate), originInvoker);
                    bounds.put(key, exporter);
                }
            }
        }
        return exporter;
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        url = url.setProtocol(url.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_REGISTRY)).removeParameter(Constants.REGISTRY_KEY);
        Registry registry = registryFactory.getRegistry(url);
        if (RegistryService.class.equals(type)) {
            return proxyFactory.getInvoker((T) registry, type, url);
        }

        Map<String, String> qs = StringUtils.parseQueryString(url.getParameterAndDecoded(Constants.REFER_KEY));
        String group = qs.get(Constants.GROUP_KEY);
        if (!StringUtils.isBlank(group)) {
            if ((Constants.COMMA_SPLIT_PATTERN.split(group)).length > 1
                    || "*".equals(group)) {
                return doRefer(getMergeableCluster(), registry, type, url);
            }
        }
        return doRefer(cluster, registry, type, url);
    }

    private <T> Invoker<T> doRefer(Cluster cluster, Registry registry, Class<T> type, URL url) {
        RegistryDirectory<T> directory = new RegistryDirectory<>(type, url);
        directory.setRegistry(registry);
        directory.setProtocol(protocol);

        Map<String, String> parameters = new HashMap<>(directory.getUrl().getParameters());
        URL subscribeUrl = new URL(Constants.CONSUMER_PROTOCOL, parameters.remove(Constants.REGISTER_IP_KEY), 0, type.getName(), parameters);
        if (!Constants.ANY_VALUE.equals(url.getServiceInterface())
                && url.getParameter(Constants.REGISTER_KEY, true)) {
            registry.register(subscribeUrl.addParameters(Constants.CATEGORY_KEY, Constants.CONSUMERS_CATEGORY,
                    Constants.CHECK_KEY, String.valueOf(false)));
        }
        directory.subscribe(subscribeUrl.addParameter(Constants.CATEGORY_KEY,
                Constants.PROVIDERS_CATEGORY
                        + "," + Constants.CONFIGURATORS_CATEGORY
                        + "," + Constants.ROUTERS_CATEGORY));

        Invoker invoker = cluster.join(directory);
        ProviderConsumerRegTable.registerConsuemr(invoker, url, subscribeUrl, directory);
        return invoker;
    }

    @Override
    public void destroy() {
        List<Exporter<?>> exporters = new ArrayList<Exporter<?>>(bounds.values());
        for (Exporter<?> exporter : exporters) {
            exporter.unExport();
        }
        bounds.clear();
    }

    /**
     * Gets registry protocol.
     *
     * @param protocol the protocol
     * @return the registry protocol
     */
    public static RegistryProtocol getRegistryProtocol(Protocol protocol) {
        if (INSTANCE == null) {
            INSTANCE = new RegistryProtocol();
        }
        INSTANCE.setProtocol(protocol);
        return INSTANCE;
    }

    private Cluster getMergeableCluster() {
        return ExtensionLoader.getExtensionLoader(Cluster.class).getExtension("mergeable");
    }

    private Registry getRegistry(final Invoker<?> originInvoker) {
        URL registryUrl = getRegistryUrl(originInvoker);
        return registryFactory.getRegistry(registryUrl);
    }

    private URL getProviderUrl(final Invoker<?> originInvoker) {
        String export = originInvoker.getUrl().getParameterAndDecoded(Constants.EXPORT_KEY);
        if (export == null || export.length() == 0) {
            throw new IllegalArgumentException("The registry export url is null! registry: " + originInvoker.getUrl());
        }

        URL providerUrl = URL.valueOf(export);
        return providerUrl;
    }

    private String getCacheKey(final Invoker<?> originInvoker) {
        URL providerUrl = getProviderUrl(originInvoker);
        String key = providerUrl.removeParameters("dynamic", "enabled").toFullString();
        return key;
    }

    private URL getSubscribedOverrideUrl(URL registeredProviderUrl) {
        return registeredProviderUrl.setProtocol(Constants.PROVIDER_PROTOCOL)
                .addParameters(Constants.CATEGORY_KEY, Constants.CONFIGURATORS_CATEGORY,
                        Constants.CHECK_KEY, String.valueOf(false));
    }

    private <T> void doChangeLocalExport(final Invoker<T> originInvoker, URL newInvokerUrl) {
        String key = getCacheKey(originInvoker);
        final ExporterChangeableWrapper<T> exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
        if (exporter == null) {
            log.warn(new IllegalStateException("error state, exporter should not be null").getMessage());
        } else {
            final Invoker<T> invokerDelegete = new InvokerDelegate<T>(originInvoker, newInvokerUrl);
            exporter.setExporter(protocol.export(invokerDelegete));
        }
    }

    private URL getRegistryUrl(Invoker<?> originInvoker) {
        URL registryUrl = originInvoker.getUrl();
        if (Constants.REGISTRY_PROTOCOL.equals(registryUrl.getProtocol())) {
            String protocol = registryUrl.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_DIRECTORY);
            registryUrl = registryUrl.setProtocol(protocol).removeParameter(Constants.REGISTRY_KEY);
        }
        return registryUrl;
    }
    // ==================ExporterChangeableWrapper=======================

    private class ExporterChangeableWrapper<T> implements Exporter<T> {

        private final Invoker<T> originInvoker;
        private Exporter<T> exporter;

        /**
         * Instantiates a new Exporter changeable wrapper.
         *
         * @param exporter      the exporter
         * @param originInvoker the origin invoker
         */
        public ExporterChangeableWrapper(Exporter<T> exporter, Invoker<T> originInvoker) {
            this.exporter = exporter;
            this.originInvoker = originInvoker;
        }

        /**
         * Gets origin invoker.
         *
         * @return the origin invoker
         */
        public Invoker<T> getOriginInvoker() {
            return originInvoker;
        }

        @Override
        public Invoker<T> getInvoker() {
            return exporter.getInvoker();
        }

        /**
         * Sets exporter.
         *
         * @param exporter the exporter
         */
        public void setExporter(Exporter<T> exporter) {
            this.exporter = exporter;
        }

        @Override
        public void unExport() {
            String key = getCacheKey(this.originInvoker);
            bounds.remove(key);
            exporter.unExport();
        }
    }

    /**
     * The type Invoker delegate.
     *
     * @param <T> the type parameter
     */
    public static class InvokerDelegate<T> extends InvokerWrapper<T> {
        private final Invoker<T> invoker;

        /**
         * Instantiates a new Invoker delegate.
         *
         * @param invoker the invoker
         * @param url     the url
         */
        public InvokerDelegate(Invoker<T> invoker, URL url) {
            super(invoker, url);
            this.invoker = invoker;
        }

        /**
         * Gets invoker.
         *
         * @return the invoker
         */
        public Invoker<T> getInvoker() {
            if (invoker instanceof InvokerDelegate) {
                return ((InvokerDelegate<T>) invoker).getInvoker();
            } else {
                return invoker;
            }
        }
    }

    private class OverrideListener implements NotifyListener {

        private final URL subscribeUrl;
        private final Invoker originInvoker;

        /**
         * Instantiates a new Override listener.
         *
         * @param subscribeUrl    the subscribe url
         * @param originalInvoker the original invoker
         */
        public OverrideListener(URL subscribeUrl, Invoker originalInvoker) {
            this.subscribeUrl = subscribeUrl;
            this.originInvoker = originalInvoker;
        }

        @Override
        public synchronized void notify(List<URL> urls) {
            List<URL> matchedUrls = getMatchedUrls(urls, subscribeUrl);

            if (matchedUrls.isEmpty()) {
                return;
            }

            List<Configurator> configurators = RegistryDirectory.toConfigurators(matchedUrls);

            final Invoker<?> invoker;
            if (originInvoker instanceof InvokerDelegate) {
                invoker = ((InvokerDelegate<?>) originInvoker).getInvoker();
            } else {
                invoker = originInvoker;
            }
            URL originUrl = RegistryProtocol.this.getProviderUrl(invoker);

            String key = getCacheKey(originInvoker);
            ExporterChangeableWrapper<?> exporter = bounds.get(key);
            if (exporter == null) {
                log.warn(new IllegalStateException("error state, exporter should not be null").getMessage());
                return;
            }

            URL currentUrl = exporter.getInvoker().getUrl();
            URL newUrl = getConfigedInvokerUrl(configurators, originUrl);

            if (!currentUrl.equals(newUrl)) {
                RegistryProtocol.this.doChangeLocalExport(originInvoker, newUrl);
                log.info("exported provider url changed, origin url: " + originUrl + ", old export url: " + currentUrl + ", new export url: " + newUrl);
            }
        }

        private List<URL> getMatchedUrls(List<URL> configuratorUrls, URL currentSubscribe) {
            List<URL> result = new ArrayList<URL>();
            for (URL url : configuratorUrls) {
                URL overrideUrl = url;

                if (url.getParameter(Constants.CATEGORY_KEY) == null
                        && Constants.OVERRIDE_PROTOCOL.equals(url.getProtocol())) {
                    overrideUrl = url.addParameter(Constants.CATEGORY_KEY, Constants.CONFIGURATORS_CATEGORY);
                }

                if (UrlUtils.isMatch(currentSubscribe, overrideUrl)) {
                    result.add(url);
                }
            }
            return result;
        }

        private URL getConfigedInvokerUrl(List<Configurator> configurators, URL url) {
            for (Configurator configurator : configurators) {
                url = configurator.configure(url);
            }
            return url;
        }
    }

    static private class DestroyableExporter<T> implements Exporter<T> {

        /**
         * The constant executor.
         */
        public static final ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("Exporter-Unexport", true));

        private Exporter<T> exporter;
        private Invoker<T> originInvoker;
        private URL subscribeUrl;
        private URL registerUrl;

        /**
         * Instantiates a new Destroyable exporter.
         *
         * @param exporter      the exporter
         * @param originInvoker the origin invoker
         * @param subscribeUrl  the subscribe url
         * @param registerUrl   the register url
         */
        public DestroyableExporter(Exporter<T> exporter, Invoker<T> originInvoker, URL subscribeUrl, URL registerUrl) {
            this.exporter = exporter;
            this.originInvoker = originInvoker;
            this.subscribeUrl = subscribeUrl;
            this.registerUrl = registerUrl;
        }
        @Override
        public Invoker<T> getInvoker() {
            return exporter.getInvoker();
        }

        @Override
        public void unExport() {
            Registry registry = RegistryProtocol.INSTANCE.getRegistry(originInvoker);
            try {
                registry.unRegister(registerUrl);
            } catch (Throwable t) {
                log.warn(t.getMessage(), t);
            }
            try {
                NotifyListener listener = RegistryProtocol.INSTANCE.overrideListeners.remove(subscribeUrl);
                registry.unSubscribe(subscribeUrl, listener);
            } catch (Throwable t) {
                log.warn(t.getMessage(), t);
            }

            executor.submit(() -> {
                try {
                    int timeout = ConfigUtils.getServerShutdownTimeout();
                    if (timeout > 0) {
                        log.info("Waiting " + timeout + "ms for registry to notify all consumers before unexport. Usually, this is called when you use rpc API");
                        Thread.sleep(timeout);
                    }
                    exporter.unExport();
                } catch (Throwable t) {
                    log.warn(t.getMessage(), t);
                }
            });
        }
    }
}
