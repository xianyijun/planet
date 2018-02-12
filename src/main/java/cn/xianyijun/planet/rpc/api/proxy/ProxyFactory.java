package cn.xianyijun.planet.rpc.api.proxy;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.Adaptive;
import cn.xianyijun.planet.common.extension.SPI;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invoker;

/**
 * The interface Proxy factory.
 */
@SPI("javassist")
public interface ProxyFactory {

    /**
     * Gets proxy.
     *
     * @param <T>     the type parameter
     * @param invoker the invoker
     * @return the proxy
     * @throws RpcException the rpc exception
     */
    @Adaptive({Constants.PROXY_KEY})
    <T> T getProxy(Invoker<T> invoker) throws RpcException;

    /**
     * Gets invoker.
     *
     * @param <T>   the type parameter
     * @param proxy the proxy
     * @param type  the type
     * @param url   the url
     * @return the invoker
     * @throws RpcException the rpc exception
     */
    @Adaptive({Constants.PROXY_KEY})
    <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) throws RpcException;
}
