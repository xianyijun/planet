package cn.xianyijun.planet.remoting.zookeeper.curator;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.remoting.zookeeper.ZookeeperClient;
import cn.xianyijun.planet.remoting.zookeeper.ZookeeperTransporter;

/**
 * The type Curator zookeeper transporter.
 *
 * @author xianyijun
 */
public class CuratorZookeeperTransporter implements ZookeeperTransporter {

    @Override
    public ZookeeperClient connect(URL url) {
        return new CuratorZookeeperClient(url);
    }

}