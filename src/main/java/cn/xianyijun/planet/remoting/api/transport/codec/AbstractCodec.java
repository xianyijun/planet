package cn.xianyijun.planet.remoting.api.transport.codec;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.serialize.Serialization;
import cn.xianyijun.planet.exception.ExceedPayloadLimitException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.Codec;
import cn.xianyijun.planet.remoting.api.transport.CodecSupport;
import cn.xianyijun.planet.utils.NetUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * The type Abstract codec.
 */
@Slf4j
public abstract class AbstractCodec implements Codec {


    /**
     * Check payload.
     *
     * @param channel the channel
     * @param size    the size
     * @throws IOException the io exception
     */
    protected static void checkPayload(Channel channel, long size) throws IOException {
        int payload = Constants.DEFAULT_PAYLOAD;
        if (channel != null && channel.getUrl() != null) {
            payload = channel.getUrl().getParameter(Constants.PAYLOAD_KEY, Constants.DEFAULT_PAYLOAD);
        }
        if (payload > 0 && size > payload) {
            ExceedPayloadLimitException e = new ExceedPayloadLimitException("Data length too large: " + size + ", max payload: " + payload + ", channel: " + channel);
            log.error(e.getMessage(),e);
            throw e;
        }
    }

    /**
     * Gets serialization.
     *
     * @param channel the channel
     * @return the serialization
     */
    protected Serialization getSerialization(Channel channel) {
        return CodecSupport.getSerialization(channel.getUrl());
    }

    /**
     * Is client side boolean.
     *
     * @param channel the channel
     * @return the boolean
     */
    protected boolean isClientSide(Channel channel) {
        String side = (String) channel.getAttribute(Constants.SIDE_KEY);
        if ("client".equals(side)) {
            return true;
        } else if ("server".equals(side)) {
            return false;
        } else {
            InetSocketAddress address = channel.getRemoteAddress();
            URL url = channel.getUrl();
            boolean client = url.getPort() == address.getPort()
                    && NetUtils.filterLocalHost(url.getIp()).equals(
                    NetUtils.filterLocalHost(address.getAddress()
                            .getHostAddress()));
            channel.setAttribute(Constants.SIDE_KEY, client ? "client"
                    : "server");
            return client;
        }
    }


    /**
     * Is server side boolean.
     *
     * @param channel the channel
     * @return the boolean
     */
    protected boolean isServerSide(Channel channel) {
        return !isClientSide(channel);
    }
}
