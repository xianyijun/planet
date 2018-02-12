package cn.xianyijun.planet.rpc.api.protocol;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Protocol;
import cn.xianyijun.planet.utils.ProtocolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The type Abstract protocol.
 */
public abstract class AbstractProtocol implements Protocol {
    /**
     * The Logger.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The Exporter map.
     */
    protected final Map<String, Exporter<?>> exporterMap = new ConcurrentHashMap<>();

    /**
     * The Invokers.
     */
    protected final Set<Invoker<?>> invokers = new CopyOnWriteArraySet<>();

    /**
     * Service key string.
     *
     * @param url the url
     * @return the string
     */
    protected static String serviceKey(URL url) {
        return ProtocolUtils.serviceKey(url);
    }

    /**
     * Service key string.
     *
     * @param port           the port
     * @param serviceName    the service name
     * @param serviceVersion the service version
     * @param serviceGroup   the service group
     * @return the string
     */
    protected static String serviceKey(int port, String serviceName, String serviceVersion, String serviceGroup) {
        return ProtocolUtils.serviceKey(port, serviceName, serviceVersion, serviceGroup);
    }

    @Override
    public void destroy() {
        for (Invoker<?> invoker : invokers) {
            if (invoker != null) {
                invokers.remove(invoker);
                try {
                    invoker.destroy();
                } catch (Throwable t) {
                    logger.warn(t.getMessage(), t);
                }
            }
        }
        for (String key : new ArrayList<>(exporterMap.keySet())) {
            Exporter<?> exporter = exporterMap.remove(key);
            if (exporter != null) {
                try {
                    if (logger.isInfoEnabled()) {
                        logger.info("Unexport service: " + exporter.getInvoker().getUrl());
                    }
                    exporter.unExport();
                } catch (Throwable t) {
                    logger.warn(t.getMessage(), t);
                }
            }
        }
    }
}
