package cn.xianyijun.planet.remoting.api.transport.dispatcher;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.remoting.api.ChannelHandler;

/**
 *
 * @author xianyijun
 * @date 2017/10/28
 */
public class AllDispatcher implements Dispatcher {

    /**
     * The constant NAME.
     */
    public static final String NAME = "all";

    @Override
    public ChannelHandler dispatch(ChannelHandler handler, URL url) {
        return new AllChannelHandler(handler, url);
    }
}
