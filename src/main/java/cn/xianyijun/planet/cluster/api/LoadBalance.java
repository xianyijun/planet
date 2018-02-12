package cn.xianyijun.planet.cluster.api;

import cn.xianyijun.planet.cluster.support.loadbalance.RandomLoadBalance;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.Adaptive;
import cn.xianyijun.planet.common.extension.SPI;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;

import java.util.List;

/**
 * The interface Load balance.
 * @author xianyijun
 */
@SPI(RandomLoadBalance.NAME)
public interface LoadBalance {

    /**
     * Select invoker.
     *
     * @param <T>        the type parameter
     * @param invokers   the invokers
     * @param url        the url
     * @param invocation the invocation
     * @return the invoker
     * @throws RpcException the rpc exception
     */
    @Adaptive("loadbalance")
    <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException;

}
