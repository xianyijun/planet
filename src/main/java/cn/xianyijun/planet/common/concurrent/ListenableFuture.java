package cn.xianyijun.planet.common.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Created by xianyijun on 2018/2/4.
 *
 * @param <V> the type parameter
 */
public interface ListenableFuture<V> extends Future<V>{

    /**
     * Add listener.
     *
     * @param listener the listener
     * @param executor the executor
     */
    void addListener(Runnable listener, Executor executor);

    /**
     * Add listener.
     *
     * @param listener the listener
     */
    void addListener(Runnable listener);
}
