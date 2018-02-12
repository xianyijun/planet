package cn.xianyijun.planet.common.threadpool;


import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.utils.JVMUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * The type Abort policy with report.
 *
 * @author xianyijun
 */
@Slf4j
public class AbortPolicyWithReport extends ThreadPoolExecutor.AbortPolicy {
    private final String threadName;

    private final URL url;

    private static volatile long lastPrintTime = 0;

    private static Semaphore guard = new Semaphore(1);

    /**
     * Instantiates a new Abort policy with report.
     *
     * @param threadName the thread name
     * @param url        the url
     */
    public AbortPolicyWithReport(String threadName, URL url) {
        this.threadName = threadName;
        this.url = url;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        String msg = String.format("Thread pool is EXHAUSTED!" +
                        " Thread Name: %s, Pool Size: %d (active: %d, rpc: %d, max: %d, largest: %d), Task: %d (completed: %d)," +
                        " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s), in %s://%s:%d!",
                threadName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
                e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating(),
                url.getProtocol(), url.getIp(), url.getPort());
        log.warn(msg);
        dumpJStack();
        throw new RejectedExecutionException(msg);
    }

    private void dumpJStack() {
        long now = System.currentTimeMillis();

        //十分钟打一次
        if (now - lastPrintTime < 10 * 60 * 1000) {
            return;
        }

        if (!guard.tryAcquire()) {
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            String dumpPath = url.getParameter(Constants.DUMP_DIRECTORY, System.getProperty("user.home"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String dateStr = sdf.format(new Date());
            FileOutputStream jstackStream = null;
            try {
                jstackStream = new FileOutputStream(new File(dumpPath, "rpc_JStack.log" + "." + dateStr));
                JVMUtil.jstack(jstackStream);
            } catch (Throwable t) {
                log.error("dump jstack error", t);
            } finally {
                guard.release();
                if (jstackStream != null) {
                    try {
                        jstackStream.flush();
                        jstackStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }

            lastPrintTime = System.currentTimeMillis();
        });

    }

}
