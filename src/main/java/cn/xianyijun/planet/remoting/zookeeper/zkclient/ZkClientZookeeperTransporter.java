package cn.xianyijun.planet.remoting.zookeeper.zkclient;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.remoting.zookeeper.ZookeeperClient;
import cn.xianyijun.planet.remoting.zookeeper.ZookeeperTransporter;

/**
 * The type Zk client zookeeper transporter.
 *
 * @author xianyijun
 * @date 2018 /2/4
 */
public class ZkClientZookeeperTransporter implements ZookeeperTransporter {

    @Override
    public ZookeeperClient connect(URL url) {
        return new ZkClientZookeeperClient(url);
    }

}
