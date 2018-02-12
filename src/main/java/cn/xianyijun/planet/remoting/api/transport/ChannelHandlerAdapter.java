package cn.xianyijun.planet.remoting.api.transport;

import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.ChannelHandler;

/**
 * The type Channel handler adapter.
 */
public class ChannelHandlerAdapter implements ChannelHandler {

    @Override
    public void connected(Channel channel) throws RemotingException {

    }

    @Override
    public void disConnected(Channel channel) throws RemotingException {

    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {

    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {

    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {

    }
}
