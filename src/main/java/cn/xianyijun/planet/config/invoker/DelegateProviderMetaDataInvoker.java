package cn.xianyijun.planet.config.invoker;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.config.api.ServiceConfig;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Result;

/**
 *
 * @author xianyijun
 * @date 2018/1/27
 *
 * @param <T> the type parameter
 */
public class DelegateProviderMetaDataInvoker<T> implements Invoker {
    /**
     * The Invoker.
     */
    protected final Invoker<T> invoker;
    private ServiceConfig metadata;

    /**
     * Instantiates a new Delegate provider meta data invoker.
     *
     * @param invoker  the invoker
     * @param metadata the metadata
     */
    public DelegateProviderMetaDataInvoker(Invoker<T> invoker, ServiceConfig metadata) {
        this.invoker = invoker;
        this.metadata = metadata;
    }

    @Override
    public Class<T> getInterface() {
        return invoker.getInterface();
    }

    @Override
    public URL getUrl() {
        return invoker.getUrl();
    }

    @Override
    public boolean isAvailable() {
        return invoker.isAvailable();
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        return invoker.invoke(invocation);
    }

    @Override
    public void destroy() {
        invoker.destroy();
    }

    /**
     * Gets metadata.
     *
     * @return the metadata
     */
    public ServiceConfig getMetadata() {
        return metadata;
    }

}
