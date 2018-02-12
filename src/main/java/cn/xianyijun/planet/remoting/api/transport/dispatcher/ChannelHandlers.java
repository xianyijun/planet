package cn.xianyijun.planet.remoting.api.transport.dispatcher;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.exchange.support.header.HeartbeatHandler;
import cn.xianyijun.planet.remoting.api.transport.MultiMessageHandler;

/**
 * @author xianyijun
 */
public class ChannelHandlers {
    private static ChannelHandlers INSTANCE = new ChannelHandlers();

    /**
     * Instantiates a new Channel handlers.
     */
    protected ChannelHandlers() {
    }

    /**
     * Wrap channel handler.
     *
     * @param handler the handler
     * @param url     the url
     * @return the channel handler
     */
    public static ChannelHandler wrap(ChannelHandler handler, URL url) {
        return ChannelHandlers.getInstance().wrapInternal(handler, url);
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    protected static ChannelHandlers getInstance() {
        return INSTANCE;
    }

    /**
     * Sets testing channel handlers.
     *
     * @param instance the instance
     */
    static void setTestingChannelHandlers(ChannelHandlers instance) {
        INSTANCE = instance;
    }

    /**
     * Wrap internal channel handler.
     *
     * @param handler the handler
     * @param url     the url
     * @return the channel handler
     */
    protected ChannelHandler wrapInternal(ChannelHandler handler, URL url) {
        return new MultiMessageHandler(new HeartbeatHandler(ExtensionLoader.getExtensionLoader(Dispatcher.class)
                .getAdaptiveExtension().dispatch(handler, url)));
    }
}
