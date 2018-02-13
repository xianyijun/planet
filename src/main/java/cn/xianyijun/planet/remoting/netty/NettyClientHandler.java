package cn.xianyijun.planet.remoting.netty;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.remoting.api.ChannelHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Netty client handler.
 *
 * @author xianyijun
 */
@RequiredArgsConstructor
@io.netty.channel.ChannelHandler.Sharable
@Slf4j
public class NettyClientHandler extends ChannelDuplexHandler {
    @NonNull
    private final URL url;
    @NonNull
    private final ChannelHandler handler;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("[NttyClientHandler] channelActive , channel : {} , url : {} , handler: {}", ctx.channel(), url, handler);
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("[NttyClientHandler] channelInactive , channel : {} , url : {} , handler: {}", ctx.channel(), url, handler);
        ctx.fireChannelInactive();
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise future)
            throws Exception {
        log.info("[NttyClientHandler] disconnect , channel : {} , url : {} , handler: {}  ", ctx.channel(), url, handler);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            handler.disConnected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("[NttyClientHandler] channelRead , channel : {} , url : {} , handler: {} , msg: {}", ctx.channel(), url, handler, msg);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            handler.received(channel, msg);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }


    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        log.info("[NttyClientHandler] write , channel : {} , url : {} , handler: {}, msg: {}, ", ctx.channel(), url, handler,msg);
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
        log.info("[NttyClientHandler] exceptionCaught , channel : {} , url : {} , handler: {} , cause: {}", ctx.channel(), url, handler,cause);

        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), url, handler);
        try {
            handler.caught(channel, cause);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

}
