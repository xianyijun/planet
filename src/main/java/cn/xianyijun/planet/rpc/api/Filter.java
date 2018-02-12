package cn.xianyijun.planet.rpc.api;

import cn.xianyijun.planet.common.extension.SPI;
import cn.xianyijun.planet.exception.RpcException;

/**
 * The interface Filter.
 * @author xianyijun
 */
@SPI
public interface Filter {

    /**
     * Invoke result.
     *
     * @param invoker    the invoker
     * @param invocation the invocation
     * @return the result
     * @throws RpcException the rpc exception
     */
    Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException;

}
