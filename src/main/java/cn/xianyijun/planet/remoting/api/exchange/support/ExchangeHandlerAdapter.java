package cn.xianyijun.planet.remoting.api.exchange.support;

import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeChannel;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeHandler;
import cn.xianyijun.planet.remoting.api.transport.ChannelHandlerAdapter;

/**
 *
 * @author xianyijun
 * @date 2017/10/28
 */
public abstract class ExchangeHandlerAdapter extends ChannelHandlerAdapter implements ExchangeHandler {
    @Override
    public Object reply(ExchangeChannel channel, Object request) throws RemotingException {
        return null;
    }
}
