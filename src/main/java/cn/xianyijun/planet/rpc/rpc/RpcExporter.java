package cn.xianyijun.planet.rpc.rpc;

import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.protocol.AbstractExporter;

import java.util.Map;

/**
 * The type Rpc exporter.
 *
 * @param <T> the type parameter
 * @author xianyijun
 */
public class RpcExporter<T> extends AbstractExporter<T> {
    private final String key;

    private final Map<String, Exporter<?>> exporterMap;

    /**
     * Instantiates a new Rpc exporter.
     *
     * @param invoker     the invoker
     * @param key         the key
     * @param exporterMap the exporter map
     */
    public RpcExporter(Invoker<T> invoker, String key, Map<String, Exporter<?>> exporterMap) {
        super(invoker);
        this.key = key;
        this.exporterMap = exporterMap;
    }

    @Override
    public void unExport() {
        super.unExport();
        exporterMap.remove(key);
    }
}
