package cn.xianyijun.planet.cluster.api;

import cn.xianyijun.planet.common.URL;

/**
 * The interface Configurator.
 * @author xianyijun
 */
public interface Configurator extends Comparable<Configurator>{
    /**
     * Gets url.
     *
     * @return the url
     */
    URL getUrl();

    /**
     * Configure url.
     *
     * @param url the url
     * @return the url
     */
    URL configure(URL url);
}
