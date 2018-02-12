package cn.xianyijun.planet.remoting.api.exchange.support.header;


import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Transporters;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeClient;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeHandler;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeServer;
import cn.xianyijun.planet.remoting.api.exchange.Exchanger;
import cn.xianyijun.planet.remoting.api.transport.DecodeHandler;

/**
 * The type Header exchanger.
 * @author xianyijun
 */
public class HeaderExchanger implements Exchanger {

    /**
     * The constant NAME.
     */
    public static final String NAME = "header";

    @Override
    public ExchangeClient connect(URL url, ExchangeHandler handler) throws RemotingException {
        return new HeaderExchangeClient(Transporters.connect(url, new DecodeHandler(new HeaderExchangeHandler(handler))), true);
    }

    @Override
    public ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {
        return new HeaderExchangeServer(Transporters.bind(url, new DecodeHandler(new HeaderExchangeHandler(handler))));
    }

}
