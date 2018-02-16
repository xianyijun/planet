package cn.xianyijun.planet.config.spring.extension;

import cn.xianyijun.planet.common.extension.SPI;

/**
 * The interface Extension factory.
 */
@SPI
public interface ExtensionFactory {
    /**
     * Gets extension.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @param name the name
     * @return the extension
     */
    <T> T getExtension(Class<T> type, String name);
}
