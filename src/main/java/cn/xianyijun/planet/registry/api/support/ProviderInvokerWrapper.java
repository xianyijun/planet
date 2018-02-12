package cn.xianyijun.planet.registry.api.support;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Result;
import lombok.Data;

/**
 * The type Provider invoker wrapper.
 *
 * @param <T> the type parameter
 * @author xianyijun
 * @date 2018 /1/26
 */
@Data
public class ProviderInvokerWrapper<T> implements Invoker {
    private Invoker<T> invoker;
    private URL originUrl;
    private URL registryUrl;
    private URL providerUrl;
    private volatile boolean isReg;

    /**
     * Instantiates a new Provider invoker wrapper.
     *
     * @param invoker     the invoker
     * @param registryUrl the registry url
     * @param providerUrl the provider url
     */
    public ProviderInvokerWrapper(Invoker<T> invoker, URL registryUrl, URL providerUrl) {
        this.invoker = invoker;
        this.originUrl = URL.valueOf(invoker.getUrl().toFullString());
        this.registryUrl = URL.valueOf(registryUrl.toFullString());
        this.providerUrl = providerUrl;
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
