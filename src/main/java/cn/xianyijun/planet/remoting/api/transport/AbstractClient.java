package cn.xianyijun.planet.remoting.api.transport;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.Version;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.common.store.DataStore;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.Client;
import cn.xianyijun.planet.remoting.api.transport.dispatcher.ChannelHandlers;
import cn.xianyijun.planet.utils.ExecutorUtil;
import cn.xianyijun.planet.utils.NamedThreadFactory;
import cn.xianyijun.planet.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The type Abstract client.
 */
@Slf4j
public abstract class AbstractClient extends AbstractEndpoint implements Client {
    /**
     * The constant CLIENT_THREAD_POOL_NAME.
     */
    protected static final String CLIENT_THREAD_POOL_NAME = "RpcClientHandler";

    private static final AtomicInteger CLIENT_THREAD_POOL_ID = new AtomicInteger();

    private static final ScheduledThreadPoolExecutor reconnectExecutorService =
            new ScheduledThreadPoolExecutor(2,new NamedThreadFactory("RpcClientReconnectTimer",true));

    private final Lock connectLock = new ReentrantLock();

    private final boolean sendReconnect;

    private final AtomicInteger reconnectCount = new AtomicInteger(0);
    private final AtomicBoolean reconnectErrorLogFlag = new AtomicBoolean(false);

    private final int reconnectWarningPeriod;
    private final long shutdownTimeout;

    /**
     * The Executor.
     */
    protected volatile ExecutorService executor;
    private volatile ScheduledFuture<?> reconnectExecutorFuture = null;
    private long lastConnectedTime = System.currentTimeMillis();

    /**
     * Instantiates a new Abstract client.
     *
     * @param url     the url
     * @param handler the handler
     * @throws RemotingException the remoting exception
     */
    public AbstractClient(URL url, ChannelHandler handler) throws RemotingException{
        super(url, handler);

        sendReconnect = url.getParameter(Constants.SEND_RECONNECT_KEY, false);

        shutdownTimeout = url.getParameter(Constants.SHUTDOWN_TIMEOUT_KEY, Constants.DEFAULT_SHUTDOWN_TIMEOUT);

        reconnectWarningPeriod = url.getParameter("reconnect.waring.period", 1800);

        try {
            doOpen();
        } catch (Throwable t) {
            close();
            throw new RemotingException(url.toInetSocketAddress(), null,
                    "Failed to start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress()
                            + " connect to the server " + getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }
        try {
            connect();
        } catch (RemotingException t) {
            if (url.getParameter(Constants.CHECK_KEY, true)) {
                close();
                throw t;
            } else {
                log.warn("Failed to start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress()
                        + " connect to the server " + getRemoteAddress() + " (check == false, ignore and retry later!), cause: " + t.getMessage(), t);
            }
        } catch (Throwable t) {
            close();
            throw new RemotingException(url.toInetSocketAddress(), null,
                    "Failed to start " + getClass().getSimpleName() + " " + NetUtils.getLocalAddress()
                            + " connect to the server " + getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }

        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
        executor = (ExecutorService) dataStore.get(Constants.CONSUMER_SIDE, Integer.toString(url.getPort()));

        dataStore.remove(Constants.CONSUMER_SIDE, Integer.toString(url.getPort()));
    }


    private static int getReconnectParam(URL url) {
        int reconnect;
        String param = url.getParameter(Constants.RECONNECT_KEY);
        if (param == null || param.length() == 0 || "true".equalsIgnoreCase(param)) {
            reconnect = Constants.DEFAULT_RECONNECT_PERIOD;
        } else if ("false".equalsIgnoreCase(param)) {
            reconnect = 0;
        } else {
            try {
                reconnect = Integer.parseInt(param);
            } catch (Exception e) {
                throw new IllegalArgumentException("reconnect param must be nonnegative integer or false/true. input is:" + param);
            }
            if (reconnect < 0) {
                throw new IllegalArgumentException("reconnect param must be nonnegative integer or false/true. input is:" + param);
            }
        }
        return reconnect;
    }

    /**
     * Create executor executor service.
     *
     * @return the executor service
     */
    protected ExecutorService createExecutor() {
        return Executors.newCachedThreadPool(new NamedThreadFactory(CLIENT_THREAD_POOL_NAME + CLIENT_THREAD_POOL_ID.incrementAndGet() + "-" + getUrl().getAddress(), true));
    }

    /**
     * Wrap channel handler channel handler.
     *
     * @param url     the url
     * @param handler the handler
     * @return the channel handler
     */
    protected static ChannelHandler wrapChannelHandler(URL url, ChannelHandler handler) {
        url = ExecutorUtil.setThreadName(url, CLIENT_THREAD_POOL_NAME);
        url = url.addParameterIfAbsent(Constants.THREAD_POOL_KEY, Constants.DEFAULT_CLIENT_THREAD_POOL);
        return ChannelHandlers.wrap(handler, url);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        if (sendReconnect && !isConnected()) {
            connect();
        }
        Channel channel = getChannel();
        if (channel == null || !channel.isConnected()) {
            throw new RemotingException(this, "message can not send, because channel is closed . url:" + getUrl());
        }
        channel.send(message, sent);
    }

    /**
     * Connect.
     *
     * @throws RemotingException the remoting exception
     */
    protected void connect() throws RemotingException {
        connectLock.lock();
        try {
            if (isConnected()) {
                return;
            }
            initConnectStatusCheckCommand();
            doConnect();
            if (!isConnected()) {
                throw new RemotingException(this, "Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                        + NetUtils.getLocalHost() + " using rpc version " + Version.getVersion()
                        + ", cause: Connect wait timeout: " + getTimeout() + "ms.");
            } else {
                if (log.isInfoEnabled()) {
                    log.info("Successed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                            + NetUtils.getLocalHost() + " using rpc version " + Version.getVersion()
                            + ", channel is " + this.getChannel());
                }
            }
            reconnectCount.set(0);
            reconnectErrorLogFlag.set(false);
        } catch (RemotingException e) {
            throw e;
        } catch (Throwable e) {
            throw new RemotingException(this, "Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName() + " "
                    + NetUtils.getLocalHost() + " using rpc version " + Version.getVersion()
                    + ", cause: " + e.getMessage(), e);
        } finally {
            connectLock.unlock();
        }
    }

    private synchronized void initConnectStatusCheckCommand() {
        int reconnect = getReconnectParam(getUrl());
        if (reconnect > 0 && (reconnectExecutorFuture == null || reconnectExecutorFuture.isCancelled())) {
            Runnable connectStatusCheckCommand = () -> {
                try {
                    if (!isConnected()) {
                        connect();
                    } else {
                        lastConnectedTime = System.currentTimeMillis();
                    }
                } catch (Throwable t) {
                    String errorMsg = "client reconnect to " + getUrl().getAddress() + " find error . url: " + getUrl();
                    if (System.currentTimeMillis() - lastConnectedTime > shutdownTimeout) {
                        if (!reconnectErrorLogFlag.get()) {
                            reconnectErrorLogFlag.set(true);
                            log.error(errorMsg, t);
                            return;
                        }
                    }
                    if (reconnectCount.getAndIncrement() % reconnectWarningPeriod == 0) {
                        log.warn(errorMsg, t);
                    }
                }
            };
            reconnectExecutorFuture = reconnectExecutorService.scheduleWithFixedDelay(connectStatusCheckCommand, reconnect, reconnect, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void reConnect() throws RemotingException {
        disconnect();
        connect();
    }

    /**
     * Disconnect.
     */
    public void disconnect() {
        connectLock.lock();
        try {
            destroyConnectStatusCheckCommand();
            try {
                Channel channel = getChannel();
                if (channel != null) {
                    channel.close();
                }
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
            }
            try {
                doDisConnect();
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
            }
        } finally {
            connectLock.unlock();
        }
    }

    private synchronized void destroyConnectStatusCheckCommand() {
        try {
            if (reconnectExecutorFuture != null && !reconnectExecutorFuture.isDone()) {
                reconnectExecutorFuture.cancel(true);
                reconnectExecutorService.purge();
            }
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            if (executor != null) {
                ExecutorUtil.shutdownNow(executor, 100);
            }
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
        try {
            super.close();
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
        try {
            disconnect();
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
        try {
            doClose();
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
    }

    @Override
    public void close(int timeout) {
        ExecutorUtil.gracefulShutdown(executor, timeout);
        close();
    }

    /**
     * Gets connect address.
     *
     * @return the connect address
     */
    public InetSocketAddress getConnectAddress() {
        return new InetSocketAddress(NetUtils.filterLocalHost(getUrl().getHost()), getUrl().getPort());
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        Channel channel = getChannel();
        if (channel == null) {
            return getUrl().toInetSocketAddress();
        }
        return channel.getRemoteAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        Channel channel = getChannel();
        if (channel == null) {
            return InetSocketAddress.createUnresolved(NetUtils.getLocalHost(), 0);
        }
        return channel.getLocalAddress();
    }

    @Override
    public boolean isConnected() {
        Channel channel = getChannel();
        return channel != null && channel.isConnected();
    }

    @Override
    public Object getAttribute(String key) {
        Channel channel = getChannel();
        if (channel == null) {
            return null;
        }
        return channel.getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        Channel channel = getChannel();
        if (channel == null) {
            return;
        }
        channel.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        Channel channel = getChannel();
        if (channel == null) {
            return;
        }
        channel.removeAttribute(key);
    }

    @Override
    public boolean hasAttribute(String key) {
        Channel channel = getChannel();
        if (channel == null) {
            return false;
        }
        return channel.hasAttribute(key);
    }


    //================== abstract method ==========================

    /**
     * Do open.
     *
     * @throws Throwable the throwable
     */
    protected abstract void doOpen() throws Throwable;

    /**
     * Do close.
     *
     * @throws Throwable the throwable
     */
    protected abstract void doClose() throws Throwable;

    /**
     * Do connect.
     *
     * @throws Throwable the throwable
     */
    protected abstract void doConnect() throws Throwable;

    /**
     * Do dis connect.
     *
     * @throws Throwable the throwable
     */
    protected abstract void doDisConnect() throws Throwable;

    /**
     * Gets channel.
     *
     * @return the channel
     */
    protected abstract Channel getChannel();


}
