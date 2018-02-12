package cn.xianyijun.planet.rpc.injvm;

import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.protocol.AbstractExporter;

import java.util.Map;

/**
 * The type Injvm exporter.
 *
 * @param <T> the type parameter
 * @author xianyijun
 * @date 2018 /1/21
 */
public class InjvmExporter<T> extends AbstractExporter {

    private final String key;

    private final Map<String, Exporter<?>> exporterMap;

    /**
     * Instantiates a new Injvm exporter.
     *
     * @param invoker     the invoker
     * @param key         the key
     * @param exporterMap the exporter map
     */
    public InjvmExporter(Invoker<T> invoker, String key, Map<String, Exporter<?>> exporterMap) {
        super(invoker);
        this.key = key;
        this.exporterMap = exporterMap;
        exporterMap.put(key, this);
    }

    @Override
    public void unExport() {
        super.unExport();
        exporterMap.remove(key);
    }
}

