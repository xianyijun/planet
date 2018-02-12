package cn.xianyijun.planet.rpc.api;

import cn.xianyijun.planet.common.Node;
import cn.xianyijun.planet.exception.RpcException;

/**
 * Created by xianyijun on 2018/2/4.
 *
 * @param <T> the type parameter
 */
public interface Invoker<T> extends Node {

    /**
     * get service interface.
     *
     * @return service interface.
     */
    Class<T> getInterface();

    /**
     * invoke.
     *
     * @param invocation the invocation
     * @return result result
     * @throws RpcException the rpc exception
     */
    Result invoke(Invocation invocation) throws RpcException;

}