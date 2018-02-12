package cn.xianyijun.planet.cluster.api;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.Adaptive;
import cn.xianyijun.planet.common.extension.SPI;

/**
 * The interface Configurator factory.
 * @author xianyijun
 */
@SPI
public interface ConfiguratorFactory {
    /**
     * Gets configurator.
     *
     * @param url the url
     * @return the configurator
     */
    @Adaptive("protocol")
    Configurator getConfigurator(URL url);
}
