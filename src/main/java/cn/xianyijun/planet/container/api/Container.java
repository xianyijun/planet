package cn.xianyijun.planet.container.api;


import cn.xianyijun.planet.common.extension.SPI;

/**
 * The interface Container.
 * @author xianyijun
 */
@SPI("spring")
public interface Container {

    /**
     * Start.
     */
    public void start();

    /**
     * Stop.
     */
    public void stop();
}
