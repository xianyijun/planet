package cn.xianyijun.planet.registry.api.support;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.Version;
import cn.xianyijun.planet.registry.api.NotifyListener;
import cn.xianyijun.planet.registry.api.RegistryService;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.utils.NamedThreadFactory;
import cn.xianyijun.planet.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The type Rpc registry.
 */
@Slf4j
public class RpcRegistry extends FailbackRegistry {

    private static final int RECONNECT_PERIOD_DEFAULT = 3 * 1000;

    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("RpcRegistryReconnectTimer", true));

    private final ScheduledFuture<?> reconnectFuture;

    private final ReentrantLock clientLock = new ReentrantLock();

    private final Invoker<RegistryService> registryInvoker;

    private final RegistryService registryService;

    /**
     * Instantiates a new Rpc registry.
     *
     * @param registryInvoker the registry invoker
     * @param registryService the registry service
     */
    public RpcRegistry(Invoker<RegistryService> registryInvoker, RegistryService registryService) {
        super(registryInvoker.getUrl());
        this.registryInvoker = registryInvoker;
        this.registryService = registryService;

        int reconnectPeriod = registryInvoker.getUrl().getParameter(Constants.REGISTRY_RECONNECT_PERIOD_KEY, RECONNECT_PERIOD_DEFAULT);
        reconnectFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {

            try {
                connect();
            } catch (Throwable t) {
                log.error("Unexpected error occur at reconnect, cause: " + t.getMessage(), t);
            }
        }, reconnectPeriod, reconnectPeriod, TimeUnit.MILLISECONDS);
    }

    /**
     * Connect.
     */
    protected final void connect() {
        try {
            if (isAvailable()) {
                return;
            }
            clientLock.lock();
            try {
                if (isAvailable()) {
                    return;
                }
                recover();
            } finally {
                clientLock.unlock();
            }
        } catch (Throwable t) { // 忽略所有异常，等待下次重试
            if (getUrl().getParameter(Constants.CHECK_KEY, true)) {
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                }
                throw new RuntimeException(t.getMessage(), t);
            }
            log.error("Failed to connect to registry " + getUrl().getAddress() + " from provider/consumer " + NetUtils.getLocalHost() + " use rpc " + Version.getVersion() + ", cause: " + t.getMessage(), t);
        }
    }

    @Override
    public boolean isAvailable() {
        return registryInvoker == null ? false :registryInvoker.isAvailable();
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            if (!reconnectFuture.isCancelled()) {
                reconnectFuture.cancel(true);
            }
        } catch (Throwable t) {
            log.warn("Failed to cancel reconnect timer", t);
        }
        registryInvoker.destroy();
    }

    @Override
    protected void doRegister(URL url) {
        registryService.register(url);
    }

    @Override
    protected void doUnregister(URL url) {
        registryService.unRegister(url);
    }

    @Override
    protected void doSubscribe(URL url, NotifyListener listener) {
        registryService.subscribe(url, listener);
    }

    @Override
    protected void doUnSubscribe(URL url, NotifyListener listener) {
        registryService.unSubscribe(url, listener);
    }

    @Override
    public List<URL> lookup(URL url) {
        return registryService.lookup(url);
    }

}
