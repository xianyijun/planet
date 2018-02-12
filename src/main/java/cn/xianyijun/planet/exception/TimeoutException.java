package cn.xianyijun.planet.exception;

import cn.xianyijun.planet.remoting.api.Channel;

import java.net.InetSocketAddress;

/**
 * Created by xianyijun on 2017/10/28.
 */
public class TimeoutException extends RemotingException {

    /**
     * The constant CLIENT_SIDE.
     */
    public static final int CLIENT_SIDE = 0;
    /**
     * The constant SERVER_SIDE.
     */
    public static final int SERVER_SIDE = 1;
    private static final long serialVersionUID = 3122966731958222692L;
    private final int phase;

    /**
     * Instantiates a new Timeout exception.
     *
     * @param serverSide the server side
     * @param channel    the channel
     * @param message    the message
     */
    public TimeoutException(boolean serverSide, Channel channel, String message) {
        super(channel, message);
        this.phase = serverSide ? SERVER_SIDE : CLIENT_SIDE;
    }

    /**
     * Instantiates a new Timeout exception.
     *
     * @param serverSide    the server side
     * @param localAddress  the local address
     * @param remoteAddress the remote address
     * @param message       the message
     */
    public TimeoutException(boolean serverSide, InetSocketAddress localAddress,
                            InetSocketAddress remoteAddress, String message) {
        super(localAddress, remoteAddress, message);
        this.phase = serverSide ? SERVER_SIDE : CLIENT_SIDE;
    }

    /**
     * Gets phase.
     *
     * @return the phase
     */
    public int getPhase() {
        return phase;
    }

    /**
     * Is server side boolean.
     *
     * @return the boolean
     */
    public boolean isServerSide() {
        return phase == 1;
    }

    /**
     * Is client side boolean.
     *
     * @return the boolean
     */
    public boolean isClientSide() {
        return phase == 0;
    }
}
