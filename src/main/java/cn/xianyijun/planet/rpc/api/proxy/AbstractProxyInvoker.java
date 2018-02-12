package cn.xianyijun.planet.rpc.api.proxy;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Result;
import cn.xianyijun.planet.rpc.api.RpcResult;

import java.lang.reflect.InvocationTargetException;

/**
 * The type Abstract proxy invoker.
 *
 * @author xianyijun
 * @param <T> the type parameter
 */
public abstract class AbstractProxyInvoker<T> implements Invoker<T> {
    private final T proxy;

    private final Class<T> type;

    private final URL url;

    /**
     * Instantiates a new Abstract proxy invoker.
     *
     * @param proxy the proxy
     * @param type  the type
     * @param url   the url
     */
    public AbstractProxyInvoker(T proxy, Class<T> type, URL url) {
        if (proxy == null) {
            throw new IllegalArgumentException("proxy == null");
        }
        if (type == null) {
            throw new IllegalArgumentException("interface == null");
        }
        if (!type.isInstance(proxy)) {
            throw new IllegalArgumentException(proxy.getClass().getName() + " not implement interface " + type);
        }
        this.proxy = proxy;
        this.type = type;
        this.url = url;
    }

    @Override
    public Class<T> getInterface() {
        return type;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        try {
            return new RpcResult(doInvoke(proxy, invocation.getMethodName(), invocation.getParameterTypes(), invocation.getArguments()));
        } catch (InvocationTargetException e) {
            return new RpcResult(e.getTargetException());
        } catch (Throwable e) {
            throw new RpcException("Failed to invoke remote proxy method " + invocation.getMethodName() + " to " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    /**
     * Do invoke object.
     *
     * @param proxy          the proxy
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @param arguments      the arguments
     * @return the object
     * @throws Throwable the throwable
     */
    protected abstract Object doInvoke(T proxy, String methodName, Class<?>[] parameterTypes, Object[] arguments) throws Throwable;

    @Override
    public String toString() {
        return getInterface() + " -> " + getUrl() == null ? " " : getUrl().toString();
    }

}
