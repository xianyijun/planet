package cn.xianyijun.planet.remoting.api.exchange.support.header;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.Version;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.Server;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeChannel;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeServer;
import cn.xianyijun.planet.remoting.api.exchange.Request;
import cn.xianyijun.planet.remoting.api.exchange.support.DefaultFuture;
import cn.xianyijun.planet.utils.NamedThreadFactory;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * The type Header exchange server.
 */
@Getter
@Slf4j
@ToString
@EqualsAndHashCode
public class HeaderExchangeServer implements ExchangeServer {

    private final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1,
            new NamedThreadFactory(
                    "rpc-remoting-server-heartbeat",
                    true));
    private final Server server;
    // 心跳定时器
    private ScheduledFuture<?> heatbeatTimer;
    // 心跳超时，毫秒。缺省0，不会执行心跳。
    private int heartbeat;
    private int heartbeatTimeout;
    private AtomicBoolean closed = new AtomicBoolean(false);

    /**
     * Instantiates a new Header exchange server.
     *
     * @param server the server
     */
    public HeaderExchangeServer(Server server) {
        if (server == null) {
            throw new IllegalArgumentException("server can not be null");
        }
        this.server = server;
        this.heartbeat = server.getUrl().getParameter(Constants.HEARTBEAT_KEY, 0);
        this.heartbeatTimeout = server.getUrl().getParameter(Constants.HEARTBEAT_TIMEOUT_KEY, heartbeat * 3);
        if (heartbeatTimeout < heartbeat * 2) {
            throw new IllegalStateException("heartbeatTimeout < heartbeatInterval * 2");
        }
        startHeartbeatTimer();
    }


    private void startHeartbeatTimer() {
        stopHeartbeatTimer();
        if (heartbeat > 0) {
            heatbeatTimer = scheduled.scheduleWithFixedDelay(
                    new HeartBeatTask(() -> Collections.unmodifiableCollection(
                            HeaderExchangeServer.this.getChannels()), heartbeat, heartbeatTimeout),
                    heartbeat, heartbeat, TimeUnit.MILLISECONDS);
        }
    }

    private void stopHeartbeatTimer() {
        try {
            ScheduledFuture<?> timer = heatbeatTimer;
            if (timer != null && !timer.isCancelled()) {
                timer.cancel(true);
            }
        } catch (Throwable t) {
            log.warn(t.getMessage(), t);
        } finally {
            heatbeatTimer = null;
        }
    }

    @Override
    public void reset(URL url) {
        server.reset(url);
    }

    @Override
    public URL getUrl() {
        return server.getUrl();
    }

    @Override
    public boolean isBound() {
        return server.isBound();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return server.getChannelHandler();
    }

    @Override
    public Collection<ExchangeChannel> getExchangeChannels() {
        Collection<ExchangeChannel> exchangeChannels = new ArrayList<>();
        Collection<Channel> channels = server.getChannels();
        if (channels != null && channels.size() > 0) {
            for (Channel channel : channels) {
                exchangeChannels.add(HeaderExchangeChannel.getOrAddChannel(channel));
            }
        }
        return exchangeChannels;
    }

    @Override
    public Collection<Channel> getChannels() {
        return (Collection)getExchangeChannels();
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        return getExchangeChannel(remoteAddress);
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return server.getLocalAddress();
    }

    @Override
    public ExchangeChannel getExchangeChannel(InetSocketAddress remoteAddress) {
        Channel channel = server.getChannel(remoteAddress);
        return HeaderExchangeChannel.getOrAddChannel(channel);
    }

    @Override
    public void send(Object message) throws RemotingException {
        server.send(message);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        server.send(message,sent);
    }

    @Override
    public void close() {
        doClose();
        server.close();
    }

    @Override
    public void close(int timeout) {
        startClose();
        if (timeout > 0) {
            final long max = (long) timeout;
            final long start = System.currentTimeMillis();
            if (getUrl().getParameter(Constants.CHANNEL_SEND_READONLYEVENT_KEY, true)) {
                sendChannelReadOnlyEvent();
            }
            while (HeaderExchangeServer.this.isRunning()
                    && System.currentTimeMillis() - start < max) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
        doClose();
        server.close(timeout);
    }


    private void sendChannelReadOnlyEvent() {
        Request request = new Request();
        request.setEvent(Request.READONLY_EVENT);
        request.setTwoWay(false);
        request.setVersion(Version.getVersion());

        Collection<Channel> channels = getChannels();
        for (Channel channel : channels) {
            try {
                if (channel.isConnected()) {
                    channel.send(request, getUrl().getParameter(Constants.CHANNEL_READONLY_EVENT_SENT_KEY, true));
                }
            } catch (RemotingException e) {
                log.warn("send connot write messge error.", e);
            }
        }
    }

    private void doClose() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        stopHeartbeatTimer();
        try {
            scheduled.shutdown();
        } catch (Throwable t) {
            log.warn(t.getMessage(), t);
        }
    }

    @Override
    public void startClose() {
        server.startClose();
    }

    @Override
    public boolean isClosed() {
        return server.isClosed();
    }

    private boolean isRunning() {
        Collection<Channel> channels = getChannels();
        for (Channel channel : channels) {
            if (DefaultFuture.hasFuture(channel)) {
                return true;
            }
        }
        return false;
    }
}
