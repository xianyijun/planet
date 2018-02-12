package cn.xianyijun.planet.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xianyijun on 2017/10/28.
 */
public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);

    private final AtomicInteger mThreadNum = new AtomicInteger(1);

    private final String mPrefix;

    private final boolean mDaemo;

    private final ThreadGroup mGroup;

    /**
     * Instantiates a new Named thread factory.
     */
    public NamedThreadFactory() {
        this("pool-" + POOL_SEQ.getAndIncrement(), false);
    }

    /**
     * Instantiates a new Named thread factory.
     *
     * @param prefix the prefix
     */
    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    /**
     * Instantiates a new Named thread factory.
     *
     * @param prefix the prefix
     * @param daemo  the daemo
     */
    public NamedThreadFactory(String prefix, boolean daemo) {
        mPrefix = prefix + "-thread-";
        mDaemo = daemo;
        SecurityManager s = System.getSecurityManager();
        mGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable runnable) {
        String name = mPrefix + mThreadNum.getAndIncrement();
        Thread ret = new Thread(mGroup, runnable, name, 0);
        ret.setDaemon(mDaemo);
        return ret;
    }

    /**
     * Gets thread group.
     *
     * @return the thread group
     */
    public ThreadGroup getThreadGroup() {
        return mGroup;
    }
}
