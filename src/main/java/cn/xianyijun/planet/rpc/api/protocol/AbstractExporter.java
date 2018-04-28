package cn.xianyijun.planet.rpc.api.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.Invoker;

/**
 * The type Abstract exporter.
 *
 * @param <T> the type parameter
 * @author xianyijun
 */
public abstract class AbstractExporter<T> implements Exporter<T> {

    /**
     * The Logger.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Invoker<T> invoker;

    private volatile boolean unExported = false;

    /**
     * Instantiates a new Abstract exporter.
     *
     * @param invoker the invoker
     */
    public AbstractExporter(Invoker<T> invoker) {
        if (invoker == null) {
            throw new IllegalStateException("service invoker == null");
        }
        if (invoker.getInterface() == null) {
            throw new IllegalStateException("service type == null");
        }
        if (invoker.getUrl() == null) {
            throw new IllegalStateException("service url == null");
        }
        this.invoker = invoker;
    }

    @Override
    public Invoker<T> getInvoker() {
        return invoker;
    }

    @Override
    public void unExport() {
        if (unExported) {
            return;
        }
        unExported = true;
        getInvoker().destroy();
    }

    @Override
    public String toString() {
        return getInvoker().toString();
    }
}
