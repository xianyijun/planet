package cn.xianyijun.planet.exception;

import cn.xianyijun.planet.remoting.api.Channel;

import java.net.InetSocketAddress;

/**
 *
 * @author xianyijun
 * @date 2017/10/22
 */
public class RemotingException extends Exception{
    private InetSocketAddress localAddress;

    private InetSocketAddress remoteAddress;

    /**
     * Instantiates a new Remoting exception.
     *
     * @param channel the channel
     * @param msg     the msg
     */
    public RemotingException(Channel channel, String msg) {
        this(channel == null ? null : channel.getLocalAddress(), channel == null ? null : channel.getRemoteAddress(),
                msg);
    }

    /**
     * Instantiates a new Remoting exception.
     *
     * @param localAddress  the local address
     * @param remoteAddress the remote address
     * @param message       the message
     */
    public RemotingException(InetSocketAddress localAddress, InetSocketAddress remoteAddress, String message) {
        super(message);

        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    /**
     * Instantiates a new Remoting exception.
     *
     * @param channel the channel
     * @param cause   the cause
     */
    public RemotingException(Channel channel, Throwable cause) {
        this(channel == null ? null : channel.getLocalAddress(), channel == null ? null : channel.getRemoteAddress(),
                cause);
    }

    /**
     * Instantiates a new Remoting exception.
     *
     * @param localAddress  the local address
     * @param remoteAddress the remote address
     * @param cause         the cause
     */
    public RemotingException(InetSocketAddress localAddress, InetSocketAddress remoteAddress, Throwable cause) {
        super(cause);

        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    /**
     * Instantiates a new Remoting exception.
     *
     * @param channel the channel
     * @param message the message
     * @param cause   the cause
     */
    public RemotingException(Channel channel, String message, Throwable cause) {
        this(channel == null ? null : channel.getLocalAddress(), channel == null ? null : channel.getRemoteAddress(),
                message, cause);
    }

    /**
     * Instantiates a new Remoting exception.
     *
     * @param localAddress  the local address
     * @param remoteAddress the remote address
     * @param message       the message
     * @param cause         the cause
     */
    public RemotingException(InetSocketAddress localAddress, InetSocketAddress remoteAddress, String message,
                             Throwable cause) {
        super(message, cause);

        this.localAddress = localAddress;
        this.remoteAddress = remoteAddress;
    }

    /**
     * Gets local address.
     *
     * @return the local address
     */
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    /**
     * Gets remote address.
     *
     * @return the remote address
     */
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
}