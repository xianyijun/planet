package cn.xianyijun.planet.cluster.support.invoker;

import cn.xianyijun.planet.cluster.api.Directory;
import cn.xianyijun.planet.cluster.api.LoadBalance;
import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.Version;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Result;
import cn.xianyijun.planet.utils.NetUtils;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The type Failover cluster invoker.
 *
 * @param <T> the type parameter
 * @author xianyijun
 */
@Slf4j
@ToString
public class FailoverClusterInvoker<T> extends AbstractClusterInvoker<T> {

    /**
     * Instantiates a new Failover cluster invoker.
     *
     * @param directory the directory
     */
    public FailoverClusterInvoker(Directory<T> directory) {
        super(directory);
    }


    @Override
    public Result doInvoke(Invocation invocation, final List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException {
        List<Invoker<T>> copyInvokers = invokers;
        checkInvokers(copyInvokers, invocation);
        int len = getUrl().getMethodParameter(invocation.getMethodName(), Constants.RETRIES_KEY, Constants.DEFAULT_RETRIES) + 1;
        if (len <= 0) {
            len = 1;
        }
        RpcException le = null;
        List<Invoker<T>> invoked = new ArrayList<>(copyInvokers.size());
        Set<String> providers = new HashSet<>(len);
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                checkWhetherDestroyed();
                copyInvokers = list(invocation);
                checkInvokers(copyInvokers, invocation);
            }
            Invoker<T> invoker = select(loadbalance, invocation, copyInvokers, invoked);
            invoked.add(invoker);
            try {
                Result result = invoker.invoke(invocation);
                if (le != null && log.isWarnEnabled()) {
                    log.warn("Although retry the method " + invocation.getMethodName()
                            + " in the service " + getInterface().getName()
                            + " was successful by the provider " + invoker.getUrl().getAddress()
                            + ", but there have been failed providers " + providers
                            + " (" + providers.size() + "/" + copyInvokers.size()
                            + ") from the registry " + directory.getUrl().getAddress()
                            + " on the consumer " + NetUtils.getLocalHost()
                            + " using the rpc version " + Version.getVersion() + ". Last error is: "
                            + le.getMessage(), le);
                }
                return result;
            } catch (RpcException e) {
                if (e.isBiz()) {
                    throw e;
                }
                le = e;
            } catch (Throwable e) {
                le = new RpcException(e.getMessage(), e);
            } finally {
                providers.add(invoker.getUrl().getAddress());
            }
        }
        throw new RpcException(le.getCode(), "Failed to invoke the method " + invocation.getMethodName() + " in the service " + getInterface().getName() + ". Tried " + len + " times of the providers " + providers + " (" + providers.size() + "/" + copyInvokers.size() + ") from the registry " + directory.getUrl().getAddress() + " on the consumer " + NetUtils.getLocalHost() + " using the rpc version " + Version.getVersion() + ". Last error is: " + le.getMessage(), le.getCause() != null ? le.getCause() : le);
    }

}
