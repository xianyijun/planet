package cn.xianyijun.planet.remoting.api.exchange.support;

import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeChannel;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeHandler;
import cn.xianyijun.planet.remoting.api.transport.ChannelHandlerDispatcher;

/**
 * The type Exchange handler dispatcher.
 */
public class ExchangeHandlerDispatcher implements ExchangeHandler {
    private final ReplierDispatcher replierDispatcher;

    private final ChannelHandlerDispatcher handlerDispatcher;

    /**
     * Instantiates a new Exchange handler dispatcher.
     */
    public ExchangeHandlerDispatcher() {
        replierDispatcher = new ReplierDispatcher();
        handlerDispatcher = new ChannelHandlerDispatcher();
    }

    /**
     * Instantiates a new Exchange handler dispatcher.
     *
     * @param replier  the replier
     * @param handlers the handlers
     */
    public ExchangeHandlerDispatcher(Replier<?> replier, ChannelHandler... handlers) {
        replierDispatcher = new ReplierDispatcher(replier);
        handlerDispatcher = new ChannelHandlerDispatcher(handlers);
    }


    @Override
    public void connected(Channel channel) throws RemotingException {
        handlerDispatcher.connected(channel);
    }

    @Override
    public void disConnected(Channel channel) throws RemotingException {
        handlerDispatcher.disConnected(channel);
    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        handlerDispatcher.sent(channel,message);
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        handlerDispatcher.received(channel,message);
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        handlerDispatcher.caught(channel,exception);
    }

    @Override
    public Object reply(ExchangeChannel channel, Object request) throws RemotingException {
        return replierDispatcher.reply(channel, request);
    }
}
