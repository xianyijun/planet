package cn.xianyijun.planet.remoting.api.transport.dispatcher;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.common.store.DataStore;
import cn.xianyijun.planet.common.threadpool.LimitedThreadPool;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.transport.ChannelHandlerDelegate;
import cn.xianyijun.planet.utils.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

;

/**
 * The type Wrapped channel handler.
 */
@Slf4j
public class WrappedChannelHandler implements ChannelHandlerDelegate {


    /**
     * The constant SHARED_EXECUTOR.
     */
    protected static final ExecutorService SHARED_EXECUTOR = Executors.newCachedThreadPool(new NamedThreadFactory("RpcSharedHandler", true));

    /**
     * The Executor.
     */
    protected final ExecutorService executor;

    /**
     * The Handler.
     */
    protected final ChannelHandler handler;

    /**
     * The Url.
     */
    protected final URL url;

    /**
     * Instantiates a new Wrapped channel handler.
     *
     * @param handler the handler
     * @param url     the url
     */
    public WrappedChannelHandler(ChannelHandler handler, URL url) {
        this.handler = handler;
        this.url = url;
        executor = (ExecutorService)new LimitedThreadPool().getExecutor(url);

        String componentKey = Constants.EXECUTOR_SERVICE_COMPONENT_KEY;
        if (Constants.CONSUMER_SIDE.equalsIgnoreCase(url.getParameter(Constants.SIDE_KEY))) {
            componentKey = Constants.CONSUMER_SIDE;
        }
        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
        dataStore.put(componentKey, Integer.toString(url.getPort()), executor);
    }

    /**
     * Close.
     */
    public void close() {
        try {
            if (executor != null) {
                executor.shutdown();
            }
        } catch (Throwable t) {
            log.warn("fail to destroy thread pool of server: " + t.getMessage(), t);
        }
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        handler.connected(channel);
    }

    @Override
    public void disConnected(Channel channel) throws RemotingException {
        handler.disConnected(channel);
    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        handler.sent(channel, message);
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        handler.received(channel, message);
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        handler.caught(channel, exception);
    }

    /**
     * Gets executor.
     *
     * @return the executor
     */
    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public ChannelHandler getHandler() {
        if (handler instanceof ChannelHandlerDelegate) {
            return ((ChannelHandlerDelegate) handler).getHandler();
        } else {
            return handler;
        }
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public URL getUrl() {
        return url;
    }

}
