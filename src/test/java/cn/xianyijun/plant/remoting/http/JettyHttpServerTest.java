package cn.xianyijun.plant.remoting.http;

import org.apache.http.client.fluent.Request;
import org.junit.Test;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.remoting.http.HttpServer;
import cn.xianyijun.planet.remoting.http.jetty.JettyHttpServer;
import cn.xianyijun.planet.utils.NetUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class JettyHttpServerTest {
    @Test
    public void shouldAbleHandleRequestForJettyBinder() throws Exception {
        int port = NetUtils.getAvailablePort();
        URL url = new URL("http", "localhost", port,
                new String[]{Constants.BIND_PORT_KEY, String.valueOf(port)});
        HttpServer httpServer = new JettyHttpServer(url, (request, response) -> response.getWriter().write("Jetty"));

        String response = Request.Get(url.toJavaURL().toURI()).execute().returnContent().asString();

        assertThat(response, is("Jetty"));

        httpServer.close();
    }
}
