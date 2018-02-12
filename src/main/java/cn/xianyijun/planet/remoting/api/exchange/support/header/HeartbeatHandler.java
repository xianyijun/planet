package cn.xianyijun.planet.remoting.api.exchange.support.header;

import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.transport.AbstractChannelHandlerDelegate;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Heartbeat handler.
 */
@Slf4j
public class HeartbeatHandler extends AbstractChannelHandlerDelegate {

    /**
     * The constant KEY_READ_TIMESTAMP.
     */
    public static String KEY_READ_TIMESTAMP = "READ_TIMESTAMP";

    /**
     * The constant KEY_WRITE_TIMESTAMP.
     */
    public static String KEY_WRITE_TIMESTAMP = "WRITE_TIMESTAMP";

    /**
     * Instantiates a new Heartbeat handler.
     *
     * @param handler the handler
     */
    public HeartbeatHandler(ChannelHandler handler) {
        super(handler);
    }
}
