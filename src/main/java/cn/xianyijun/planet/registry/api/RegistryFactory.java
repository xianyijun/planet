package cn.xianyijun.planet.registry.api;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.Adaptive;
import cn.xianyijun.planet.common.extension.SPI;

/**
 * The interface Registry factory.
 *
 * @author xianyijun
 */
@SPI("rpc")
public interface RegistryFactory {
    /**
     * Gets registry.
     *
     * @param url the url
     * @return the registry
     */
    @Adaptive({"protocol"})
    Registry getRegistry(URL url);
}
