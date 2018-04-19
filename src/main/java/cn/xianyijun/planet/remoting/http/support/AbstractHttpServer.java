package cn.xianyijun.planet.remoting.http.support;

import java.net.InetSocketAddress;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.remoting.http.HttpHandler;
import cn.xianyijun.planet.remoting.http.HttpServer;

public class AbstractHttpServer implements HttpServer {
    private final URL url;

    private final HttpHandler handler;

    private volatile boolean closed;

    public AbstractHttpServer(URL url, HttpHandler handler) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.url = url;
        this.handler = handler;
    }

    @Override
    public HttpHandler getHttpHandler() {
        return handler;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public void reset(URL url) {
    }

    @Override
    public boolean isBound() {
        return true;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return url.toInetSocketAddress();
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public void close(int timeout) {
        close();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

}
