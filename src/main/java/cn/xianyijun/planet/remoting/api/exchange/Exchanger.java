package cn.xianyijun.planet.remoting.api.exchange;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;

/**
 * The interface Exchanger.
 */
public interface Exchanger {
    /**
     * Bind exchange server.
     *
     * @param url     the url
     * @param handler the handler
     * @return the exchange server
     * @throws RemotingException the remoting exception
     */
    ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException;

    /**
     * Connect exchange client.
     *
     * @param url     the url
     * @param handler the handler
     * @return the exchange client
     * @throws RemotingException the remoting exception
     */
    ExchangeClient connect(URL url, ExchangeHandler handler) throws RemotingException;
}
