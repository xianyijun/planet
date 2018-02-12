package cn.xianyijun.planet.registry.api.support;


import cn.xianyijun.planet.cluster.api.Cluster;
import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.bytecode.Wrapper;
import cn.xianyijun.planet.registry.api.Registry;
import cn.xianyijun.planet.registry.api.RegistryService;
import cn.xianyijun.planet.registry.api.integration.RegistryDirectory;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Protocol;
import cn.xianyijun.planet.rpc.api.proxy.ProxyFactory;
import cn.xianyijun.planet.utils.NetUtils;
import cn.xianyijun.planet.utils.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * The type Rpc registry factory.
 *
 * @author xianyijun
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class RpcRegistryFactory extends AbstractRegistryFactory {
    private Protocol protocol;
    private ProxyFactory proxyFactory;
    private Cluster cluster;

    @Override
    protected Registry createRegistry(URL url) {
        url = getRegistryURL(url);
        List<URL> urlList = new ArrayList<>();
        urlList.add(url.removeParameter(Constants.BACKUP_KEY));
        String backUp = url.getParameter(Constants.BACKUP_KEY);
        if (!StringUtils.isBlank(backUp)) {
            String[] addresses = Constants.COMMA_SPLIT_PATTERN.split(backUp);
            for (String address : addresses) {
                urlList.add(url.setAddress(address));
            }
        }
        RegistryDirectory<RegistryService> directory = new RegistryDirectory<>(RegistryService.class, url.addParameter(Constants.INTERFACE_KEY, RegistryService.class.getName()).addParameterAndEncoded(Constants.REFER_KEY, url.toParameterString()));
        Invoker<RegistryService> registryInvoker = cluster.join(directory);

        RegistryService registryService = proxyFactory.getProxy(registryInvoker);

        RpcRegistry registry = new RpcRegistry(registryInvoker, registryService);
        directory.setRegistry(registry);
        directory.setProtocol(protocol);
        directory.notify(urlList);
        directory.subscribe(new URL(Constants.CONSUMER_PROTOCOL, NetUtils.getLocalHost(), 0, RegistryService.class.getName(), url.getParameters()));
        return registry;
    }

    private static URL getRegistryURL(URL url) {
        return url.setPath(RegistryService.class.getName())
                .removeParameter(Constants.EXPORT_KEY).removeParameter(Constants.REFER_KEY)
                .addParameter(Constants.INTERFACE_KEY, RegistryService.class.getName())
                .addParameter(Constants.CLUSTER_STICKY_KEY, "true")
                .addParameter(Constants.LAZY_CONNECT_KEY, "true")
                .addParameter(Constants.RECONNECT_KEY, "false")
                .addParameterIfAbsent(Constants.TIMEOUT_KEY, "10000")
                .addParameterIfAbsent(Constants.CONNECT_TIMEOUT_KEY, "10000")
                .addParameter(Constants.METHODS_KEY, StringUtils.join(new HashSet<>(Arrays.asList(Wrapper.getWrapper(RegistryService.class).getDeclaredMethodNames())), ","))
                .addParameter("subscribe.1.callback", "true")
                .addParameter("unSubscribe.1.callback", "false");
    }

}
