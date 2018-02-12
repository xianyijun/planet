package cn.xianyijun.planet.rpc.api;

import cn.xianyijun.planet.exception.RpcException;

/**
 * The interface Exporter listener.
 */
public interface ExporterListener {
    /**
     * Exported.
     *
     * @param exporter the exporter
     * @throws RpcException the rpc exception
     */
    void exported(Exporter<?> exporter) throws RpcException;

    /**
     * Un exported.
     *
     * @param exporter the exporter
     */
    void unExported(Exporter<?> exporter);
}
