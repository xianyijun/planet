package cn.xianyijun.planet.cluster.support.configurator.override;

import cn.xianyijun.planet.cluster.support.configurator.AbstractConfigurator;
import cn.xianyijun.planet.common.URL;

/**
 * The type Override configurator.
 *
 * @author xianyijun
 */
public class OverrideConfigurator extends AbstractConfigurator {
    /**
     * Instantiates a new Override configurator.
     *
     * @param url the url
     */
    public OverrideConfigurator(URL url) {
        super(url);
    }

    @Override
    public URL doConfigure(URL currentUrl, URL configUrl) {
        return currentUrl.addParameters(configUrl.getParameters());
    }
}
