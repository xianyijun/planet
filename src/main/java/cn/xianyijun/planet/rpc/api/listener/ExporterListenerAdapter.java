package cn.xianyijun.planet.rpc.api.listener;

import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.ExporterListener;

/**
 * The type Exporter listener adapter.
 */
public abstract class ExporterListenerAdapter implements ExporterListener {
    @Override
    public void exported(Exporter<?> exporter) throws RpcException {

    }

    @Override
    public void unExported(Exporter<?> exporter) {

    }
}
