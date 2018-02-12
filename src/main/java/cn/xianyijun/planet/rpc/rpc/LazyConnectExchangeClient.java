package cn.xianyijun.planet.rpc.rpc;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeClient;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeHandler;
import cn.xianyijun.planet.remoting.api.exchange.Exchangers;
import cn.xianyijun.planet.remoting.api.exchange.ResponseFuture;
import cn.xianyijun.planet.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The type Lazy connect exchange client.
 *
 * @author xianyijun
 */
@Slf4j
public class LazyConnectExchangeClient implements ExchangeClient {

    /**
     * The Request with warning key.
     */
    static final String REQUEST_WITH_WARNING_KEY = "lazy_client_request_with_warning";

    /**
     * The Request with warning.
     */
    protected final boolean requestWithWarning;

    private final URL url;
    private final ExchangeHandler requestHandler;
    private final Lock connectLock = new ReentrantLock();
    private final boolean initialState;

    private volatile ExchangeClient client;

    private AtomicLong warningCount = new AtomicLong(0);

    /**
     * Instantiates a new Lazy connect exchange client.
     *
     * @param url            the url
     * @param requestHandler the request handler
     */
    public LazyConnectExchangeClient(URL url, ExchangeHandler requestHandler) {
        this.url = url.addParameter(Constants.SEND_RECONNECT_KEY, Boolean.TRUE.toString());
        this.requestHandler = requestHandler;
        this.initialState = url.getParameter(Constants.LAZY_CONNECT_INITIAL_STATE_KEY, Constants.DEFAULT_LAZY_CONNECT_INITIAL_STATE);
        this.requestWithWarning = url.getParameter(REQUEST_WITH_WARNING_KEY, false);
    }

    private void initClient() throws RemotingException {
        if (client != null){
            return;
        }
        if (log.isInfoEnabled()) {
            log.info("Lazy connect to " + url);
        }
        connectLock.lock();
        try {
            if (client != null){
                return;
            }
            this.client = Exchangers.connect(url, requestHandler);
        } finally {
            connectLock.unlock();
        }
    }

    @Override
    public ResponseFuture request(Object request) throws RemotingException {
        warning(request);
        initClient();
        return client.request(request);
    }

    private void warning(Object request) {
        if (requestWithWarning) {
            if (warningCount.get() % 5000 == 0) {
                log.warn(new IllegalStateException("safe guard client , should not be called ,must have a bug.").getMessage());
            }
            warningCount.incrementAndGet();
        }
    }

    @Override
    public ChannelHandler getChannelHandler() {
        checkClient();
        return client.getChannelHandler();
    }

    @Override
    public boolean isConnected() {
        if (client == null) {
            return initialState;
        } else {
            return client.isConnected();
        }
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        if (client == null) {
            return InetSocketAddress.createUnresolved(NetUtils.getLocalHost(), 0);
        } else {
            return client.getLocalAddress();
        }
    }

    @Override
    public ExchangeHandler getExchangeHandler() {
        return requestHandler;
    }

    @Override
    public void send(Object message) throws RemotingException {
        initClient();
        client.send(message);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        initClient();
        client.send(message, sent);
    }

    @Override
    public boolean isClosed() {
        if (client != null){
            return client.isClosed();
        }
        else{
            return true;
        }
    }

    @Override
    public void close() {
        if (client != null){
            client.close();
        }
    }

    @Override
    public void close(int timeout) {
        if (client != null){
            client.close(timeout);
        }
    }

    @Override
    public void startClose() {
        if (client != null) {
            client.startClose();
        }
    }

    @Override
    public void reset(URL url) {
        checkClient();
        client.reset(url);
    }

    private void checkClient() {
        if (client == null) {
            throw new IllegalStateException(
                    "LazyConnectExchangeClient state error. the client has not be init .url:" + url);
        }
    }

    @Override
    public void reConnect() throws RemotingException {
        checkClient();
        client.reConnect();
    }

    @Override
    public Object getAttribute(String key) {
        if (client == null) {
            return null;
        } else {
            return client.getAttribute(key);
        }
    }

    @Override
    public void setAttribute(String key, Object value) {
        checkClient();
        client.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        checkClient();
        client.removeAttribute(key);
    }

    @Override
    public boolean hasAttribute(String key) {
        return client != null && client.hasAttribute(key);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        if (client == null) {
            return InetSocketAddress.createUnresolved(url.getHost(), url.getPort());
        } else {
            return client.getRemoteAddress();
        }
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public ResponseFuture request(Object request, int timeout) throws RemotingException {
        warning(request);
        initClient();
        return client.request(request, timeout);
    }
}
