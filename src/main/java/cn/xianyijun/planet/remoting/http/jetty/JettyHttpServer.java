package cn.xianyijun.planet.remoting.http.jetty;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.log.Log;
import org.mortbay.log.StdErrLog;
import org.mortbay.thread.QueuedThreadPool;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.remoting.http.HttpHandler;
import cn.xianyijun.planet.remoting.http.servlet.DispatcherServlet;
import cn.xianyijun.planet.remoting.http.servlet.ServletManager;
import cn.xianyijun.planet.remoting.http.support.AbstractHttpServer;
import cn.xianyijun.planet.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xianyijun
 */
@Slf4j
public class JettyHttpServer extends AbstractHttpServer {

    private Server server;

    private URL url;

    public JettyHttpServer(URL url, final HttpHandler handler) {
        super(url, handler);
        this.url = url;
        Log.setLog(new StdErrLog());
        Log.getLog().setDebugEnabled(false);

        DispatcherServlet.addHttpHandler(url.getParameter(Constants.BIND_PORT_KEY, url.getPort()), handler);

        int threads = url.getParameter(Constants.THREADS_KEY, Constants.DEFAULT_THREADS);
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setDaemon(true);
        threadPool.setMaxThreads(threads);
        threadPool.setMinThreads(threads);

        SelectChannelConnector connector = new SelectChannelConnector();

        String bindIp = url.getParameter(Constants.BIND_IP_KEY, url.getHost());
        if (!url.isAnyHost() && NetUtils.isValidLocalHost(bindIp)) {
            connector.setHost(bindIp);
        }
        connector.setPort(url.getParameter(Constants.BIND_PORT_KEY, url.getPort()));

        server = new Server();
        server.setThreadPool(threadPool);
        server.addConnector(connector);

        ServletHandler servletHandler = new ServletHandler();
        ServletHolder servletHolder = servletHandler.addServletWithMapping(DispatcherServlet.class, "/*");
        servletHolder.setInitOrder(2);

        Context context = new Context(server, "/", Context.SESSIONS);
        context.setServletHandler(servletHandler);
        ServletManager.getInstance().addServletContext(url.getParameter(Constants.BIND_PORT_KEY, url.getPort()), context.getServletContext());

        try {
            server.start();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to start jetty server on " + url.getParameter(Constants.BIND_IP_KEY) + ":" + url.getParameter(Constants.BIND_PORT_KEY) + ", cause: "
                    + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        super.close();

        ServletManager.getInstance().removeServletContext(url.getParameter(Constants.BIND_PORT_KEY, url.getPort()));

        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }
}
