package cn.xianyijun.planet.cluster.api;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.Adaptive;
import cn.xianyijun.planet.common.extension.SPI;

/**
 * The interface Router factory.
 */
@SPI
public interface RouterFactory {
    /**
     * Gets router.
     *
     * @param url the url
     * @return the router
     */
    @Adaptive("protocol")
    Router getRouter(URL url);
}
