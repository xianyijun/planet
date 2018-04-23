package cn.xianyijun.planet.rpc.rest;

import cn.xianyijun.planet.common.URL;

/**
 * The interface Rest server.
 *
 * @author xianyijun
 */
public interface RestServer {
    /**
     * Start.
     *
     * @param url the url
     */
    void start(URL url);

    /**
     * Deploy.
     *
     * @param resourceDef      the resource def
     * @param resourceInstance the resource instance
     * @param contextPath      the context path
     */
    void deploy(Class resourceDef, Object resourceInstance, String contextPath);

    /**
     * Un deploy.
     *
     * @param resourceDef the resource def
     */
    void unDeploy(Class resourceDef);

    /**
     * Stop.
     */
    void stop();
}
