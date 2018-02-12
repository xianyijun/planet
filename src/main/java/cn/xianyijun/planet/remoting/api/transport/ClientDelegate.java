package cn.xianyijun.planet.remoting.api.transport;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.Client;

import java.net.InetSocketAddress;

public class ClientDelegate implements Client {
    private transient Client client;

    public ClientDelegate() {
    }

    public ClientDelegate(Client client) {
        setClient(client);
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("client == null");
        }
        this.client = client;
    }

    @Override
    public void reset(URL url) {
        client.reset(url);
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
    public void reConnect() throws RemotingException {
        client.reConnect();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return client.getChannelHandler();
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
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
    public Object getAttribute(String key) {
        return client.getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        client.setAttribute(key, value);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        client.send(message, sent);
    }

    @Override
    public void removeAttribute(String key) {
        client.removeAttribute(key);
    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    public void close(int timeout) {
        client.close(timeout);
    }

    @Override
    public void startClose() {
        client.startClose();
    }

    @Override
    public boolean isClosed() {
        return client.isClosed();
    }

}