package cn.xianyijun.planet.remoting.api.transport;

import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.Decodeable;
import cn.xianyijun.planet.remoting.api.exchange.Request;
import cn.xianyijun.planet.remoting.api.exchange.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Decode handler.
 * @author xianyijun
 */
@Slf4j
public class DecodeHandler extends AbstractChannelHandlerDelegate {
    /**
     * Instantiates a new Decode handler.
     *
     * @param handler the handler
     */
    public DecodeHandler(ChannelHandler handler) {
        super(handler);
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        if (message instanceof Decodeable) {
            decode(message);
        }

        if (message instanceof Request) {
            decode(((Request) message).getData());
        }

        if (message instanceof Response) {
            decode(((Response) message).getResult());
        }

        handler.received(channel, message);
    }

    private void decode(Object message) {
        if (message != null && message instanceof Decodeable) {
            try {
                ((Decodeable) message).decode();
                if (log.isDebugEnabled()) {
                    log.debug("Decode decodeable message " +
                            message.getClass().getName());
                }
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn(
                            new StringBuilder(32)
                                    .append("Call Decodeable.decode failed: ")
                                    .append(e.getMessage()).toString(),
                            e);
                }
            }
        }
    }
}
