package cn.xianyijun.planet.remoting.api.exchange;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.exchange.support.ExchangeHandlerDispatcher;
import cn.xianyijun.planet.remoting.api.exchange.support.Replier;
import cn.xianyijun.planet.remoting.api.exchange.support.header.HeaderExchanger;
import cn.xianyijun.planet.remoting.api.transport.ChannelHandlerAdapter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * The type Exchangers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Exchangers {

    /**
     * Bind exchange server.
     *
     * @param url     the url
     * @param handler the handler
     * @return the exchange server
     * @throws RemotingException the remoting exception
     */
    public static ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        url = url.addParameterIfAbsent(Constants.CODEC_KEY, "exchange");
        return getExchanger(url).bind(url, handler);
    }

    /**
     * Bind exchange server.
     *
     * @param url     the url
     * @param replier the replier
     * @return the exchange server
     * @throws RemotingException the remoting exception
     */
    public static ExchangeServer bind(String url, Replier<?> replier) throws RemotingException {
        return bind(URL.valueOf(url), replier);
    }

    /**
     * Bind exchange server.
     *
     * @param url     the url
     * @param replier the replier
     * @return the exchange server
     * @throws RemotingException the remoting exception
     */
    public static ExchangeServer bind(URL url, Replier<?> replier) throws RemotingException {
        return bind(url, new ChannelHandlerAdapter(), replier);
    }

    /**
     * Bind exchange server.
     *
     * @param url     the url
     * @param handler the handler
     * @param replier the replier
     * @return the exchange server
     * @throws RemotingException the remoting exception
     */
    public static ExchangeServer bind(String url, ChannelHandler handler, Replier<?> replier) throws RemotingException {
        return bind(URL.valueOf(url), handler, replier);
    }

    /**
     * Bind exchange server.
     *
     * @param url     the url
     * @param handler the handler
     * @param replier the replier
     * @return the exchange server
     * @throws RemotingException the remoting exception
     */
    public static ExchangeServer bind(URL url, ChannelHandler handler, Replier<?> replier) throws RemotingException {
        return bind(url, new ExchangeHandlerDispatcher(replier, handler));
    }

    /**
     * Bind exchange server.
     *
     * @param url     the url
     * @param handler the handler
     * @return the exchange server
     * @throws RemotingException the remoting exception
     */
    public static ExchangeServer bind(String url, ExchangeHandler handler) throws RemotingException {
        return bind(URL.valueOf(url), handler);
    }

    /**
     * Gets exchanger.
     *
     * @param url the url
     * @return the exchanger
     */
    public static Exchanger getExchanger(URL url) {
        String type = url.getParameter(Constants.EXCHANGER_KEY, Constants.DEFAULT_EXCHANGER);
        return getExchanger(type);
    }

    /**
     * Gets exchanger.
     *
     * @param type the type
     * @return the exchanger
     */
    public static Exchanger getExchanger(String type) {
        return new HeaderExchanger();
    }

    /**
     * Connect exchange client.
     *
     * @param url the url
     * @return the exchange client
     * @throws RemotingException the remoting exception
     */
    public static ExchangeClient connect(String url) throws RemotingException {
        return connect(URL.valueOf(url));
    }

    /**
     * Connect exchange client.
     *
     * @param url the url
     * @return the exchange client
     * @throws RemotingException the remoting exception
     */
    public static ExchangeClient connect(URL url) throws RemotingException {
        return connect(url, new ChannelHandlerAdapter(), null);
    }

    /**
     * Connect exchange client.
     *
     * @param url     the url
     * @param replier the replier
     * @return the exchange client
     * @throws RemotingException the remoting exception
     */
    public static ExchangeClient connect(String url, Replier<?> replier) throws RemotingException {
        return connect(URL.valueOf(url), new ChannelHandlerAdapter(), replier);
    }

    /**
     * Connect exchange client.
     *
     * @param url     the url
     * @param replier the replier
     * @return the exchange client
     * @throws RemotingException the remoting exception
     */
    public static ExchangeClient connect(URL url, Replier<?> replier) throws RemotingException {
        return connect(url, new ChannelHandlerAdapter(), replier);
    }

    /**
     * Connect exchange client.
     *
     * @param url     the url
     * @param handler the handler
     * @param replier the replier
     * @return the exchange client
     * @throws RemotingException the remoting exception
     */
    public static ExchangeClient connect(String url, ChannelHandler handler, Replier<?> replier) throws RemotingException {
        return connect(URL.valueOf(url), handler, replier);
    }

    /**
     * Connect exchange client.
     *
     * @param url     the url
     * @param handler the handler
     * @param replier the replier
     * @return the exchange client
     * @throws RemotingException the remoting exception
     */
    public static ExchangeClient connect(URL url, ChannelHandler handler, Replier<?> replier) throws RemotingException {
        return connect(url, new ExchangeHandlerDispatcher(replier, handler));
    }

    /**
     * Connect exchange client.
     *
     * @param url     the url
     * @param handler the handler
     * @return the exchange client
     * @throws RemotingException the remoting exception
     */
    public static ExchangeClient connect(String url, ExchangeHandler handler) throws RemotingException {
        return connect(URL.valueOf(url), handler);
    }

    /**
     * Connect exchange client.
     *
     * @param url     the url
     * @param handler the handler
     * @return the exchange client
     * @throws RemotingException the remoting exception
     */
    public static ExchangeClient connect(URL url, ExchangeHandler handler) throws RemotingException {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        url = url.addParameterIfAbsent(Constants.CODEC_KEY, "exchange");
        return getExchanger(url).connect(url, handler);
    }

}
