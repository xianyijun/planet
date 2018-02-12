package cn.xianyijun.planet.remoting.api.transport.dispatcher;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.Adaptive;
import cn.xianyijun.planet.common.extension.SPI;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
/**
 *
 * @author xianyijun
 * @date 2017/10/28
 */
@SPI(AllDispatcher.NAME)
public interface Dispatcher {
    /**
     * Dispatch channel handler.
     *
     * @param handler the handler
     * @param url     the url
     * @return the channel handler
     */
    @Adaptive({Constants.DISPATCHER_KEY, "dispather", "channel.handler"})
    ChannelHandler dispatch(ChannelHandler handler, URL url);
}
