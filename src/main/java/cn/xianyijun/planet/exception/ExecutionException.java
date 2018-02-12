package cn.xianyijun.planet.exception;

import cn.xianyijun.planet.remoting.api.Channel;

import java.net.InetSocketAddress;

/**
 * The type Execution exception.
 */
public class ExecutionException extends RemotingException {
    private final Object request;

    /**
     * Instantiates a new Execution exception.
     *
     * @param request the request
     * @param channel the channel
     * @param message the message
     * @param cause   the cause
     */
    public ExecutionException(Object request, Channel channel, String message, Throwable cause) {
        super(channel, message, cause);
        this.request = request;
    }

    /**
     * Instantiates a new Execution exception.
     *
     * @param request the request
     * @param channel the channel
     * @param msg     the msg
     */
    public ExecutionException(Object request, Channel channel, String msg) {
        super(channel, msg);
        this.request = request;
    }

    /**
     * Instantiates a new Execution exception.
     *
     * @param request the request
     * @param channel the channel
     * @param cause   the cause
     */
    public ExecutionException(Object request, Channel channel, Throwable cause) {
        super(channel, cause);
        this.request = request;
    }

    /**
     * Instantiates a new Execution exception.
     *
     * @param request       the request
     * @param localAddress  the local address
     * @param remoteAddress the remote address
     * @param message       the message
     * @param cause         the cause
     */
    public ExecutionException(Object request, InetSocketAddress localAddress, InetSocketAddress remoteAddress, String message,
                              Throwable cause) {
        super(localAddress, remoteAddress, message, cause);
        this.request = request;
    }

    /**
     * Instantiates a new Execution exception.
     *
     * @param request       the request
     * @param localAddress  the local address
     * @param remoteAddress the remote address
     * @param message       the message
     */
    public ExecutionException(Object request, InetSocketAddress localAddress, InetSocketAddress remoteAddress, String message) {
        super(localAddress, remoteAddress, message);
        this.request = request;
    }

    /**
     * Instantiates a new Execution exception.
     *
     * @param request       the request
     * @param localAddress  the local address
     * @param remoteAddress the remote address
     * @param cause         the cause
     */
    public ExecutionException(Object request, InetSocketAddress localAddress, InetSocketAddress remoteAddress, Throwable cause) {
        super(localAddress, remoteAddress, cause);
        this.request = request;
    }


    /**
     * Gets request.
     *
     * @return the request
     */
    public Object getRequest() {
        return request;
    }
}
