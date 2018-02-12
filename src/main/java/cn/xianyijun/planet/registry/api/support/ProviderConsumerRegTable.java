package cn.xianyijun.planet.registry.api.support;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.registry.api.integration.RegistryDirectory;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.utils.ConcurrentHashSet;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Provider consumer reg table.
 *
 * @author xianyijun
 * @date 2018 /1/26
 */
public class ProviderConsumerRegTable {
    /**
     * The constant providerInvokers.
     */
    private static ConcurrentHashMap<String, Set<ProviderInvokerWrapper>> providerInvokers = new ConcurrentHashMap<String, Set<ProviderInvokerWrapper>>();
    /**
     * The constant consumerInvokers.
     */
    private static ConcurrentHashMap<String, Set<ConsumerInvokerWrapper>> consumerInvokers = new ConcurrentHashMap<String, Set<ConsumerInvokerWrapper>>();

    /**
     * Register provider.
     *
     * @param invoker     the invoker
     * @param registryUrl the registry url
     * @param providerUrl the provider url
     */
    public static void registerProvider(Invoker invoker, URL registryUrl, URL providerUrl) {
        ProviderInvokerWrapper wrapperInvoker = new ProviderInvokerWrapper(invoker, registryUrl, providerUrl);
        String serviceUniqueName = providerUrl.getServiceKey();
        Set<ProviderInvokerWrapper> invokers = providerInvokers.get(Objects.requireNonNull(serviceUniqueName));
        if (invokers == null) {
            providerInvokers.putIfAbsent(serviceUniqueName, new ConcurrentHashSet<>());
            invokers = providerInvokers.get(serviceUniqueName);
        }
        invokers.add(wrapperInvoker);
    }

    /**
     * Gets provider invoker.
     *
     * @param serviceUniqueName the service unique name
     * @return the provider invoker
     */
    public static Set<ProviderInvokerWrapper> getProviderInvoker(String serviceUniqueName) {
        Set<ProviderInvokerWrapper> invokers = providerInvokers.get(serviceUniqueName);
        if (invokers == null) {
            return Collections.emptySet();
        }
        return invokers;
    }

    /**
     * Gets provider wrapper.
     *
     * @param invoker the invoker
     * @return the provider wrapper
     */
    public static ProviderInvokerWrapper getProviderWrapper(Invoker invoker) {
        URL providerUrl = invoker.getUrl();
        if (Constants.REGISTRY_PROTOCOL.equals(providerUrl.getProtocol())) {
            providerUrl = URL.valueOf(providerUrl.getParameterAndDecoded(Constants.EXPORT_KEY));
        }
        String serviceUniqueName = providerUrl.getServiceKey();
        Set<ProviderInvokerWrapper> invokers = providerInvokers.get(Objects.requireNonNull(serviceUniqueName));
        if (invokers == null) {
            return null;
        }

        for (ProviderInvokerWrapper providerWrapper : invokers) {
            Invoker providerInvoker = providerWrapper.getInvoker();
            if (providerInvoker == invoker) {
                return providerWrapper;
            }
        }

        return null;
    }

    /**
     * Register consuemr.
     *
     * @param invoker           the invoker
     * @param registryUrl       the registry url
     * @param consumerUrl       the consumer url
     * @param registryDirectory the registry directory
     */
    public static void registerConsuemr(Invoker invoker, URL registryUrl, URL consumerUrl, RegistryDirectory registryDirectory) {
        ConsumerInvokerWrapper wrapperInvoker = new ConsumerInvokerWrapper(invoker, registryUrl, consumerUrl, registryDirectory);
        String serviceUniqueName = consumerUrl.getServiceKey();
        Set<ConsumerInvokerWrapper> invokers = consumerInvokers.get(serviceUniqueName);
        if (invokers == null) {
            consumerInvokers.putIfAbsent(serviceUniqueName, new ConcurrentHashSet<ConsumerInvokerWrapper>());
            invokers = consumerInvokers.get(serviceUniqueName);
        }
        invokers.add(wrapperInvoker);
    }

    /**
     * Gets consumer invoker.
     *
     * @param serviceUniqueName the service unique name
     * @return the consumer invoker
     */
    public static Set<ConsumerInvokerWrapper> getConsumerInvoker(String serviceUniqueName) {
        Set<ConsumerInvokerWrapper> invokers = consumerInvokers.get(serviceUniqueName);
        if (invokers == null) {
            return Collections.emptySet();
        }
        return invokers;
    }

}
