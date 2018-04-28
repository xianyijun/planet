package cn.xianyijun.plant.remoting.http.jetty;

import org.apache.http.client.fluent.Request;
import org.junit.Test;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.remoting.http.HttpServer;
import cn.xianyijun.planet.remoting.http.jetty.JettyHttpBinder;
import cn.xianyijun.planet.utils.NetUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JettyHttpBinderTest {

    @Test
    public void handleRequestAndResponseForJettyBinder() throws Exception {
        int port = NetUtils.getAvailablePort();
        String responseStr = "Hello World";
        URL url = new URL("http", "localhost", port, new String[]{
                Constants.BIND_PORT_KEY, String.valueOf(port)
        });
        HttpServer httpServer = new JettyHttpBinder().bind(url, (request, response) -> {
            response.getWriter().write(responseStr);
        });

        String response = Request.Get(url.toJavaURL().toURI()).execute().returnContent().asString();
        System.out.println(response);
        assertThat(response, is(responseStr));

        httpServer.close();
    }
}
