package cn.xianyijun.planet.remoting.zookeeper;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.Adaptive;
import cn.xianyijun.planet.common.extension.SPI;

/**
 * The interface Zookeeper transporter.
 *
 * @author xianyijun
 * @date 2018 /2/4
 */
@SPI("curator")
public interface  ZookeeperTransporter {
    /**
     * Connect zookeeper client.
     *
     * @param url the url
     * @return the zookeeper client
     */
    @Adaptive({Constants.CLIENT_KEY, Constants.TRANSPORTER_KEY})
    public abstract ZookeeperClient connect(URL url);
}
