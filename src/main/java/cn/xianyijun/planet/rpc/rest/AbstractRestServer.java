package cn.xianyijun.planet.rpc.rest;

import org.jboss.resteasy.spi.ResteasyDeployment;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.utils.StringUtils;

/**
 * The type Abstract rest server.
 *
 * @author xianyijun
 */
public abstract class AbstractRestServer implements RestServer {

    @Override
    public void start(URL url) {
        getDeployment().getMediaTypeMappings().put("json", "application/json");
        getDeployment().getMediaTypeMappings().put("xml", "text/xml");
        getDeployment().getProviderClasses().add(RpcContextFilter.class.getName());

        loadProviders(url.getParameter(Constants.EXTENSION_KEY, ""));

        doStart(url);
    }

    @Override
    public void deploy(Class resourceDef, Object resourceInstance, String contextPath) {
        if (StringUtils.isEmpty(contextPath)) {
            getDeployment().getRegistry().addResourceFactory(new RpcResourceFactory(resourceInstance, resourceDef));
        } else {
            getDeployment().getRegistry().addResourceFactory(new RpcResourceFactory(resourceInstance, resourceDef), contextPath);
        }
    }

    @Override
    public void unDeploy(Class resourceDef) {
        getDeployment().getRegistry().removeRegistrations(resourceDef);
    }

    /**
     * Load providers.
     *
     * @param value the value
     */
    protected void loadProviders(String value) {
        for (String clazz : Constants.COMMA_SPLIT_PATTERN.split(value)) {
            if (!StringUtils.isEmpty(clazz)) {
                getDeployment().getProviderClasses().add(clazz.trim());
            }
        }
    }

    /**
     * Gets deployment.
     *
     * @return the deployment
     */
    protected abstract ResteasyDeployment getDeployment();

    /**
     * Do start.
     *
     * @param url the url
     */
    protected abstract void doStart(URL url);
}
