package cn.xianyijun.planet.rpc.api;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.Adaptive;
import cn.xianyijun.planet.common.extension.SPI;
import cn.xianyijun.planet.exception.RpcException;


/**
 * The interface Protocol.
 *
 * @author xianyijun
 */
@SPI("rpc")
public interface Protocol {

    /**
     * Get default port int.
     *
     * @return the int
     */
    default int getDefaultPort(){
        return 30110;
    }

    /**
     * Export exporter.
     *
     * @param <T>     the type parameter
     * @param invoker the invoker
     * @return the exporter
     * @throws RpcException the rpc exception
     */
    @Adaptive
    <T> Exporter<T> export(Invoker<T> invoker) throws RpcException;

    /**
     * Refer invoker.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @param url  the url
     * @return the invoker
     * @throws RpcException the rpc exception
     */
    @Adaptive
    <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException;

    /**
     * Destroy.
     */
    void destroy();
}
