package cn.xianyijun.planet.rpc.api;

import cn.xianyijun.planet.exception.RpcException;

/**
 * The interface Invoker listener.
 */
public interface InvokerListener {

    /**
     * Referred.
     *
     * @param invoker the invoker
     * @throws RpcException the rpc exception
     */
    void referred(Invoker<?> invoker) throws RpcException;

    /**
     * Destroyed.
     *
     * @param invoker the invoker
     */
    void destroyed(Invoker<?> invoker);

}
