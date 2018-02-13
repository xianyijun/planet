package cn.xianyijun.planet.remoting.api;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.transport.ChannelHandlerAdapter;
import cn.xianyijun.planet.remoting.api.transport.ChannelHandlerDispatcher;
import cn.xianyijun.planet.remoting.netty.NettyTransporter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * The type Transporters.
 * @author xianyijun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class Transporters {

    /**
     * Bind server.
     *
     * @param url     the url
     * @param handler the handler
     * @return the server
     * @throws RemotingException the remoting exception
     */
    public static Server bind(String url, ChannelHandler... handler) throws RemotingException {
        return bind(URL.valueOf(url), handler);
    }

    /**
     * Bind server.
     *
     * @param url      the url
     * @param handlers the handlers
     * @return the server
     * @throws RemotingException the remoting exception
     */
    public static Server bind(URL url, ChannelHandler... handlers) throws RemotingException {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handlers == null || handlers.length == 0) {
            throw new IllegalArgumentException("handlers == null");
        }
        ChannelHandler handler;
        if (handlers.length == 1) {
            handler = handlers[0];
        } else {
            handler = new ChannelHandlerDispatcher(handlers);
        }
        return getTransporter().bind(url, handler);
    }

    /**
     * Connect client.
     *
     * @param url     the url
     * @param handler the handler
     * @return the client
     * @throws RemotingException the remoting exception
     */
    public static Client connect(String url, ChannelHandler... handler) throws RemotingException {
        return connect(URL.valueOf(url), handler);
    }

    /**
     * Connect client.
     *
     * @param url      the url
     * @param handlers the handlers
     * @return the client
     * @throws RemotingException the remoting exception
     */
    public static Client connect(URL url, ChannelHandler... handlers) throws RemotingException {
        log.info("[Transporters] connect , url : {} ,handlers :{} ",url ,handlers);
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        ChannelHandler handler;
        if (handlers == null || handlers.length == 0) {
            handler = new ChannelHandlerAdapter();
        } else if (handlers.length == 1) {
            handler = handlers[0];
        } else {
            handler = new ChannelHandlerDispatcher(handlers);
        }
        return getTransporter().connect(url, handler);
    }

    /**
     * Gets transporter.
     *
     * @return the transporter
     */
    public static Transporter getTransporter() {
        return new NettyTransporter();
    }

    /**
     * Get supported types set.
     *
     * @return the set
     */
    public static Set<String> getSupportedTypes(){
        Set<String> result = new HashSet<>();
        result.add("netty");
        return result;
    }

}
