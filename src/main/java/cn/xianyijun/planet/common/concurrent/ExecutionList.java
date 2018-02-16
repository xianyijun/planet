package cn.xianyijun.planet.common.concurrent;

import cn.xianyijun.planet.utils.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author xianyijun
 * @date 2018/2/4
 */
@Slf4j
public final class ExecutionList {

    private RunnableExecutorPair runnables;

    private boolean executed;

    private static final Executor DEFAULT_EXECUTOR = new ThreadPoolExecutor(1, 10, 60000L, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), new NamedThreadFactory("RpcFutureCallbackDefault", true));


    /**
     * Add.
     *
     * @param runnable the runnable
     * @param executor the executor
     */
    public void add(Runnable runnable, Executor executor) {
        if (runnable == null) {
            throw new NullPointerException("Runnable can not be null!");
        }
        if (executor == null) {
            log.info("Executor for listenable future is null, will use default executor!");
            executor = DEFAULT_EXECUTOR;
        }
        synchronized (this) {
            if (!executed) {
                runnables = new RunnableExecutorPair(runnable, executor, runnables);
                return;
            }
        }
        executeListener(runnable, executor);
    }

    private static void executeListener(Runnable runnable, Executor executor) {
        try {
            executor.execute(runnable);
        } catch (RuntimeException e) {
            log.error("RuntimeException while executing runnable "
                    + runnable + " with executor " + executor, e);
        }
    }

    private static final class RunnableExecutorPair {
        /**
         * The Runnable.
         */
        final Runnable runnable;
        /**
         * The Executor.
         */
        final Executor executor;
        /**
         * The Next.
         */
        RunnableExecutorPair next;

        /**
         * Instantiates a new Runnable executor pair.
         *
         * @param runnable the runnable
         * @param executor the executor
         * @param next     the next
         */
        RunnableExecutorPair(Runnable runnable, Executor executor, RunnableExecutorPair next) {
            this.runnable = runnable;
            this.executor = executor;
            this.next = next;
        }
    }
}
