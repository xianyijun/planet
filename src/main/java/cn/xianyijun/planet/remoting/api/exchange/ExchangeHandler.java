package cn.xianyijun.planet.remoting.api.exchange;

import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.ChannelHandler;

/**
 * The interface Exchange handler.
 */
public interface ExchangeHandler extends ChannelHandler {
    /**
     * Reply object.
     *
     * @param channel the channel
     * @param request the request
     * @return the object
     * @throws RemotingException the remoting exception
     */
    Object reply(ExchangeChannel channel, Object request) throws RemotingException;
}
