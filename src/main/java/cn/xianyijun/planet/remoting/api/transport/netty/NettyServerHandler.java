package cn.xianyijun.planet.remoting.api.transport.netty;


import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import cn.xianyijun.planet.utils.NetUtils;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Netty server handler.
 * @author xianyijun
 */
@RequiredArgsConstructor
@Getter
@io.netty.channel.ChannelHandler.Sharable
@Slf4j
public class NettyServerHandler extends ChannelDuplexHandler {
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();

    @NonNull
    private final URL url;

    @NonNull
    private final ChannelHandler handler;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("[NettyServerHandler] channelActive , channel : {} , url : {} , handler: {}", ctx.channel(), url.toFullString(), handler);
        ctx.fireChannelActive();

        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            if (channel != null) {
                channels.put(NetUtils.toAddressString((InetSocketAddress)ctx.channel().remoteAddress()), channel);
            }
            handler.connected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("[NettyServerHandler] channelInactive , channel : {} , url : {} , handler: {}", ctx.channel(), url.toFullString(), handler);

        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            channels.remove(NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress()));
            handler.disConnected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }


    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise future) {
        log.info("[NettyServerHandler] disconnect , channel : {} , url : {} , handler: {}, future :{} ", ctx.channel(), url.toFullString(), handler, future);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("[NettyServerHandler] channelRead , channel : {} , url : {} , handler: {} , message :{} ", ctx.channel(), url.toFullString(), handler, msg);

        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            handler.received(channel, msg);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log.info("[NettyServerHandler] write , channel : {} , url : {} , handler: {} , message :{} ", ctx.channel(), url.toFullString(), handler, msg);

        super.write(ctx, msg, promise);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            handler.sent(channel, msg);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        log.info("[NettyServerHandler] exceptionCaught , channel : {} , url : {} , handler: {} , message :{} ", ctx.channel(), url.toFullString(), handler);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            handler.caught(channel, cause);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

}

