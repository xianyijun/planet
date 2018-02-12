package cn.xianyijun.planet.cluster.support;

import cn.xianyijun.planet.cluster.api.Cluster;
import cn.xianyijun.planet.cluster.api.Directory;
import cn.xianyijun.planet.cluster.support.invoker.FailoverClusterInvoker;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invoker;

/**
 * The type Failover cluster.
 *
 * @author xianyijun
 * @date 2018 /1/22
 */
public class FailoverCluster implements Cluster {
    /**
     * The constant NAME.
     */
    public final static String NAME = "failover";

    @Override
    public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
        return new FailoverClusterInvoker<>(directory);
    }

}
