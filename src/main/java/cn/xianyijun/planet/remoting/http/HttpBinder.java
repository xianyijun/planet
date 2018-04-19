package cn.xianyijun.planet.remoting.http;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.Adaptive;
import cn.xianyijun.planet.common.extension.SPI;

/**
 * The interface Http binder.
 *
 * @author xianyijun
 */
@SPI("jetty")
public interface HttpBinder {

    /**
     * Bind http server.
     *
     * @param url     the url
     * @param handler the handler
     * @return the http server
     */
    @Adaptive({Constants.SERVER_KEY})
    HttpServer bind(URL url, HttpHandler handler);

}