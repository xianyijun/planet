package cn.xianyijun.planet.rpc.api.listener;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.InvokerListener;
import cn.xianyijun.planet.rpc.api.Result;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * The type Listener invoker wrapper.
 *
 * @param <T> the type parameter
 */
@Slf4j
public class ListenerInvokerWrapper<T> implements Invoker<T> {

    private final Invoker<T> invoker;

    private final List<InvokerListener> listeners;

    /**
     * Instantiates a new Listener invoker wrapper.
     *
     * @param invoker   the invoker
     * @param listeners the listeners
     */
    public ListenerInvokerWrapper(Invoker<T> invoker, List<InvokerListener> listeners) {
        if (invoker == null) {
            throw new IllegalArgumentException("invoker == null");
        }
        this.invoker = invoker;
        this.listeners = listeners;
        if (listeners != null && listeners.size() > 0) {
            for (InvokerListener listener : listeners) {
                if (listener != null) {
                    try {
                        listener.referred(invoker);
                    } catch (Throwable t) {
                        log.error(t.getMessage(), t);
                    }
                }
            }
        }
    }

    @Override
    public Class<T> getInterface() {
        return invoker.getInterface();
    }

    @Override
    public URL getUrl() {
        return invoker.getUrl();
    }

    @Override
    public boolean isAvailable() {
        return invoker.isAvailable();
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        return invoker.invoke(invocation);
    }

    @Override
    public String toString() {
        return getInterface() + " -> " + (getUrl() == null ? " " : getUrl().toString());
    }

    @Override
    public void destroy() {
        try {
            invoker.destroy();
        } finally {
            if (listeners != null && listeners.size() > 0) {
                for (InvokerListener listener : listeners) {
                    if (listener != null) {
                        try {
                            listener.destroyed(invoker);
                        } catch (Throwable t) {
                            log.error(t.getMessage(), t);
                        }
                    }
                }
            }
        }
    }
}
