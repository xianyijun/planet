package cn.xianyijun.planet.remoting.netty;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.Client;
import cn.xianyijun.planet.remoting.api.Server;
import cn.xianyijun.planet.remoting.api.Transporter;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Netty transporter.
 */
@Slf4j
public class NettyTransporter implements Transporter {
    /**
     * The constant NAME.
     */
    public static final String NAME = "netty";

    @Override
    public Server bind(URL url, ChannelHandler handler) throws RemotingException {
        return new NettyServer(url, handler);
    }

    @Override
    public Client connect(URL url, ChannelHandler handler) throws RemotingException {
        log.info("[NettyTransporter] connect , url :{} ,handler :{}",url, handler);
        return new NettyClient(url,handler);
    }
}
