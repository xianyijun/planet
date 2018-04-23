package cn.xianyijun.planet.rpc.rest;

import cn.xianyijun.planet.remoting.http.HttpBinder;
import lombok.Setter;

/**
 * @author xianyijun
 */
public class RestServerFactory {

    @Setter
    private HttpBinder httpBinder;

    public RestServer createServer(String name) {
        if ("jetty".equalsIgnoreCase(name)) {
            return new RpcHttpServer(httpBinder);
        } else if ("netty".equalsIgnoreCase(name)) {
            return new NettyRestServer();
        } else {
            throw new IllegalArgumentException("Unrecognized server name: " + name);
        }
    }
}
