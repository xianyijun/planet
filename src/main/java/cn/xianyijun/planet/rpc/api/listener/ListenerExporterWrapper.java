package cn.xianyijun.planet.rpc.api.listener;

import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.ExporterListener;
import cn.xianyijun.planet.rpc.api.Invoker;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * The type Listener exporter wrapper.
 *
 * @param <T> the type parameter
 */
@Slf4j
public class ListenerExporterWrapper<T> implements Exporter<T> {
    private final Exporter<T> exporter;

    private final List<ExporterListener> listeners;

    /**
     * Instantiates a new Listener exporter wrapper.
     *
     * @param exporter  the exporter
     * @param listeners the listeners
     */
    public ListenerExporterWrapper(Exporter<T> exporter, List<ExporterListener> listeners) {
        if (exporter == null) {
            throw new IllegalArgumentException("exporter == null");
        }
        this.exporter = exporter;
        this.listeners = listeners;
        if (listeners != null && listeners.size() > 0) {
            RuntimeException exception = null;
            for (ExporterListener listener : listeners) {
                if (listener != null) {
                    try {
                        listener.exported(this);
                    } catch (RuntimeException t) {
                        log.error(t.getMessage(), t);
                        exception = t;
                    }
                }
            }
            if (exception != null) {
                throw exception;
            }
        }
    }
    @Override
    public Invoker<T> getInvoker() {
        return exporter.getInvoker();
    }

    @Override
    public void unExport() {
        try {
            exporter.unExport();
        } finally {
            if (listeners != null && listeners.size() > 0) {
                RuntimeException exception = null;
                for (ExporterListener listener : listeners) {
                    if (listener != null) {
                        try {
                            listener.unExported(this);
                        } catch (RuntimeException t) {
                            log.error(t.getMessage(), t);
                            exception = t;
                        }
                    }
                }
                if (exception != null) {
                    throw exception;
                }
            }
        }
    }

}
