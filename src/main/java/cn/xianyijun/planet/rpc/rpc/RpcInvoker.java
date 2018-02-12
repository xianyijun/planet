package cn.xianyijun.planet.rpc.rpc;


import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.exception.TimeoutException;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeClient;
import cn.xianyijun.planet.remoting.api.exchange.ResponseFuture;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Result;
import cn.xianyijun.planet.rpc.api.RpcContext;
import cn.xianyijun.planet.rpc.api.RpcInvocation;
import cn.xianyijun.planet.rpc.api.RpcResult;
import cn.xianyijun.planet.rpc.api.protocol.AbstractInvoker;
import cn.xianyijun.planet.utils.AtomicPositiveInteger;
import cn.xianyijun.planet.utils.ConfigUtils;
import cn.xianyijun.planet.utils.RpcUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The type Rpc invoker.
 *
 * @author xianyijun
 * @param <T> the type parameter
 */
@Slf4j
public class RpcInvoker<T> extends AbstractInvoker<T> {
    private final ExchangeClient[] clients;

    private final AtomicPositiveInteger index = new AtomicPositiveInteger();

    private final String version;

    private final ReentrantLock destroyLock = new ReentrantLock();

    private final Set<Invoker<?>> invokers;

    /**
     * Instantiates a new Rpc invoker.
     *
     * @param serviceType the service type
     * @param url         the url
     * @param clients     the clients
     */
    public RpcInvoker(Class<T> serviceType, URL url, ExchangeClient[] clients) {
        this(serviceType, url, clients, null);
    }

    /**
     * Instantiates a new Rpc invoker.
     *
     * @param serviceType the service type
     * @param url         the url
     * @param clients     the clients
     * @param invokers    the invokers
     */
    public RpcInvoker(Class<T> serviceType, URL url, ExchangeClient[] clients, Set<Invoker<?>> invokers) {
        super(serviceType, url, new String[]{Constants.INTERFACE_KEY, Constants.GROUP_KEY, Constants.TOKEN_KEY, Constants.TIMEOUT_KEY});
        this.clients = clients;
        this.version = url.getParameter(Constants.VERSION_KEY, "0.0.0");
        this.invokers = invokers;
    }

    @Override
    protected Result doInvoke(final Invocation invocation) throws Throwable {
        RpcInvocation inv = (RpcInvocation) invocation;
        final String methodName = RpcUtils.getMethodName(invocation);
        inv.setAttachment(Constants.PATH_KEY, getUrl().getPath());
        inv.setAttachment(Constants.VERSION_KEY, version);

        ExchangeClient currentClient;
        if (clients.length == 1) {
            currentClient = clients[0];
        } else {
            currentClient = clients[index.getAndIncrement() % clients.length];
        }
        try {
            boolean isAsync = RpcUtils.isAsync(getUrl(), invocation);
            boolean isOneway = RpcUtils.isOneway(getUrl(), invocation);
            int timeout = getUrl().getMethodParameter(methodName, Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
            if (isOneway) {
                boolean isSent = getUrl().getMethodParameter(methodName, Constants.SENT_KEY, false);
                currentClient.send(inv, isSent);
                RpcContext.getContext().setFuture(null);
                return new RpcResult();
            } else if (isAsync) {
                ResponseFuture future = currentClient.request(inv, timeout);
                RpcContext.getContext().setFuture(new FutureAdapter<>(future));
                return new RpcResult();
            } else {
                RpcContext.getContext().setFuture(null);
                return (Result) currentClient.request(inv, timeout).get();
            }
        } catch (TimeoutException e) {
            throw new RpcException(RpcException.TIMEOUT_EXCEPTION, "Invoke remote method timeout. method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
        } catch (RemotingException e) {
            throw new RpcException(RpcException.NETWORK_EXCEPTION, "Failed to invoke remote method: " + invocation.getMethodName() + ", provider: " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        if (!super.isAvailable()) {
            return false;
        }
        for (ExchangeClient client : clients) {
            if (client.isConnected() && !client.hasAttribute(Constants.CHANNEL_ATTRIBUTE_READONLY_KEY)) {
                //cannot write == not Available ?
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        //防止client被关闭多次.在connect per jvm的情况下，client.close方法会调用计数器-1，当计数器小于等于0的情况下，才真正关闭
        if (super.isDestroyed()) {
            return;
        } else {
            //rpc check ,避免多次关闭
            destroyLock.lock();
            try {
                if (super.isDestroyed()) {
                    return;
                }
                super.destroy();
                if (invokers != null) {
                    invokers.remove(this);
                }
                for (ExchangeClient client : clients) {
                    try {
                        client.close(getShutdownTimeout());
                    } catch (Throwable t) {
                        log.warn(t.getMessage(), t);
                    }
                }

            } finally {
                destroyLock.unlock();
            }
        }
    }

    /**
     * Gets shutdown timeout.
     *
     * @return the shutdown timeout
     */
    protected static int getShutdownTimeout() {
        int timeout = Constants.DEFAULT_SERVER_SHUTDOWN_TIMEOUT;
        String value = ConfigUtils.getProperty(Constants.SHUTDOWN_WAIT_KEY);
        if (value != null && value.length() > 0) {
            try {
                timeout = Integer.parseInt(value);
            } catch (Exception e) {
            }
        }
        return timeout;
    }
}