package cn.xianyijun.planet.registry.zookeeper;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.registry.api.Registry;
import cn.xianyijun.planet.registry.api.support.AbstractRegistryFactory;
import cn.xianyijun.planet.remoting.zookeeper.ZookeeperTransporter;
import lombok.Setter;

/**
 * The type Zookeeper registry factory.
 *
 * @author xianyijun
 * @date 2018 /2/4
 */
public class ZookeeperRegistryFactory extends AbstractRegistryFactory {

    @Setter
    private ZookeeperTransporter zookeeperTransporter;

    @Override
    public Registry createRegistry(URL url) {
        return new ZookeeperRegistry(url, zookeeperTransporter);
    }
}
