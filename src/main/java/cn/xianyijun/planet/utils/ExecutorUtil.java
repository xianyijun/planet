package cn.xianyijun.planet.utils;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The type Executor util.
 */
@Slf4j
public class ExecutorUtil {
    private static final ThreadPoolExecutor shutdownExecutor = new ThreadPoolExecutor(0, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(100),
            new NamedThreadFactory("Close-ExecutorService-Timer", true));

    /**
     * Sets thread name.
     *
     * @param url         the url
     * @param defaultName the default name
     * @return the thread name
     */
    public static URL setThreadName(URL url, String defaultName) {
        String name = url.getParameter(Constants.THREAD_NAME_KEY, defaultName);
        name = new StringBuilder(32).append(name).append("-").append(url.getAddress()).toString();
        url = url.addParameter(Constants.THREAD_NAME_KEY, name);
        return url;
    }

    /**
     * Is shutdown boolean.
     *
     * @param executor the executor
     * @return the boolean
     */
    public static boolean isShutdown(Executor executor) {
        if (executor instanceof ExecutorService) {
            if (((ExecutorService) executor).isShutdown()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Graceful shutdown.
     *
     * @param executor the executor
     * @param timeout  the timeout
     */
    public static void gracefulShutdown(Executor executor, int timeout) {
        if (!(executor instanceof ExecutorService) || isShutdown(executor)) {
            return;
        }
        final ExecutorService es = (ExecutorService) executor;
        try {
            es.shutdown(); // Disable new tasks from being submitted
        } catch (SecurityException ex2) {
            return;
        } catch (NullPointerException ex2) {
            return;
        }
        try {
            if (!es.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                es.shutdownNow();
            }
        } catch (InterruptedException ex) {
            es.shutdownNow();
            Thread.currentThread().interrupt();
        }
        if (!isShutdown(es)) {
            newThreadToCloseExecutor(es);
        }
    }

    /**
     * Shutdown now.
     *
     * @param executor the executor
     * @param timeout  the timeout
     */
    public static void shutdownNow(Executor executor, final int timeout) {
        if (!(executor instanceof ExecutorService) || isShutdown(executor)) {
            return;
        }
        final ExecutorService es = (ExecutorService) executor;
        try {
            es.shutdownNow();
        } catch (SecurityException ex2) {
            return;
        } catch (NullPointerException ex2) {
            return;
        }
        try {
            es.awaitTermination(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (!isShutdown(es)) {
            newThreadToCloseExecutor(es);
        }
    }

    private static void newThreadToCloseExecutor(final ExecutorService es) {
        if (!isShutdown(es)) {
            shutdownExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < 1000; i++) {
                            es.shutdownNow();
                            if (es.awaitTermination(10, TimeUnit.MILLISECONDS)) {
                                break;
                            }
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } catch (Throwable e) {
                        log.warn(e.getMessage(), e);
                    }
                }
            });
        }
    }

}
