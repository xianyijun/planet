package cn.xianyijun.planet.remoting.api.transport;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.common.store.DataStore;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.Server;
import cn.xianyijun.planet.utils.ExecutorUtil;
import cn.xianyijun.planet.utils.NetUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

;

/**
 * The type Abstract server.
 * @author xianyijun
 */
@Slf4j
@Getter
public abstract class AbstractServer extends AbstractEndpoint implements Server {

    /**
     * The constant SERVER_THREAD_POOL_NAME.
     */
    protected static final String SERVER_THREAD_POOL_NAME = "RpcServerHandler";

    /**
     * The Executor.
     */
    ExecutorService executor;
    private InetSocketAddress localAddress;
    private InetSocketAddress bindAddress;
    private int accepts;
    private int idleTimeout = 600;

    /**
     * Instantiates a new Abstract server.
     *
     * @param url     the url
     * @param handler the handler
     * @throws RemotingException the remoting exception
     */
    public AbstractServer(URL url, ChannelHandler handler) throws RemotingException {
        super(url, handler);
        localAddress = getUrl().toInetSocketAddress();
        String bindIp = getUrl().getParameter(Constants.BIND_IP_KEY, getUrl().getHost());
        int bindPort = getUrl().getParameter(Constants.BIND_PORT_KEY, getUrl().getPort());
        if (url.getParameter(Constants.ANY_HOST_KEY, false) || NetUtils.isInvalidLocalHost(bindIp)) {
            bindIp = NetUtils.ANY_HOST;
        }
        bindAddress = new InetSocketAddress(bindIp, bindPort);
        this.accepts = url.getParameter(Constants.ACCEPTS_KEY, Constants.DEFAULT_ACCEPTS);
        this.idleTimeout = url.getParameter(Constants.IDLE_TIMEOUT_KEY, Constants.DEFAULT_IDLE_TIMEOUT);
        try {
            doOpen();
        } catch (Throwable t) {
            throw new RemotingException(url.toInetSocketAddress(), null, "Failed to bind " + getClass().getSimpleName()
                    + " on " + getLocalAddress() + ", cause: " + t.getMessage(), t);
        }
        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();

        executor = (ExecutorService) dataStore.get(Constants.EXECUTOR_SERVICE_COMPONENT_KEY, Integer.toString(url.getPort()));
    }


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

    @Override
    public void reset(URL url) {
        if (url == null) {
            return;
        }
        try {
            if (url.hasParameter(Constants.ACCEPTS_KEY)) {
                int a = url.getParameter(Constants.ACCEPTS_KEY, 0);
                if (a > 0) {
                    this.accepts = a;
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        try {
            if (url.hasParameter(Constants.IDLE_TIMEOUT_KEY)) {
                int t = url.getParameter(Constants.IDLE_TIMEOUT_KEY, 0);
                if (t > 0) {
                    this.idleTimeout = t;
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        try {
            if (url.hasParameter(Constants.THREADS_KEY)
                    && executor instanceof ThreadPoolExecutor && !executor.isShutdown()) {
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                int threads = url.getParameter(Constants.THREADS_KEY, 0);
                int max = threadPoolExecutor.getMaximumPoolSize();
                int core = threadPoolExecutor.getCorePoolSize();
                if (threads > 0 && (threads != max || threads != core)) {
                    if (threads < core) {
                        threadPoolExecutor.setCorePoolSize(threads);
                        if (core == max) {
                            threadPoolExecutor.setMaximumPoolSize(threads);
                        }
                    } else {
                        threadPoolExecutor.setMaximumPoolSize(threads);
                        if (core == max) {
                            threadPoolExecutor.setCorePoolSize(threads);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        super.setUrl(getUrl().addParameters(url.getParameters()));
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        Collection<Channel> channels = getChannels();
        for (Channel channel : channels) {
            if (channel.isConnected()) {
                channel.send(message, sent);
            }
        }
    }

    @Override
    public void close() {
        ExecutorUtil.shutdownNow(executor, 100);
        try {
            super.close();
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

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * Gets bind address.
     *
     * @return the bind address
     */
    public InetSocketAddress getBindAddress() {
        return bindAddress;
    }

    /**
     * Gets accepts.
     *
     * @return the accepts
     */
    public int getAccepts() {
        return accepts;
    }

    /**
     * Gets idle timeout.
     *
     * @return the idle timeout
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    @Override
    public void connected(Channel ch) throws RemotingException {
        // 如果server已进入关闭流程，拒绝新的连接
        if (this.isClosing() || this.isClosed()) {
            log.warn("Close new channel " + ch + ", cause: server is closing or has been closed. For example, receive a new connect request while in shutdown process.");
            ch.close();
            return;
        }

        Collection<Channel> channels = getChannels();
        if (accepts > 0 && channels.size() > accepts) {
            log.error("Close channel " + ch + ", cause: The server " + ch.getLocalAddress() + " connections greater than max config " + accepts);
            ch.close();
            return;
        }
        super.connected(ch);
    }

    @Override
    public void disConnected(Channel ch) throws RemotingException {
        Collection<Channel> channels = getChannels();
        if (channels.size() == 0) {
            log.warn("All clients has disConnected from " + ch.getLocalAddress() + ". You can graceful shutdown now.");
        }
        super.disConnected(ch);
    }

}
