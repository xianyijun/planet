package cn.xianyijun.planet.remoting.api.transport;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.Endpoint;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public abstract class AbstractPeer implements Endpoint, ChannelHandler {

    private final ChannelHandler handler;

    private volatile URL url;

    private volatile boolean closing;

    private volatile boolean closed;

    /**
     * Instantiates a new Abstract peer.
     *
     * @param url     the url
     * @param handler the handler
     */
    public AbstractPeer(URL url, ChannelHandler handler) {
        if (url == null) {
            throw new IllegalArgumentException("url can not be null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler can not be null");
        }
        this.url = url;
        this.handler = handler;
    }

    @Override
    public void send(Object message) throws RemotingException {
        log.info("[AbstractPeer] send , msg:{}" ,message);
        send(message, url.getParameter(Constants.SENT_KEY, false));
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public void close(int timeout) {
        close();
    }

    @Override
    public void startClose() {
        if (isClosed()) {
            return;
        }
        closing = true;
    }

    /**
     * Sets url.
     *
     * @param url the url
     */
    protected void setUrl(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url can not be null");
        }
        this.url = url;
    }

    @Override
    public ChannelHandler getChannelHandler() {
        if (handler instanceof ChannelHandlerDelegate) {
            return ((ChannelHandlerDelegate) handler).getHandler();
        } else {
            return handler;
        }
    }

    /**
     * Gets delegate handler.
     *
     * @return the delegate handler
     */
    public ChannelHandler getDelegateHandler() {
        return handler;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * Is closing boolean.
     *
     * @return the boolean
     */
    boolean isClosing() {
        return closing && !closed;
    }

    @Override
    public void connected(Channel ch) throws RemotingException {
        log.info("[abstractPeer] connected， ch :{} ,  , handler :{} ", ch, handler);
        if (closed) {
            return;
        }
        handler.connected(ch);
    }

    @Override
    public void disConnected(Channel ch) throws RemotingException {
        log.info("[abstractPeer] disConnected， ch :{} , msg :{} , handler :{} ", ch, handler);
        handler.disConnected(ch);
    }

    @Override
    public void sent(Channel ch, Object msg) throws RemotingException {
        log.info("[abstractPeer] sent， ch :{} , msg :{} , handler :{} ", ch, msg, handler);
        if (closed) {
            return;
        }
        handler.sent(ch, msg);
    }

    @Override
    public void received(Channel ch, Object msg) throws RemotingException {
        log.info("[abstractPeer] received， ch :{} , msg :{} , handler :{} ", ch, msg, handler);
        if (closed) {
            return;
        }
        handler.received(ch, msg);
    }
    @Override
    public void caught(Channel ch, Throwable ex) throws RemotingException {
        handler.caught(ch, ex);
    }

    @Override
    public URL getUrl() {
        return url;
    }
}
