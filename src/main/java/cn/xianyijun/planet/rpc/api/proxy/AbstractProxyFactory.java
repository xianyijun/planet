package cn.xianyijun.planet.rpc.api.proxy;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.utils.ReflectUtils;

/**
 * The type Abstract proxy factory.
 * @author xianyijun
 */
public abstract class AbstractProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(Invoker<T> invoker) throws RpcException {
        Class<?>[] interfaces = null;
        String config = invoker.getUrl().getParameter("interfaces");
        if (config != null && config.length() > 0) {
            String[] types = Constants.COMMA_SPLIT_PATTERN.split(config);
            if (types != null && types.length > 0) {
                interfaces = new Class<?>[types.length + 2];
                interfaces[0] = invoker.getInterface();
                for (int i = 0; i < types.length; i++) {
                    interfaces[i + 1] = ReflectUtils.forName(types[i]);
                }
            }
        }
        if (interfaces == null) {
            interfaces = new Class<?>[]{invoker.getInterface()};
        }
        return getProxy(invoker, interfaces);
    }

    /**
     * Gets proxy.
     *
     * @param <T>     the type parameter
     * @param invoker the invoker
     * @param types   the types
     * @return the proxy
     */
    public abstract <T> T getProxy(Invoker<T> invoker, Class<?>[] types);
}
