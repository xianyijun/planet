package cn.xianyijun.planet.remoting.api.transport;

import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The type Channel handler dispatcher.
 *
 * @author xianyijun
 */
@Slf4j
@NoArgsConstructor
public class ChannelHandlerDispatcher implements ChannelHandler {

    @Getter
    private final Collection<ChannelHandler> channelHandlers = new CopyOnWriteArraySet<ChannelHandler>();

    /**
     * Instantiates a new Channel handler dispatcher.
     *
     * @param handlers the handlers
     */
    public ChannelHandlerDispatcher(ChannelHandler... handlers) {
        this(handlers == null ? Collections.emptyList() :Arrays.asList(handlers));
    }

    /**
     * Instantiates a new Channel handler dispatcher.
     *
     * @param handlers the handlers
     */
    public ChannelHandlerDispatcher(Collection<ChannelHandler> handlers) {
        if (handlers != null && !handlers.isEmpty()){
            channelHandlers.addAll(handlers);
        }
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.connected(channel);
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }
    }

    @Override
    public void disConnected(Channel channel) throws RemotingException {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.disConnected(channel);
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }
    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.sent(channel, message);
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.received(channel, message);
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        for (ChannelHandler listener : channelHandlers) {
            try {
                listener.caught(channel, exception);
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }
    }


    /**
     * Add channel handler channel handler dispatcher.
     *
     * @param handler the handler
     * @return the channel handler dispatcher
     */
    public ChannelHandlerDispatcher addChannelHandler(ChannelHandler handler) {
        this.channelHandlers.add(handler);
        return this;
    }

    /**
     * Remove channel handler channel handler dispatcher.
     *
     * @param handler the handler
     * @return the channel handler dispatcher
     */
    public ChannelHandlerDispatcher removeChannelHandler(ChannelHandler handler) {
        this.channelHandlers.remove(handler);
        return this;
    }
}
