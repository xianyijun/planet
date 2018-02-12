package cn.xianyijun.planet.rpc.api.protocol;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Result;

/**
 * The type Invoker wrapper.
 *
 * @param <T> the type parameter
 */
public class InvokerWrapper<T> implements Invoker<T> {
    private final Invoker<T> invoker;

    private final URL url;

    /**
     * Instantiates a new Invoker wrapper.
     *
     * @param invoker the invoker
     * @param url     the url
     */
    public InvokerWrapper(Invoker<T> invoker, URL url) {
        this.invoker = invoker;
        this.url = url;
    }
    @Override
    public Class<T> getInterface() {
        return invoker.getInterface();
    }

    @Override
    public URL getUrl() {
        return url;
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

}
