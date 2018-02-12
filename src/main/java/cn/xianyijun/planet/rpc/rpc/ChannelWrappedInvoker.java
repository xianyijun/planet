package cn.xianyijun.planet.rpc.rpc;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.exception.TimeoutException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeClient;
import cn.xianyijun.planet.remoting.api.exchange.support.header.HeaderExchangeClient;
import cn.xianyijun.planet.remoting.api.transport.ClientDelegate;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Result;
import cn.xianyijun.planet.rpc.api.RpcInvocation;
import cn.xianyijun.planet.rpc.api.RpcResult;
import cn.xianyijun.planet.rpc.api.protocol.AbstractInvoker;

import java.net.InetSocketAddress;

/**
 * @author xianyijun
 */
public class ChannelWrappedInvoker<T> extends AbstractInvoker<T> {

    private final Channel channel;
    private final String serviceKey;
    private final ExchangeClient currentClient;

    ChannelWrappedInvoker(Class<T> serviceType, Channel channel, URL url, String serviceKey) {
        super(serviceType, url, new String[]{Constants.GROUP_KEY, Constants.TOKEN_KEY, Constants.TIMEOUT_KEY});
        this.channel = channel;
        this.serviceKey = serviceKey;
        this.currentClient = new HeaderExchangeClient(new ChannelWrapper(this.channel), false);
    }

    @Override
    protected Result doInvoke(Invocation invocation) throws Throwable {
        RpcInvocation inv = (RpcInvocation) invocation;
        inv.setAttachment(Constants.PATH_KEY, getInterface().getName());
        inv.setAttachment(Constants.CALLBACK_SERVICE_KEY, serviceKey);

        try {
            if (getUrl().getMethodParameter(invocation.getMethodName(), Constants.ASYNC_KEY, false)) {
                currentClient.send(inv, getUrl().getMethodParameter(invocation.getMethodName(), Constants.SENT_KEY, false));
                return new RpcResult();
            }
            int timeout = getUrl().getMethodParameter(invocation.getMethodName(), Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
            if (timeout > 0) {
                return (Result) currentClient.request(inv, timeout).get();
            } else {
                return (Result) currentClient.request(inv).get();
            }
        } catch (RpcException e) {
            throw e;
        } catch (TimeoutException e) {
            throw new RpcException(RpcException.TIMEOUT_EXCEPTION, e.getMessage(), e);
        } catch (RemotingException e) {
            throw new RpcException(RpcException.NETWORK_EXCEPTION, e.getMessage(), e);
        } catch (Throwable e) {
            throw new RpcException(e.getMessage(), e);
        }
    }

    public static class ChannelWrapper extends ClientDelegate {

        private final Channel channel;
        private final URL url;

        ChannelWrapper(Channel channel) {
            this.channel = channel;
            this.url = channel.getUrl().addParameter("codec", RpcCodec.NAME);
        }

        @Override
        public URL getUrl() {
            return url;
        }

        @Override
        public ChannelHandler getChannelHandler() {
            return channel.getChannelHandler();
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return channel.getLocalAddress();
        }

        @Override
        public void close() {
            channel.close();
        }

        @Override
        public boolean isClosed() {
            return channel == null || channel.isClosed();
        }

        @Override
        public void reset(URL url) {
            throw new RpcException("ChannelInvoker can not reset.");
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return channel.getLocalAddress();
        }

        @Override
        public boolean isConnected() {
            return channel != null && channel.isConnected();
        }

        @Override
        public boolean hasAttribute(String key) {
            return channel.hasAttribute(key);
        }

        @Override
        public Object getAttribute(String key) {
            return channel.getAttribute(key);
        }

        @Override
        public void setAttribute(String key, Object value) {
            channel.setAttribute(key, value);
        }

        @Override
        public void removeAttribute(String key) {
            channel.removeAttribute(key);
        }

        public void reconnect() throws RemotingException {

        }

        @Override
        public void send(Object message) throws RemotingException {
            channel.send(message);
        }

        @Override
        public void send(Object message, boolean sent) throws RemotingException {
            channel.send(message, sent);
        }
    }
}
