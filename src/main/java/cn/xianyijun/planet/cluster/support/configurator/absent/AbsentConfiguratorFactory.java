package cn.xianyijun.planet.cluster.support.configurator.absent;


import cn.xianyijun.planet.cluster.api.Configurator;
import cn.xianyijun.planet.cluster.api.ConfiguratorFactory;
import cn.xianyijun.planet.common.URL;

/**
 * The type Absent configurator factory.
 *
 * @author xianyijun
 */
public class AbsentConfiguratorFactory implements ConfiguratorFactory {

    @Override
    public Configurator getConfigurator(URL url) {
        return new AbsentConfigurator(url);
    }

}
