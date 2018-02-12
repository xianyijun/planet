package cn.xianyijun.planet.cluster.api;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;

import java.util.List;

/**
 * The interface Router.
 * @author xianyijun
 */
public interface Router extends Comparable<Router> {

    /**
     * Gets url.
     *
     * @return the url
     */
    URL getUrl();

    /**
     * Route list.
     *
     * @param <T>        the type parameter
     * @param invokers   the invokers
     * @param url        the url
     * @param invocation the invocation
     * @return the list
     * @throws RpcException the rpc exception
     */
    <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException;
}
