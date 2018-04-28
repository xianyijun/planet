package cn.xianyijun.planet.remoting.http.jetty;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.remoting.http.HttpBinder;
import cn.xianyijun.planet.remoting.http.HttpHandler;
import cn.xianyijun.planet.remoting.http.HttpServer;

/**
 * @author xianyijun
 */
public class JettyHttpBinder implements HttpBinder {

    @Override
    public HttpServer bind(URL url, HttpHandler handler) {
        return new JettyHttpServer(url, handler);
    }

}
