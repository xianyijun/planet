package cn.xianyijun.planet.remoting.api;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;

import java.net.InetSocketAddress;


/**
 * The interface Endpoint.
 */
public interface Endpoint {
    /**
     * Gets url.
     *
     * @return the url
     */
    URL getUrl();

    /**
     * Gets channel handler.
     *
     * @return the channel handler
     */
    ChannelHandler getChannelHandler();

    /**
     * Gets local address.
     *
     * @return the local address
     */
    InetSocketAddress getLocalAddress();

    /**
     * Send.
     *
     * @param message the message
     * @throws RemotingException the remoting exception
     */
    void send(Object message) throws RemotingException;

    /**
     * Send.
     *
     * @param message the message
     * @param sent    the sent
     * @throws RemotingException the remoting exception
     */
    void send(Object message, boolean sent) throws RemotingException;

    /**
     * Close.
     */
    void close();

    /**
     * Close.
     *
     * @param timeout the timeout
     */
    void close(int timeout);

    /**
     * Start close.
     */
    void startClose();

    /**
     * Is closed boolean.
     *
     * @return the boolean
     */
    boolean isClosed();

}
