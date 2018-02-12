package cn.xianyijun.planet.cluster.api;

import cn.xianyijun.planet.common.Node;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;

import java.util.List;

/**
 * The interface Directory.
 *
 * @param <T> the type parameter
 */
public interface Directory<T> extends Node {
    /**
     * Gets interface.
     *
     * @return the interface
     */
    Class<T> getInterface();

    /**
     * List list.
     *
     * @param invocation the invocation
     * @return the list
     * @throws RpcException the rpc exception
     */
    List<Invoker<T>> list(Invocation invocation) throws RpcException;
}
