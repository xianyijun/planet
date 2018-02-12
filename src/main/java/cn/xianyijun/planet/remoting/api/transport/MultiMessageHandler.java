package cn.xianyijun.planet.remoting.api.transport;

import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.exchange.support.MultiMessage;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Multi message handler.
 * @author xianyijun
 */
@Slf4j
@ToString
public class MultiMessageHandler extends AbstractChannelHandlerDelegate {

    /**
     * Instantiates a new Multi message handler.
     *
     * @param handler the handler
     */
    public MultiMessageHandler(ChannelHandler handler) {
        super(handler);
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        log.info("[MultiMessageHandler] received channel : {} ,message: {}",channel ,message);
        if (message instanceof MultiMessage) {
            MultiMessage list = (MultiMessage) message;
            for (Object obj : list) {
                handler.received(channel, obj);
            }
        } else {
            handler.received(channel, message);
        }
    }
}
