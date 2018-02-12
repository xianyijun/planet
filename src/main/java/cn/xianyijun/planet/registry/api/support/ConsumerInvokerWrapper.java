package cn.xianyijun.planet.registry.api.support;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.registry.api.integration.RegistryDirectory;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Result;
import lombok.Data;

/**
 * The type Consumer invoker wrapper.
 *
 * @param <T> the type parameter
 * @author xianyijun
 * @date 2018 /1/26
 */
@Data
public class ConsumerInvokerWrapper<T> implements Invoker {
    private Invoker<T> invoker;
    private URL originUrl;
    private URL registryUrl;
    private URL consumerUrl;
    private RegistryDirectory registryDirectory;

    /**
     * Instantiates a new Consumer invoker wrapper.
     *
     * @param invoker           the invoker
     * @param registryUrl       the registry url
     * @param consumerUrl       the consumer url
     * @param registryDirectory the registry directory
     */
    public ConsumerInvokerWrapper(Invoker<T> invoker, URL registryUrl, URL consumerUrl, RegistryDirectory registryDirectory) {
        this.invoker = invoker;
        this.originUrl = URL.valueOf(invoker.getUrl().toFullString());
        this.registryUrl = URL.valueOf(registryUrl.toFullString());
        this.consumerUrl = consumerUrl;
        this.registryDirectory = registryDirectory;
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
    public void destroy() {
        invoker.destroy();
    }
}
