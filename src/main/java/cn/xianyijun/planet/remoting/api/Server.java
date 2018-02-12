package cn.xianyijun.planet.remoting.api;


import cn.xianyijun.planet.common.ReSetable;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * The interface Server.
 *
 * @author xianyijun
 */
public interface Server extends Endpoint, ReSetable {
    /**
     * Is bound boolean.
     *
     * @return the boolean
     */
    boolean isBound();

    /**
     * Gets channels.
     *
     * @return the channels
     */
    Collection<Channel> getChannels();

    /**
     * Gets channel.
     *
     * @param remoteAddress the remote address
     * @return the channel
     */
    Channel getChannel(InetSocketAddress remoteAddress);

}
