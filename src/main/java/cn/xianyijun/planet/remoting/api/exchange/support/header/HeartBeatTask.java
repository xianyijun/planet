package cn.xianyijun.planet.remoting.api.exchange.support.header;

import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.Client;
import cn.xianyijun.planet.remoting.api.exchange.Request;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * The type Heart beat task.
 *
 * @author xianyijun
 */
@Slf4j
public class HeartBeatTask implements Runnable {

    private ChannelProvider channelProvider;

    private int heartbeat;

    private int heartbeatTimeout;

    /**
     * Instantiates a new Heart beat task.
     *
     * @param provider         the provider
     * @param heartbeat        the heartbeat
     * @param heartbeatTimeout the heartbeat timeout
     */
    HeartBeatTask(ChannelProvider provider, int heartbeat, int heartbeatTimeout) {
        this.channelProvider = provider;
        this.heartbeat = heartbeat;
        this.heartbeatTimeout = heartbeatTimeout;
    }

    @Override
    public void run() {
        try {
            long now = System.currentTimeMillis();
            for (Channel channel : channelProvider.getChannels()) {
                if (channel.isClosed()) {
                    continue;
                }
                try {
                    Long lastRead = (Long) channel.getAttribute(
                            HeaderExchangeHandler.KEY_READ_TIMESTAMP);
                    Long lastWrite = (Long) channel.getAttribute(
                            HeaderExchangeHandler.KEY_WRITE_TIMESTAMP);
                    if ((lastRead != null && now - lastRead > heartbeat)
                            || (lastWrite != null && now - lastWrite > heartbeat)) {
                        Request req = new Request();
                        req.setVersion("2.0.0");
                        req.setTwoWay(true);
                        req.setEvent(Request.HEARTBEAT_EVENT);
                        channel.send(req);
                        if (log.isDebugEnabled()) {
                            log.debug("Send heartbeat to remote channel " + channel.getRemoteAddress()
                                    + ", cause: The channel has no data-transmission exceeds a heartbeat period: " + heartbeat + "ms");
                        }
                    }
                    if (lastRead != null && now - lastRead > heartbeatTimeout) {
                        log.warn("Close channel " + channel
                                + ", because heartbeat read idle time out: " + heartbeatTimeout + "ms");
                        if (channel instanceof Client) {
                            try {
                                ((Client) channel).reConnect();
                            } catch (Exception e) {
                                //do nothing
                            }
                        } else {
                            channel.close();
                        }
                    }
                } catch (Throwable t) {
                    log.warn("Exception when heartbeat to remote channel " + channel.getRemoteAddress(), t);
                }
            }
        } catch (Throwable t) {
            log.warn("Unhandled exception when heartbeat, cause: " + t.getMessage(), t);
        }
    }

    /**
     * The interface Channel provider.
     */
    interface ChannelProvider {
        /**
         * Gets channels.
         *
         * @return the channels
         */
        Collection<Channel> getChannels();
    }
}
