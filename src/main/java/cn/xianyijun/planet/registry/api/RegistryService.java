package cn.xianyijun.planet.registry.api;

import cn.xianyijun.planet.common.URL;

import java.util.List;

/**
 * The interface Registry service.
 *
 * @author xianyijun
 */
public interface RegistryService{

    /**
     * Register.
     *
     * @param url the url
     */
    void register(URL url);

    /**
     * Un register.
     *
     * @param url the url
     */
    void unRegister(URL url);

    /**
     * Subscribe.
     *
     * @param url      the url
     * @param listener the listener
     */
    void subscribe(URL url, NotifyListener listener);

    /**
     * Unsubscribe.
     *
     * @param url      the url
     * @param listener the listener
     */
    void unSubscribe(URL url, NotifyListener listener);

    /**
     * Lookup list.
     *
     * @param url the url
     * @return the list
     */
    List<URL> lookup(URL url);

}
