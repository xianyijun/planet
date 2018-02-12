package cn.xianyijun.planet.cluster.support.router;

import cn.xianyijun.planet.cluster.api.Router;
import cn.xianyijun.planet.cluster.api.RouterFactory;
import cn.xianyijun.planet.common.URL;

/**
 * The type Condition router factory.
 *
 * @author xianyijun
 */
public class ConditionRouterFactory implements RouterFactory {
    /**
     * The constant NAME.
     */
    public static final String NAME = "condition";
    @Override
    public Router getRouter(URL url) {
        return new ConditionRouter(url);
    }
}
