package cn.xianyijun.planet.remoting.api.transport;

import cn.xianyijun.planet.remoting.api.ChannelHandler;

/**
 * Created by xianyijun on 2017/10/28.
 */
public interface ChannelHandlerDelegate extends ChannelHandler {
    /**
     * Gets handler.
     *
     * @return the handler
     */
    ChannelHandler getHandler();
}
