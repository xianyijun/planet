package cn.xianyijun.planet.remoting.api.exchange.support.header;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.ExecutionException;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeChannel;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeHandler;
import cn.xianyijun.planet.remoting.api.exchange.Request;
import cn.xianyijun.planet.remoting.api.exchange.Response;
import cn.xianyijun.planet.remoting.api.exchange.support.DefaultFuture;
import cn.xianyijun.planet.remoting.api.transport.ChannelHandlerDelegate;
import cn.xianyijun.planet.utils.NetUtils;
import cn.xianyijun.planet.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;


/**
 * The type Header exchange handler.
 *
 * @author xianyijun
 */
@Slf4j
@RequiredArgsConstructor
public class HeaderExchangeHandler implements ChannelHandlerDelegate {
    /**
     * The constant KEY_READ_TIMESTAMP.
     */
    public static String KEY_READ_TIMESTAMP = HeartbeatHandler.KEY_READ_TIMESTAMP;

    /**
     * The constant KEY_WRITE_TIMESTAMP.
     */
    public static String KEY_WRITE_TIMESTAMP = HeartbeatHandler.KEY_WRITE_TIMESTAMP;

    private final ExchangeHandler handler;


    @Override
    public ChannelHandler getHandler() {
        if (handler instanceof ChannelHandlerDelegate) {
            return ((ChannelHandlerDelegate) handler).getHandler();
        } else {
            return handler;
        }
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        log.info("[HeaderExchangeHandler] channel:{} ,handler :{} ", channel , handler);
        channel.setAttribute(KEY_READ_TIMESTAMP, System.currentTimeMillis());
        channel.setAttribute(KEY_WRITE_TIMESTAMP, System.currentTimeMillis());
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            handler.connected(exchangeChannel);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public void disConnected(Channel channel) throws RemotingException {
        channel.setAttribute(KEY_READ_TIMESTAMP, System.currentTimeMillis());
        channel.setAttribute(KEY_WRITE_TIMESTAMP, System.currentTimeMillis());
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            handler.disConnected(exchangeChannel);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        Throwable exception = null;
        try {
            channel.setAttribute(KEY_WRITE_TIMESTAMP, System.currentTimeMillis());
            ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
            try {
                handler.sent(exchangeChannel, message);
            } finally {
                HeaderExchangeChannel.removeChannelIfDisconnected(channel);
            }
        } catch (Throwable t) {
            exception = t;
        }
        if (message instanceof Request) {
            Request request = (Request) message;
            DefaultFuture.sent(channel, request);
        }
        if (exception != null) {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException) exception;
            } else if (exception instanceof RemotingException) {
                throw (RemotingException) exception;
            } else {
                throw new RemotingException(channel.getLocalAddress(), channel.getRemoteAddress(),
                        exception.getMessage(), exception);
            }
        }
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        log.info("[HeaderExchangeHandler] received channel : {} , message :{}", channel, message);
        channel.setAttribute(KEY_READ_TIMESTAMP, System.currentTimeMillis());
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);

        try {
            if (message instanceof Request){
                Request request = (Request) message;
                if (request.isEvent()){
                    handlerEvent(channel, request);
                }else {
                    if (request.isTwoWay()){
                        Response response = handlerRequest(exchangeChannel, request);
                        channel.send(response);
                    }else {
                        handler.received(exchangeChannel, request.getData());
                    }
                }
            }else if (message instanceof Response){
                handlerResponse(channel,(Response)message);
            }else if (message instanceof String){
                if (isClientSide(channel)){
                    Exception e = new Exception("rpc client can not supported string message: " + message + " in channel: " + channel + ", url: " + channel.getUrl());
                    log.error(e.getMessage(), e);
                }
            }else {
                handler.received(exchangeChannel, message);
            }
        }finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    private static boolean isClientSide(Channel channel) {
        InetSocketAddress address = channel.getRemoteAddress();
        URL url = channel.getUrl();
        return url.getPort() == address.getPort() && NetUtils.filterLocalHost(url.getIp()).equals(NetUtils.filterLocalHost(address.getAddress().getHostAddress()));
    }

    private void handlerResponse(Channel channel, Response response) {
        log.info("[HeaderExchangeHandler] handlerResponse  channel : {} , response : {} ", channel, response);
        if (response != null &&  !response.isHeartbeat()) {
            DefaultFuture.received(channel, response);
        }
    }

    private Response handlerRequest(ExchangeChannel channel, Request request) {
        Response res = new Response(request.getId(), request.getVersion());
        if (request.isBroken()) {
            Object data = request.getData();
            String msg;
            if (data == null) {
                msg = null;
            } else if (data instanceof Throwable) {
                msg = StringUtils.toString((Throwable) data);
            } else {
                msg = data.toString();
            }
            res.setErrorMessage("Fail to decode request due to: " + msg);
            res.setStatus(Response.BAD_REQUEST);
            return res;
        }
        Object msg = request.getData();
        try {
            Object result = handler.reply(channel, msg);
            res.setStatus(Response.OK);
            res.setResult(result);
        } catch (Throwable e) {
            res.setStatus(Response.SERVICE_ERROR);
            res.setErrorMessage(StringUtils.toString(e));
        }
        return res;
    }

    private void handlerEvent(Channel channel, Request request) {
        if (request.getData() != null && request.getData().equals(Request.READONLY_EVENT)) {
            channel.setAttribute(Constants.CHANNEL_ATTRIBUTE_READONLY_KEY, Boolean.TRUE);
        }
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        if (exception instanceof ExecutionException) {
            ExecutionException e = (ExecutionException) exception;
            Object msg = e.getRequest();
            if (msg instanceof Request) {
                Request req = (Request) msg;
                if (req.isTwoWay() && !req.isHeartbeat()) {
                    Response res = new Response(req.getId(), req.getVersion());
                    res.setStatus(Response.SERVER_ERROR);
                    res.setErrorMessage(StringUtils.toString(e));
                    channel.send(res);
                    return;
                }
            }
        }
        ExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            handler.caught(exchangeChannel, exception);
        } finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }
}
