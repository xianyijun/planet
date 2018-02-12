package cn.xianyijun.planet.remoting.api;

import cn.xianyijun.planet.exception.RemotingException;

/**
 * The interface Channel handler.
 *
 * @author xianyijun
 */
public interface ChannelHandler {

    /**
     * Connected.
     *
     * @param channel the channel
     * @throws RemotingException the remoting exception
     */
    void connected(Channel channel) throws RemotingException;

    /**
     * Dis connected.
     *
     * @param channel the channel
     * @throws RemotingException the remoting exception
     */
    void disConnected(Channel channel) throws RemotingException;

    /**
     * Sent.
     *
     * @param channel the channel
     * @param message the message
     * @throws RemotingException the remoting exception
     */
    void sent(Channel channel, Object message) throws RemotingException;

    /**
     * Received.
     *
     * @param channel the channel
     * @param message the message
     * @throws RemotingException the remoting exception
     */
    void received(Channel channel, Object message) throws RemotingException;

    /**
     * Caught.
     *
     * @param channel   the channel
     * @param exception the exception
     * @throws RemotingException the remoting exception
     */
    void caught(Channel channel, Throwable exception) throws RemotingException;
}