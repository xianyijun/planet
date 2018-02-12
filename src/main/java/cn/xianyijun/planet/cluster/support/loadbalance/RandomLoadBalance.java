package cn.xianyijun.planet.cluster.support.loadbalance;


import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;

import java.util.List;
import java.util.Random;

/**
 * The type Random load balance.
 * @author xianyijun
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    /**
     * The constant NAME.
     */
    public static final String NAME = "random";

    private final Random random = new Random();

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size();
        int totalWeight = 0;
        boolean sameWeight = true;
        for (int i = 0; i < length; i++) {
            int weight = getWeight(invokers.get(i), invocation);
            totalWeight += weight;
            if (sameWeight && i > 0
                    && weight != getWeight(invokers.get(i - 1), invocation)) {
                sameWeight = false;
            }
        }
        if (totalWeight > 0 && !sameWeight) {
            int offset = random.nextInt(totalWeight);
            for (int i = 0; i < length; i++) {
                offset -= getWeight(invokers.get(i), invocation);
                if (offset < 0) {
                    return invokers.get(i);
                }
            }
        }
        return invokers.get(random.nextInt(length));
    }

}
