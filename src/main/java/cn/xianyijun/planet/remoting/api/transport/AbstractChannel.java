package cn.xianyijun.planet.remoting.api.transport;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;

/**
 * The type Abstract channel.
 */
public abstract class AbstractChannel extends AbstractPeer implements Channel {
    /**
     * Instantiates a new Abstract channel.
     *
     * @param url     the url
     * @param handler the handler
     */
    public AbstractChannel(URL url, ChannelHandler handler) {
        super(url, handler);
    }


    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        if (isClosed()) {
            throw new RemotingException(this, "Failed to send message "
                    + (message == null ? "" : message.getClass().getName()) + ":" + message
                    + ", cause: Channel closed. channel: " + getLocalAddress() + " -> " + getRemoteAddress());
        }
    }

    @Override
    public String toString() {
        return getLocalAddress() + " -> " + getRemoteAddress();
    }
}
