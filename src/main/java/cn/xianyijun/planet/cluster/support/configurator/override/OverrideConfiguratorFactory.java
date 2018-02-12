package cn.xianyijun.planet.cluster.support.configurator.override;


import cn.xianyijun.planet.cluster.api.Configurator;
import cn.xianyijun.planet.cluster.api.ConfiguratorFactory;
import cn.xianyijun.planet.common.URL;

/**
 * @author xianyijun
 */
public class OverrideConfiguratorFactory implements ConfiguratorFactory {
    @Override
    public Configurator getConfigurator(URL url) {
       return new OverrideConfigurator(url);
    }
}
