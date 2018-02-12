package cn.xianyijun.planet.common;

/**
 * The interface Node.
 */
public interface Node {
    /**
     * Gets url.
     *
     * @return the url
     */
    URL getUrl();

    /**
     * Is available boolean.
     *
     * @return the boolean
     */
    boolean isAvailable();

    /**
     * Destroy.
     */
    void destroy();

}
