package cn.xianyijun.planet.remoting.api.exchange;

import cn.xianyijun.planet.remoting.api.Server;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * The interface Exchange server.
 * @author xianyijun
 */
public interface ExchangeServer extends Server {
    /**
     * Gets exchange channels.
     *
     * @return the exchange channels
     */
    Collection<ExchangeChannel> getExchangeChannels();

    /**
     * Gets exchange channel.
     *
     * @param remoteAddress the remote address
     * @return the exchange channel
     */
    ExchangeChannel getExchangeChannel(InetSocketAddress remoteAddress);

}
