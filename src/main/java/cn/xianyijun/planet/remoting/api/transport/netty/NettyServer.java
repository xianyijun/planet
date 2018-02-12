package cn.xianyijun.planet.remoting.api.transport.netty;


import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.remoting.api.Server;
import cn.xianyijun.planet.remoting.api.transport.AbstractServer;
import cn.xianyijun.planet.remoting.api.transport.dispatcher.ChannelHandlers;
import cn.xianyijun.planet.utils.ExecutorUtil;
import cn.xianyijun.planet.utils.NetUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * The type Netty server.
 *
 * @author xianyijun
 */
@Slf4j
public class NettyServer extends AbstractServer implements Server {

    private Map<String, Channel> channels;

    private ServerBootstrap bootstrap;

    private io.netty.channel.Channel channel;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * Instantiates a new Netty server.
     *
     * @param url     the url
     * @param handler the handler
     * @throws RemotingException the remoting exception
     */
    public NettyServer(URL url, ChannelHandler handler) throws RemotingException {
        super(url, ChannelHandlers.wrap(handler, ExecutorUtil.setThreadName(url, SERVER_THREAD_POOL_NAME)));
    }

    @Override
    protected void doOpen() throws Throwable {

        bootstrap = new ServerBootstrap();

        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("NettyServerBoss", true));
        workerGroup = new NioEventLoopGroup(getUrl().getPositiveParameter(Constants.IO_THREADS_KEY, Constants.DEFAULT_IO_THREADS),
                new DefaultThreadFactory("NettyServerWorker", true));

        final NettyServerHandler nettyServerHandler = new NettyServerHandler(getUrl(), this);

        channels = nettyServerHandler.getChannels();

        bootstrap.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY,Boolean.TRUE)
                .childOption(ChannelOption.SO_REUSEADDR,Boolean.TRUE)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        NettyCodecAdapter adapter = new NettyCodecAdapter(getCodec(), getUrl(), NettyServer.this);
                        nioSocketChannel.pipeline()
                                .addLast("decoder",adapter.getDecoder())
                                .addLast("encoder",adapter.getEncoder())
                                .addLast("handler",nettyServerHandler);
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(getBindAddress());
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();
    }

    @Override
    protected void doClose() throws Throwable {
        try{
            if (channel != null){
                channel.close();
            }
        }catch (Throwable e){
            log.warn(e.getMessage(), e);
        }
        try{
            Collection<Channel> channels = getChannels();
            if (channels != null && channels.isEmpty()){
                channels.stream().forEach(Channel::close);
            }
        }catch (Throwable e){
            log.warn(e.getMessage(), e);
        }

        try {
            if (bootstrap != null) {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
        try {
            if (channels != null) {
                channels.clear();
            }
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
    }

    @Override
    public boolean isBound() {
        return channel.isActive();
    }

    @Override
    public Collection<Channel> getChannels() {
        Collection<Channel> chs = new HashSet<Channel>();
        for (Channel channel : this.channels.values()) {
            if (channel.isConnected()) {
                chs.add(channel);
            } else {
                channels.remove(NetUtils.toAddressString(channel.getRemoteAddress()));
            }
        }
        return chs;
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        return channels.get(NetUtils.toAddressString(remoteAddress));
    }
}
