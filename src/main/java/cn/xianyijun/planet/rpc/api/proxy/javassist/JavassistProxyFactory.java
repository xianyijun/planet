package cn.xianyijun.planet.rpc.api.proxy.javassist;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.bytecode.Proxy;
import cn.xianyijun.planet.common.bytecode.Wrapper;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.proxy.AbstractProxyFactory;
import cn.xianyijun.planet.rpc.api.proxy.AbstractProxyInvoker;
import cn.xianyijun.planet.rpc.api.proxy.InvokerInvocationHandler;

/**
 * The type Javassist proxy factory.
 */
public class JavassistProxyFactory extends AbstractProxyFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Invoker<T> invoker, Class<?>[] interfaces) {
        return (T) Proxy.getProxy(interfaces).newInstance(new InvokerInvocationHandler(invoker));
    }

    @Override
    public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) {
        final Wrapper wrapper = Wrapper.getWrapper(proxy.getClass().getName().indexOf('$') < 0 ? proxy.getClass() : type);
        return new AbstractProxyInvoker<T>(proxy, type, url) {
            @Override
            protected Object doInvoke(T proxy, String methodName,
                                      Class<?>[] parameterTypes,
                                      Object[] arguments) throws Throwable {
                return wrapper.invokeMethod(proxy, methodName, parameterTypes, arguments);
            }
        };
    }

}
