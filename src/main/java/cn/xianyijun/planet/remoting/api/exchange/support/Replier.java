package cn.xianyijun.planet.remoting.api.exchange.support;

import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeChannel;

/**
 * The interface Replier.
 *
 * @param <T> the type parameter
 * @author xianyijun
 * @date 2017 /10/28
 */
public interface Replier<T> {
    /**
     * Reply object.
     *
     * @param channel the channel
     * @param request the request
     * @return the object
     * @throws RemotingException the remoting exception
     */
    Object reply(ExchangeChannel channel, T request) throws RemotingException;
}
