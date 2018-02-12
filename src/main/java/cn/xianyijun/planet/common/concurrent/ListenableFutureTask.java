package cn.xianyijun.planet.common.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

/**
 * The type Listenable future task.
 *
 * @param <V> the type parameter
 * @author xianyijun
 * @date 2018 /2/4
 */
public class ListenableFutureTask<V> extends FutureTask<V> implements ListenableFuture<V>{

    private final ExecutionList executionList = new ExecutionList();

    /**
     * Instantiates a new Listenable future task.
     *
     * @param callable the callable
     */
    public ListenableFutureTask(Callable<V> callable) {
        super(callable);
    }

    /**
     * Instantiates a new Listenable future task.
     *
     * @param runnable the runnable
     * @param result   the result
     */
    public ListenableFutureTask(Runnable runnable, V result) {
        super(runnable, result);
    }

    /**
     * Create listenable future task.
     *
     * @param <V>      the type parameter
     * @param callable the callable
     * @return the listenable future task
     */
    public static <V> ListenableFutureTask<V> create(Callable<V> callable) {
        return new ListenableFutureTask<>(callable);
    }

    /**
     * Create listenable future task.
     *
     * @param <V>      the type parameter
     * @param runnable the runnable
     * @param result   the result
     * @return the listenable future task
     */
    public static <V> ListenableFutureTask<V> create(
            Runnable runnable, V result) {
        return new ListenableFutureTask<>(runnable, result);
    }


    @Override
    public void addListener(Runnable listener, Executor exec) {
        executionList.add(listener, exec);
    }

    @Override
    public void addListener(Runnable listener) {
        executionList.add(listener, null);
    }
}
