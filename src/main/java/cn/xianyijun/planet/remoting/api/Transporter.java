package cn.xianyijun.planet.remoting.api;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.SPI;
import cn.xianyijun.planet.exception.RemotingException;

/**
 * The interface Transporter.
 */
@SPI("netty")
public interface Transporter {

    /**
     * Bind server.
     *
     * @param url     the url
     * @param handler the handler
     * @return the server
     * @throws RemotingException the remoting exception
     */
    Server bind(URL url, ChannelHandler handler) throws RemotingException;

    /**
     * Connect client.
     *
     * @param url     the url
     * @param handler the handler
     * @return the client
     * @throws RemotingException the remoting exception
     */
    Client connect(URL url, ChannelHandler handler) throws RemotingException;
}
