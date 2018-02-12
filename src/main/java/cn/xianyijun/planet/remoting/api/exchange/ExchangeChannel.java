package cn.xianyijun.planet.remoting.api.exchange;

import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;

/**
 * The interface Exchange channel.
 */
public interface ExchangeChannel extends Channel {

    /**
     * Request response future.
     *
     * @param request the request
     * @return the response future
     * @throws RemotingException the remoting exception
     */
    ResponseFuture request(Object request) throws RemotingException;

    /**
     * Request response future.
     *
     * @param request the request
     * @param timeout the timeout
     * @return the response future
     * @throws RemotingException the remoting exception
     */
    ResponseFuture request(Object request, int timeout) throws RemotingException;

    /**
     * Gets exchange handler.
     *
     * @return the exchange handler
     */
    ExchangeHandler getExchangeHandler();

    @Override
    void close(int timeout);
}