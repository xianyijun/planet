package cn.xianyijun.planet.remoting.api.transport.dispatcher;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.ExecutionException;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.exchange.Request;
import cn.xianyijun.planet.remoting.api.exchange.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * The type All channel handler.
 * @author xianyijun
 */
@Slf4j
public class AllChannelHandler extends WrappedChannelHandler {
    /**
     * Instantiates a new All channel handler.
     *
     * @param handler the handler
     * @param url     the url
     */
    AllChannelHandler(ChannelHandler handler, URL url) {
        super(handler, url);
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        log.info("[AllChannelHandler] connected, channel : {}", channel);
        ExecutorService cExecutor = getExecutorService();
        try {
            cExecutor.execute(new ChannelEventRunnable(channel, handler, ChannelEventRunnable.ChannelState.CONNECTED));
        } catch (Throwable t) {
            throw new ExecutionException("connect event", channel, getClass() + " error when process connected event .", t);
        }
    }

    @Override
    public void disConnected(Channel channel) throws RemotingException {
        log.info("[AllChannelHandler] disConnected, channel : {}", channel);
        ExecutorService cExecutor = getExecutorService();
        try {
            cExecutor.execute(new ChannelEventRunnable(channel, handler, ChannelEventRunnable.ChannelState.DISCONNECTED));
        } catch (Throwable t) {
            throw new ExecutionException("disconnect event", channel, getClass() + " error when process disconnected event .", t);
        }
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        log.info("[AllChannelHandler] received, channel : {} , message: {}", channel, message);
        ExecutorService cExecutor = getExecutorService();
        try {
            cExecutor.execute(new ChannelEventRunnable(channel, handler, ChannelEventRunnable.ChannelState.RECEIVED, message));
        } catch (Throwable t) {
            if(message instanceof Request && t instanceof RejectedExecutionException){
                Request request = (Request)message;
                if(request.isTwoWay()){
                    String msg = "Server side(" + url.getIp() + "," + url.getPort() + ") threadpool is exhausted ,detail msg:" + t.getMessage();
                    Response response = new Response(request.getId(), request.getVersion());
                    response.setStatus(Response.SERVER_THREAD_POOL_EXHAUSTED_ERROR);
                    response.setErrorMessage(msg);
                    channel.send(response);
                    return;
                }
            }
            throw new ExecutionException(message, channel, getClass() + " error when process received event .", t);
        }
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        log.info("[AllChannelHandler] caught, channel : {}", channel);
        ExecutorService cExecutor = getExecutorService();
        try {
            cExecutor.execute(new ChannelEventRunnable(channel, handler, ChannelEventRunnable.ChannelState.CAUGHT, exception));
        } catch (Throwable t) {
            throw new ExecutionException("caught event", channel, getClass() + " error when process caught event .", t);
        }
    }

    private ExecutorService getExecutorService() {
        ExecutorService cExecutor = executor;
        if (cExecutor == null || cExecutor.isShutdown()) {
            cExecutor = SHARED_EXECUTOR;
        }
        return cExecutor;
    }
}
