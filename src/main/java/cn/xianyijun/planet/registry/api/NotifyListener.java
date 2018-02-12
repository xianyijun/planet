package cn.xianyijun.planet.registry.api;

import cn.xianyijun.planet.common.URL;

import java.util.List;

/**
 * The interface Notify listener.
 */
public interface NotifyListener {

    /**
     * Notify.
     *
     * @param urls the urls
     */
    void notify(List<URL> urls);
}
