package cn.xianyijun.planet.rpc.api;

/**
 * The interface Exporter.
 *
 * @param <T> the type parameter
 */
public interface Exporter<T> {
    /**
     * Gets invoker.
     *
     * @return the invoker
     */
    Invoker<T> getInvoker();

    /**
     * Un export.
     */
    void unExport();
}
