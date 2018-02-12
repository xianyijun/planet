package cn.xianyijun.planet.remoting.api.transport;

import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Abstract channel handler delegate.
 */
@Slf4j
public class AbstractChannelHandlerDelegate implements ChannelHandlerDelegate {
    /**
     * The Handler.
     */
    protected ChannelHandler handler;

    /**
     * Instantiates a new Abstract channel handler delegate.
     *
     * @param handler the handler
     */
    protected AbstractChannelHandlerDelegate(ChannelHandler handler) {
        this.handler = handler;
    }

    @Override
    public ChannelHandler getHandler() {
        if (handler instanceof ChannelHandlerDelegate) {
            return ((ChannelHandlerDelegate) handler).getHandler();
        }
        return handler;
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        log.info("[AbstractChannelHandlerDelegate] connected , channel : {} ,handler :{} ",channel, handler);
        handler.connected(channel);
    }

    @Override
    public void disConnected(Channel channel) throws RemotingException {
        log.info("[AbstractChannelHandlerDelegate] disConnected channel :{} ",channel);
        handler.disConnected(channel);
    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        log.info("[AbstractChannelHandlerDelegate] send channel :{} msg: {}",channel,message);
        handler.sent(channel, message);
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        log.info("[AbstractChannelHandlerDelegate] send channel :{} msg: {}",channel,message);
        handler.received(channel, message);
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        log.info("[AbstractChannelHandlerDelegate] caught channel :{} ex: {}",channel,exception);
        handler.caught(channel, exception);
    }
}
