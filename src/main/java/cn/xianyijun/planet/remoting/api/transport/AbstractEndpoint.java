package cn.xianyijun.planet.remoting.api.transport;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.ReSetable;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.Codec;
import cn.xianyijun.planet.rpc.rpc.RpcCodec;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Getter
public abstract class AbstractEndpoint extends AbstractPeer implements ReSetable {

    private Codec codec;

    private int timeout;

    private int connectTimeout;

    /**
     * Instantiates a new Abstract endpoint.
     *
     * @param url     the url
     * @param handler the handler
     */
    public AbstractEndpoint(URL url, ChannelHandler handler) {
        super(url, handler);
        this.codec = getChannelCodec(url);
        this.timeout = url.getPositiveParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
        this.connectTimeout = url.getPositiveParameter(Constants.CONNECT_TIMEOUT_KEY, Constants.DEFAULT_CONNECT_TIMEOUT);
    }


    /**
     * Gets channel codec.
     *
     * @param url the url
     * @return the channel codec
     */
    protected static Codec getChannelCodec(URL url) {
        return new RpcCodec();
    }

    @Override
    public void reset(URL url) {
        if (isClosed()) {
            throw new IllegalStateException("Failed to reset parameters "
                    + url + ", cause: Channel closed. channel: " + getLocalAddress());
        }
        try {
            if (url.hasParameter(Constants.TIMEOUT_KEY)) {
                int t = url.getParameter(Constants.TIMEOUT_KEY, 0);
                if (t > 0) {
                    this.timeout = t;
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        try {
            if (url.hasParameter(Constants.CONNECT_TIMEOUT_KEY)) {
                int t = url.getParameter(Constants.CONNECT_TIMEOUT_KEY, 0);
                if (t > 0) {
                    this.connectTimeout = t;
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        try {
            if (url.hasParameter(Constants.CODEC_KEY)) {
                this.codec = getChannelCodec(url);
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

}
