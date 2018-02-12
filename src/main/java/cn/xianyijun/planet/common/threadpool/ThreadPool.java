package cn.xianyijun.planet.common.threadpool;

import cn.xianyijun.planet.common.URL;

import java.util.concurrent.Executor;

/**
 * The interface Thread pool.
 */
public interface ThreadPool {
    /**
     * Gets executor.
     *
     * @param url the url
     * @return the executor
     */
    Executor getExecutor(URL url);
}
