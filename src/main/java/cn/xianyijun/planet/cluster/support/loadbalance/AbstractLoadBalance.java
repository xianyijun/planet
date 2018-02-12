package cn.xianyijun.planet.cluster.support.loadbalance;


import cn.xianyijun.planet.cluster.api.LoadBalance;
import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;

import java.util.List;

/**
 * Created by xianyijun on 2018/1/27.
 */
public abstract class AbstractLoadBalance implements LoadBalance {

    /**
     * Calculate warmup weight int.
     *
     * @param uptime the uptime
     * @param warmup the warmup
     * @param weight the weight
     * @return the int
     */
    private static int calculateWarmupWeight(int uptime, int warmup, int weight) {
        int ww = (int) ((float) uptime / ((float) warmup / (float) weight));
        return ww < 1 ? 1 : (ww > weight ? weight : ww);
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        if (invokers == null || invokers.size() == 0){
            return null;
        }
        if (invokers.size() == 1){
            return invokers.get(0);
        }
        return doSelect(invokers, url, invocation);
    }

    /**
     * Do select invoker.
     *
     * @param <T>        the type parameter
     * @param invokers   the invokers
     * @param url        the url
     * @param invocation the invocation
     * @return the invoker
     */
    protected abstract <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation);

    /**
     * Gets weight.
     *
     * @param invoker    the invoker
     * @param invocation the invocation
     * @return the weight
     */
    int getWeight(Invoker<?> invoker, Invocation invocation) {
        int weight = invoker.getUrl().getMethodParameter(invocation.getMethodName(), Constants.WEIGHT_KEY, Constants.DEFAULT_WEIGHT);
        if (weight > 0) {
            long timestamp = invoker.getUrl().getParameter(Constants.REMOTE_TIMESTAMP_KEY, 0L);
            if (timestamp > 0L) {
                int uptime = (int) (System.currentTimeMillis() - timestamp);
                int warmup = invoker.getUrl().getParameter(Constants.WARM_UP_KEY, Constants.DEFAULT_WARM_UP);
                if (uptime > 0 && uptime < warmup) {
                    weight = calculateWarmupWeight(uptime, warmup, weight);
                }
            }
        }
        return weight;
    }

}
