package cn.xianyijun.planet.registry.api.support;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.SkipFailbackWrapperException;
import cn.xianyijun.planet.registry.api.NotifyListener;
import cn.xianyijun.planet.utils.ConcurrentHashSet;
import cn.xianyijun.planet.utils.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Failback registry.
 * @author xianyijun
 */
@Slf4j
public abstract class FailbackRegistry extends AbstractRegistry {


    private final ScheduledExecutorService retryExecutor = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("RpcRegistryFailedRetryTimer", true));

    private final ScheduledFuture<?> retryFuture;

    private final Set<URL> failedRegistered = new ConcurrentHashSet<>();

    private final Set<URL> failedUnregistered = new ConcurrentHashSet<>();

    private final ConcurrentMap<URL, Set<NotifyListener>> failedSubscribed = new ConcurrentHashMap<>();

    private final ConcurrentMap<URL, Set<NotifyListener>> failedUnSubscribed = new ConcurrentHashMap<>();

    private final ConcurrentMap<URL, Map<NotifyListener, List<URL>>> failedNotified = new ConcurrentHashMap<>();

    private AtomicBoolean destroyed = new AtomicBoolean(false);

    /**
     * Instantiates a new Failback registry.
     *
     * @param url the url
     */
    public FailbackRegistry(URL url) {
        super(url);
        int retryPeriod = url.getParameter(Constants.REGISTRY_RETRY_PERIOD_KEY, Constants.DEFAULT_REGISTRY_RETRY_PERIOD);
        this.retryFuture = retryExecutor.scheduleWithFixedDelay(() -> {
            // 检测并连接注册中心
            try {
                retry();
            } catch (Throwable t) { // 防御性容错
                log.error("Unexpected error occur at failed retry, cause: " + t.getMessage(), t);
            }
        }, retryPeriod, retryPeriod, TimeUnit.MILLISECONDS);
    }

    /**
     * Retry.
     */
    protected void retry(){
        if (!failedNotified.isEmpty()){
            Set<URL> failed = new HashSet<>(failedRegistered);
            if (!failed.isEmpty()){
                failed.stream().forEach(url ->{
                    doRegister(url);
                    failedRegistered.remove(url);
                });
            }
        }

        if (!failedUnregistered.isEmpty()){
            Set<URL> failed = new HashSet<>(failedUnregistered);
            if (!failed.isEmpty()){
                failed.stream().forEach(url ->{
                    doUnregister(url);
                    failedUnregistered.remove(url);
                });
            }
        }

        if (!failedSubscribed.isEmpty()) {
            Map<URL, Set<NotifyListener>> failed = new HashMap<>(failedSubscribed);
            for (Map.Entry<URL, Set<NotifyListener>> entry : new HashMap<>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (!failed.isEmpty()) {
                try {
                    for (Map.Entry<URL, Set<NotifyListener>> entry : failed.entrySet()) {
                        URL url = entry.getKey();
                        Set<NotifyListener> listeners = entry.getValue();
                        for (NotifyListener listener : listeners) {
                            try {
                                doSubscribe(url, listener);
                                listeners.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                log.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    log.warn("Failed to retry subscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        if (!failedUnSubscribed.isEmpty()) {
            Map<URL, Set<NotifyListener>> failed = new HashMap<>(failedUnSubscribed);
            for (Map.Entry<URL, Set<NotifyListener>> entry : new HashMap<>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.isEmpty()) {
                try {
                    for (Map.Entry<URL, Set<NotifyListener>> entry : failed.entrySet()) {
                        URL url = entry.getKey();
                        Set<NotifyListener> listeners = entry.getValue();
                        for (NotifyListener listener : listeners) {
                            try {
                                doUnSubscribe(url, listener);
                                listeners.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                log.warn("Failed to retry unsubscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    log.warn("Failed to retry unSubscribe " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
        if (!failedNotified.isEmpty()) {
            Map<URL, Map<NotifyListener, List<URL>>> failed = new HashMap<URL, Map<NotifyListener, List<URL>>>(failedNotified);
            for (Map.Entry<URL, Map<NotifyListener, List<URL>>> entry : new HashMap<>(failed).entrySet()) {
                if (entry.getValue() == null || entry.getValue().isEmpty()) {
                    failed.remove(entry.getKey());
                }
            }
            if (failed.isEmpty()) {
                try {
                    for (Map<NotifyListener, List<URL>> values : failed.values()) {
                        for (Map.Entry<NotifyListener, List<URL>> entry : values.entrySet()) {
                            try {
                                NotifyListener listener = entry.getKey();
                                List<URL> urls = entry.getValue();
                                listener.notify(urls);
                                values.remove(listener);
                            } catch (Throwable t) { // 忽略所有异常，等待下次重试
                                log.warn("Failed to retry notify " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                            }
                        }
                    }
                } catch (Throwable t) { // 忽略所有异常，等待下次重试
                    log.warn("Failed to retry notify " + failed + ", waiting for again, cause: " + t.getMessage(), t);
                }
            }
        }
    }

    @Override
    public void register(URL url) {
        if (destroyed.get()){
            return;
        }
        super.register(url);
        failedRegistered.remove(url);
        failedUnregistered.remove(url);
        try {
            doRegister(url);
        } catch (Exception e) {
            Throwable t = e;

            boolean check = getUrl().getParameter(Constants.CHECK_KEY, true)
                    && url.getParameter(Constants.CHECK_KEY, true)
                    && !Constants.CONSUMER_PROTOCOL.equals(url.getProtocol());
            boolean skipFailback = t instanceof SkipFailbackWrapperException;
            if (check || skipFailback) {
                if (skipFailback) {
                    t = t.getCause();
                }
                throw new IllegalStateException("Failed to register " + url + " to registry " + getUrl().getAddress() + ", cause: " + t.getMessage(), t);
            } else {
                log.error("Failed to register " + url + ", waiting for retry, cause: " + t.getMessage(), t);
            }

            // Record a failed registration request to a failed list, retry regularly
            failedRegistered.add(url);
        }
    }

    @Override
    public void destroy() {
        if (!canDestroy()){
            return;
        }
        super.destroy();
        try {
            retryFuture.cancel(true);
        } catch (Throwable t) {
            log.warn(t.getMessage(), t);
        }
    }

    @Override
    protected void recover() throws Exception {
        Set<URL> recoverRegistered = new HashSet<>(getRegistered());
        if (!recoverRegistered.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("Recover register url " + recoverRegistered);
            }
            failedRegistered.addAll(recoverRegistered);
        }
        // subscribe
        Map<URL, Set<NotifyListener>> recoverSubscribed = new HashMap<>(getSubscribed());
        if (!recoverSubscribed.isEmpty()) {
            if (log.isInfoEnabled()) {
                log.info("Recover subscribe url " + recoverSubscribed.keySet());
            }
            for (Map.Entry<URL, Set<NotifyListener>> entry : recoverSubscribed.entrySet()) {
                URL url = entry.getKey();
                for (NotifyListener listener : entry.getValue()) {
                    addFailedSubscribed(url, listener);
                }
            }
        }
    }

    private void addFailedSubscribed(URL url, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(url);
        if (listeners == null) {
            failedSubscribed.putIfAbsent(url, new ConcurrentHashSet<>());
            listeners = failedSubscribed.get(url);
        }
        listeners.add(listener);
    }

    @Override
    public void subscribe(URL url, NotifyListener listener) {
        if (destroyed.get()){
            return;
        }
        super.subscribe(url, listener);
        removeFailedSubscribed(url, listener);
        try {
            doSubscribe(url, listener);
        } catch (Exception e) {
            Throwable t = e;

            List<URL> urls = getCacheUrls(url);
            if (urls != null && urls.size() > 0) {
                notify(url, listener, urls);
                log.error("Failed to subscribe " + url + ", Using cached list: " + urls + " from cache file: " + getUrl().getParameter(Constants.FILE_KEY, System.getProperty("user.home") + "/rpc-registry-" + url.getHost() + ".cache") + ", cause: " + t.getMessage(), t);
            } else {
                boolean check = getUrl().getParameter(Constants.CHECK_KEY, true)
                        && url.getParameter(Constants.CHECK_KEY, true);
                boolean skipFailback = t instanceof SkipFailbackWrapperException;
                if (check || skipFailback) {
                    if (skipFailback) {
                        t = t.getCause();
                    }
                    throw new IllegalStateException("Failed to subscribe " + url + ", cause: " + t.getMessage(), t);
                } else {
                    log.error("Failed to subscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
                }
            }

            addFailedSubscribed(url, listener);
        }
    }

    private void removeFailedSubscribed(URL url, NotifyListener listener) {
        Set<NotifyListener> listeners = failedSubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        listeners = failedUnSubscribed.get(url);
        if (listeners != null) {
            listeners.remove(listener);
        }
        Map<NotifyListener, List<URL>> notified = failedNotified.get(url);
        if (notified != null) {
            notified.remove(listener);
        }
    }

    @Override
    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
        if (url == null) {
            throw new IllegalArgumentException("notify url == null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("notify listener == null");
        }
        try {
            doNotify(url, listener, urls);
        } catch (Exception t) {
            Map<NotifyListener, List<URL>> listeners = failedNotified.get(url);
            if (listeners == null) {
                failedNotified.putIfAbsent(url, new ConcurrentHashMap<NotifyListener, List<URL>>());
                listeners = failedNotified.get(url);
            }
            listeners.put(listener, urls);
            log.error("Failed to notify for subscribe " + url + ", waiting for retry, cause: " + t.getMessage(), t);
        }
    }

    /**
     * Do notify.
     *
     * @param url      the url
     * @param listener the listener
     * @param urls     the urls
     */
    protected void doNotify(URL url, NotifyListener listener, List<URL> urls) {
        super.notify(url, listener, urls);
    }


    /**
     * Can destroy boolean.
     *
     * @return the boolean
     */
    protected boolean canDestroy(){
        return destroyed.compareAndSet(false, true);
    }

    /**
     * Do un subscribe.
     *
     * @param url      the url
     * @param listener the listener
     */
    protected abstract void doUnSubscribe(URL url, NotifyListener listener);

    /**
     * Do unregister.
     *
     * @param url the url
     */
    protected abstract void doUnregister(URL url);

    /**
     * Do subscribe.
     *
     * @param url      the url
     * @param listener the listener
     */
    protected abstract void doSubscribe(URL url, NotifyListener listener);

    /**
     * Do register.
     *
     * @param url the url
     */
    protected abstract void doRegister(URL url);

}
