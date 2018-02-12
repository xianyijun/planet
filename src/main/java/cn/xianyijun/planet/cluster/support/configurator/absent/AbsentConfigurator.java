package cn.xianyijun.planet.cluster.support.configurator.absent;


import cn.xianyijun.planet.cluster.support.configurator.AbstractConfigurator;
import cn.xianyijun.planet.common.URL;

/**
 * The type Absent configurator.
 * @author xianyijun
 */
public class AbsentConfigurator extends AbstractConfigurator {

    /**
     * Instantiates a new Absent configurator.
     *
     * @param configuratorUrl the configurator url
     */
    public AbsentConfigurator(URL configuratorUrl) {
        super(configuratorUrl);
    }

    @Override
    protected URL doConfigure(URL currentUrl, URL configUrl) {
        return currentUrl.addParametersIfAbsent(configUrl.getParameters());

    }
}
