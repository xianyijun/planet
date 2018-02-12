package cn.xianyijun.planet.remoting.api.exchange.support.header;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.Client;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeChannel;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeClient;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeHandler;
import cn.xianyijun.planet.remoting.api.exchange.ResponseFuture;
import cn.xianyijun.planet.utils.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The type Header exchange client.
 */
@Slf4j
public class HeaderExchangeClient implements ExchangeClient {

    private static final ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("rpc-remoting-client-heartbeat", true));
    private final Client client;
    private final ExchangeChannel channel;
    private ScheduledFuture<?> heatbeatTimer;
    private int heartbeat;
    private int heartbeatTimeout;

    /**
     * Instantiates a new Header exchange client.
     *
     * @param client        the client
     * @param needHeartbeat the need heartbeat
     */
    public HeaderExchangeClient(Client client, boolean needHeartbeat) {
        if (client == null) {
            throw new IllegalArgumentException("client can not be null");
        }
        this.client = client;
        this.channel = new HeaderExchangeChannel(client);
        String rpcVersion = client.getUrl().getParameter(Constants.RPC_VERSION_KEY);
        this.heartbeat = client.getUrl().getParameter(Constants.HEARTBEAT_KEY, rpcVersion != null && rpcVersion.startsWith("1.0.") ? Constants.DEFAULT_HEARTBEAT : 0);
        this.heartbeatTimeout = client.getUrl().getParameter(Constants.HEARTBEAT_TIMEOUT_KEY, heartbeat * 3);
        if (heartbeatTimeout < heartbeat * 2) {
            throw new IllegalStateException("heartbeatTimeout < heartbeatInterval * 2");
        }
        if (needHeartbeat) {
            startHeartbeatTimer();
        }
    }

    @Override
    public void reConnect() throws RemotingException {
        client.reConnect();
    }

    @Override
    public void close() {
        doClose();
        channel.close();
    }

    @Override
    public void close(int timeout) {
        startClose();
        doClose();
        channel.close(timeout);
    }

    private void doClose() {
        stopHeartbeatTimer();
    }

    private void startHeartbeatTimer() {
        stopHeartbeatTimer();
        if (heartbeat > 0) {
            heatbeatTimer = scheduled.scheduleWithFixedDelay(
                    new HeartBeatTask(() -> Collections.singletonList(HeaderExchangeClient.this), heartbeat, heartbeatTimeout),
                    heartbeat, heartbeat, TimeUnit.MILLISECONDS);
        }
    }

    private void stopHeartbeatTimer() {
        if (heatbeatTimer != null && !heatbeatTimer.isCancelled()) {
            try {
                heatbeatTimer.cancel(true);
                scheduled.purge();
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
        heatbeatTimer = null;
    }

    @Override
    public void reset(URL url) {
        client.reset(url);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return channel.getRemoteAddress();
    }

    @Override
    public boolean isConnected() {
        return channel.isConnected();
    }

    @Override
    public boolean hasAttribute(String key) {
        return channel.hasAttribute(key);
    }

    @Override
    public URL getUrl() {
        return channel.getUrl();
    }

    @Override
    public ResponseFuture request(Object request) throws RemotingException {
        return channel.request(request);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return channel.getChannelHandler();
    }

    @Override
    public Object getAttribute(String key) {
        return channel.getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        channel.setAttribute(key,value);
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return channel.getLocalAddress();
    }

    @Override
    public ResponseFuture request(Object request, int timeout) throws RemotingException {
        return channel.request(request,timeout);
    }

    @Override
    public void send(Object message) throws RemotingException {
        channel.send(message);
    }

    @Override
    public void removeAttribute(String key) {
        channel.removeAttribute(key);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        channel.send(message,sent);
    }

    @Override
    public ExchangeHandler getExchangeHandler() {
        return channel.getExchangeHandler();
    }

    @Override
    public void startClose() {
        channel.startClose();
    }

    @Override
    public boolean isClosed() {
        return channel.isClosed();
    }
}
