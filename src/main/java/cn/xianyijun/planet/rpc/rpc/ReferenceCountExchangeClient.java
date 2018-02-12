package cn.xianyijun.planet.rpc.rpc;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeClient;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeHandler;
import cn.xianyijun.planet.remoting.api.exchange.ResponseFuture;
import lombok.NonNull;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The type Reference count exchange client.
 *
 * @author xianyijun
 */
public class ReferenceCountExchangeClient implements ExchangeClient {

    private final URL url;
    private final AtomicInteger refenceCount = new AtomicInteger(0);

    @NonNull
    private final ConcurrentMap<String, LazyConnectExchangeClient> ghostClientMap;

    private ExchangeClient client;


    /**
     * Instantiates a new Reference count exchange client.
     *
     * @param client         the client
     * @param ghostClientMap the ghost client map
     */
    public ReferenceCountExchangeClient(ExchangeClient client, ConcurrentMap<String, LazyConnectExchangeClient> ghostClientMap) {
        this.client = client;
        refenceCount.incrementAndGet();
        this.url = client.getUrl();
        this.ghostClientMap = ghostClientMap;
    }

    @Override
    public void reset(URL url) {
        client.reset(url);
    }

    @Override
    public ResponseFuture request(Object request) throws RemotingException {
        return client.request(request);
    }

    @Override
    public URL getUrl() {
        return client.getUrl();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return client.getRemoteAddress();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return client.getChannelHandler();
    }

    @Override
    public ResponseFuture request(Object request, int timeout) throws RemotingException {
        return client.request(request, timeout);
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    @Override
    public void reConnect() throws RemotingException {
        client.reConnect();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return client.getLocalAddress();
    }

    @Override
    public boolean hasAttribute(String key) {
        return client.hasAttribute(key);
    }

    @Override
    public void send(Object message) throws RemotingException {
        client.send(message);
    }

    @Override
    public ExchangeHandler getExchangeHandler() {
        return client.getExchangeHandler();
    }

    @Override
    public Object getAttribute(String key) {
        return client.getAttribute(key);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        client.send(message, sent);
    }

    @Override
    public void setAttribute(String key, Object value) {
        client.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        client.removeAttribute(key);
    }

    /*
     * close方法将不再幂等,调用需要注意.
     */
    @Override
    public void close() {
        close(0);
    }

    @Override
    public void close(int timeout) {
        if (refenceCount.decrementAndGet() <= 0) {
            if (timeout == 0) {
                client.close();
            } else {
                client.close(timeout);
            }
            client = replaceWithLazyClient();
        }
    }

    @Override
    public void startClose() {
        client.startClose();
    }

    private LazyConnectExchangeClient replaceWithLazyClient() {

        URL lazyUrl = url.addParameter(Constants.LAZY_CONNECT_INITIAL_STATE_KEY, Boolean.FALSE)
                .addParameter(Constants.RECONNECT_KEY, Boolean.FALSE)
                .addParameter(Constants.SEND_RECONNECT_KEY, Boolean.TRUE.toString())
                .addParameter("warning", Boolean.TRUE.toString())
                .addParameter(LazyConnectExchangeClient.REQUEST_WITH_WARNING_KEY, true)
                .addParameter("_client_memo", "referencecounthandler.replacewithlazyclient");

        String key = url.getAddress();
        LazyConnectExchangeClient gclient = ghostClientMap.get(key);
        if (gclient == null || gclient.isClosed()) {
            gclient = new LazyConnectExchangeClient(lazyUrl, client.getExchangeHandler());
            ghostClientMap.put(key, gclient);
        }
        return gclient;
    }

    @Override
    public boolean isClosed() {
        return client.isClosed();
    }

    /**
     * Increment and get count.
     */
    public void incrementAndGetCount() {
        refenceCount.incrementAndGet();
    }
}
