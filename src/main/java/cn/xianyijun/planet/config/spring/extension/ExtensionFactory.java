package cn.xianyijun.planet.config.spring.extension;

import cn.xianyijun.planet.common.extension.SPI;

@SPI
public interface ExtensionFactory {
    /**
     * @param type
     * @param name
     * @param <T>
     * @return
     */
    <T> T getExtension(Class<T> type, String name);
}
