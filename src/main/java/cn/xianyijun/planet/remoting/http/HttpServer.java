package cn.xianyijun.planet.remoting.http;

import java.net.InetSocketAddress;

import cn.xianyijun.planet.common.ReSetable;
import cn.xianyijun.planet.common.URL;

public interface HttpServer extends ReSetable {
    HttpHandler getHttpHandler();

    URL getUrl();

    InetSocketAddress getLocalAddress();

    void close();

    void close(int timeout);

    boolean isBound();

    boolean isClosed();

}
