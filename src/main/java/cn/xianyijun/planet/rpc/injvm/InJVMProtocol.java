package cn.xianyijun.planet.rpc.injvm;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Protocol;
import cn.xianyijun.planet.rpc.api.protocol.AbstractProtocol;
import cn.xianyijun.planet.utils.UrlUtils;

import java.util.Map;
import java.util.Objects;

/**
 * The type In jvm protocol.
 *
 * @author xianyijun
 * @date 2018 /1/21
 */
public class InJVMProtocol extends AbstractProtocol implements Protocol {
    /**
     * The constant NAME.
     */
    public static final String NAME = Constants.LOCAL_PROTOCOL;

    /**
     * The constant DEFAULT_PORT.
     */
    private static final int DEFAULT_PORT = 0;

    private static InJVMProtocol INSTANCE;

    /**
     * Instantiates a new In jvm protocol.
     */
    public InJVMProtocol() {
        INSTANCE = this;
    }

    /**
     * Gets injvm protocol.
     *
     * @return the injvm protocol
     */
    public static InJVMProtocol getInjvmProtocol() {
        if (INSTANCE == null) {
            ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(InJVMProtocol.NAME);
        }
        return INSTANCE;
    }

    /**
     * Gets exporter.
     *
     * @param map the map
     * @param key the key
     * @return the exporter
     */
    public static Exporter<?> getExporter(Map<String, Exporter<?>> map, URL key) {
        Exporter<?> result = null;

        if (!Objects.requireNonNull(key.getServiceKey()).contains("*")) {
            result = map.get(key.getServiceKey());
        } else {
            if (map != null && !map.isEmpty()) {
                for (Exporter<?> exporter : map.values()) {
                    if (UrlUtils.isServiceKeyMatch(key, exporter.getInvoker().getUrl())) {
                        result = exporter;
                        break;
                    }
                }
            }
        }
        return result;
    }

    @Override
    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        return new InjvmExporter(invoker, invoker.getUrl().getServiceKey(), exporterMap);
    }

    @Override
    public <T> Invoker<T> refer(Class<T> serviceType, URL url) throws RpcException {
        return new InjvmInvoker<>(serviceType, url, url.getServiceKey(), exporterMap);
    }

    /**
     * Is in jvm refer boolean.
     *
     * @param url the url
     * @return the boolean
     */
    public boolean isInJVMRefer(URL url) {
        final boolean isJvmRefer;
        String scope = url.getParameter(Constants.SCOPE_KEY);
        isJvmRefer = !Constants.LOCAL_PROTOCOL.equals(url.getProtocol()) && (Constants.SCOPE_LOCAL.equals(scope) || (url.getParameter(Constants.INJVM_KEY, false)) || !Constants.SCOPE_REMOTE.equals(scope) && getExporter(exporterMap, url) != null);
        return isJvmRefer;
    }
}
