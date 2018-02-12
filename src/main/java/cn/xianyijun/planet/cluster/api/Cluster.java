package cn.xianyijun.planet.cluster.api;

import cn.xianyijun.planet.cluster.support.FailoverCluster;
import cn.xianyijun.planet.common.extension.Adaptive;
import cn.xianyijun.planet.common.extension.SPI;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invoker;

/**
 * The interface Cluster.
 * @author xianyijun
 */
@SPI(FailoverCluster.NAME)
public interface Cluster {

    /**
     * Join invoker.
     *
     * @param <T>       the type parameter
     * @param directory the directory
     * @return the invoker
     * @throws RpcException the rpc exception
     */
    @Adaptive
    <T> Invoker<T> join(Directory<T> directory) throws RpcException;
}
