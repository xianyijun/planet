package cn.xianyijun.planet.cluster.support.directory;


import cn.xianyijun.planet.cluster.api.Router;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;

import java.util.List;

/**
 * The type Static directory.
 *
 * @param <T> the type parameter
 * @author xianyijun
 */
public class StaticDirectory<T> extends AbstractDirectory {
    private final List<Invoker<T>> invokers;

    /**
     * Instantiates a new Static directory.
     *
     * @param invokers the invokers
     */
    public StaticDirectory(List<Invoker<T>> invokers) {
        this(null, invokers, null);
    }

    /**
     * Instantiates a new Static directory.
     *
     * @param invokers the invokers
     * @param routers  the routers
     */
    public StaticDirectory(List<Invoker<T>> invokers, List<Router> routers) {
        this(null, invokers, routers);
    }

    /**
     * Instantiates a new Static directory.
     *
     * @param url      the url
     * @param invokers the invokers
     */
    public StaticDirectory(URL url, List<Invoker<T>> invokers) {
        this(url, invokers, null);
    }

    /**
     * Instantiates a new Static directory.
     *
     * @param url      the url
     * @param invokers the invokers
     * @param routers  the routers
     */
    public StaticDirectory(URL url, List<Invoker<T>> invokers, List<Router> routers) {
        super(url == null && invokers != null && invokers.size() > 0 ? invokers.get(0).getUrl() : url, routers);
        if (invokers == null || invokers.size() == 0){
            throw new IllegalArgumentException("invokers == null");
        }
        this.invokers = invokers;
    }

    @Override
    public Class<T> getInterface() {
        return invokers.get(0).getInterface();
    }

    @Override
    public boolean isAvailable() {
        if (isDestroyed()) {
            return false;
        }
        for (Invoker<T> invoker : invokers) {
            if (invoker.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        if (isDestroyed()) {
            return;
        }
        super.destroy();
        for (Invoker<T> invoker : invokers) {
            invoker.destroy();
        }
        invokers.clear();
    }

    @Override
    protected List<Invoker<T>> doList(Invocation invocation) throws RpcException {
        return invokers;
    }

}
