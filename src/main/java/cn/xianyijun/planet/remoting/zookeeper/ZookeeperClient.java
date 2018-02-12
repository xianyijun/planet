package cn.xianyijun.planet.remoting.zookeeper;

import cn.xianyijun.planet.common.URL;

import java.util.List;

/**
 * The interface Zookeeper client.
 *
 * @author xianyijun
 * @date 2018 /2/4
 */
public interface ZookeeperClient {
    /**
     * Create.
     *
     * @param path      the path
     * @param ephemeral the ephemeral
     */
    void create(String path, boolean ephemeral);

    /**
     * Delete.
     *
     * @param path the path
     */
    void delete(String path);

    /**
     * Gets children.
     *
     * @param path the path
     * @return the children
     */
    List<String> getChildren(String path);

    /**
     * Add child listener list.
     *
     * @param path     the path
     * @param listener the listener
     * @return the list
     */
    List<String> addChildListener(String path, ChildListener listener);

    /**
     * Remove child listener.
     *
     * @param path     the path
     * @param listener the listener
     */
    void removeChildListener(String path, ChildListener listener);

    /**
     * Add state listener.
     *
     * @param listener the listener
     */
    void addStateListener(StateListener listener);

    /**
     * Remove state listener.
     *
     * @param listener the listener
     */
    void removeStateListener(StateListener listener);

    /**
     * Is connected boolean.
     *
     * @return the boolean
     */
    boolean isConnected();

    /**
     * Close.
     */
    void close();

    /**
     * Gets url.
     *
     * @return the url
     */
    URL getUrl();
}
