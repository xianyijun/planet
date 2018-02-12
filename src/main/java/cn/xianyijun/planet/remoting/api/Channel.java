package cn.xianyijun.planet.remoting.api;

import java.net.InetSocketAddress;

/**
 * The interface Channel.
 */
public interface Channel extends Endpoint{
    /**
     * Gets remote address.
     *
     * @return the remote address
     */
    InetSocketAddress getRemoteAddress();

    /**
     * Is connected boolean.
     *
     * @return the boolean
     */
    boolean isConnected();

    /**
     * Has attribute boolean.
     *
     * @param key the key
     * @return the boolean
     */
    boolean hasAttribute(String key);

    /**
     * Gets attribute.
     *
     * @param key the key
     * @return the attribute
     */
    Object getAttribute(String key);

    /**
     * Sets attribute.
     *
     * @param key   the key
     * @param value the value
     */
    void setAttribute(String key, Object value);

    /**
     * Remove attribute.
     *
     * @param key the key
     */
    void removeAttribute(String key);

}
