package cn.xianyijun.planet.rpc.api.protocol;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Result;
import cn.xianyijun.planet.rpc.api.proxy.ProxyFactory;
import cn.xianyijun.planet.utils.NetUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Abstract proxy protocol.
 *
 * @author xianyijun
 */
public abstract class AbstractProxyProtocol extends AbstractProtocol {

    @Getter
    @Setter
    private ProxyFactory proxyFactory;

    /**
     * Instantiates a new Abstract proxy protocol.
     */
    public AbstractProxyProtocol() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Exporter<T> export(final Invoker<T> invoker) throws RpcException {
        final String uri = serviceKey(invoker.getUrl());
        Exporter<T> exporter = (Exporter<T>) exporterMap.get(uri);
        if (exporter != null) {
            return exporter;
        }
        final Runnable runnable = doExport(proxyFactory.getProxy(invoker), invoker.getInterface(), invoker.getUrl());
        exporter = new AbstractExporter<T>(invoker) {
            @Override
            public void unExport() {
                super.unExport();
                exporterMap.remove(uri);
                if (runnable != null) {
                    try {
                        runnable.run();
                    } catch (Throwable t) {
                        logger.warn(t.getMessage(), t);
                    }
                }
            }
        };
        exporterMap.put(uri, exporter);
        return exporter;
    }

    @Override
    public <T> Invoker<T> refer(final Class<T> type, final URL url) throws RpcException {
        final Invoker<T> target = proxyFactory.getInvoker(doRefer(type, url), type, url);
        Invoker<T> invoker = new AbstractInvoker<T>(type, url) {
            @Override
            protected Result doInvoke(Invocation invocation) throws Throwable {
                try {
                    Result result = target.invoke(invocation);
                    Throwable e = result.getException();
                    if (e != null) {
                        throw new RpcException(String.format("service :%s , method: %s, failure", type, invocation.getMethodName()), e);
                    }
                    return result;
                } catch (RpcException e) {
                    throw e;
                }
            }
        };
        invokers.add(invoker);
        return invoker;
    }

    /**
     * Gets addr.
     *
     * @param url the url
     * @return the addr
     */
    protected String getAddr(URL url) {
        String bindIp = url.getParameter(Constants.BIND_IP_KEY, url.getHost());
        if (url.getParameter(Constants.ANY_HOST_KEY, false)) {
            bindIp = Constants.ANY_HOST_VALUE;
        }
        return NetUtils.getIpByHost(bindIp) + ":" + url.getParameter(Constants.BIND_PORT_KEY, url.getPort());
    }

    /**
     * Do export runnable.
     *
     * @param <T>  the type parameter
     * @param impl the
     * @param type the type
     * @param url  the url
     * @return the runnable
     * @throws RpcException the rpc exception
     */
    protected abstract <T> Runnable doExport(T impl, Class<T> type, URL url) throws RpcException;

    /**
     * Do refer t.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @param url  the url
     * @return the t
     * @throws RpcException the rpc exception
     */
    protected abstract <T> T doRefer(Class<T> type, URL url) throws RpcException;
}
