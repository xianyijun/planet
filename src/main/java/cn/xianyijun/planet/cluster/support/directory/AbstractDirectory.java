package cn.xianyijun.planet.cluster.support.directory;


import cn.xianyijun.planet.cluster.api.Directory;
import cn.xianyijun.planet.cluster.api.Router;
import cn.xianyijun.planet.cluster.api.RouterFactory;
import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.utils.CollectionUtils;
import cn.xianyijun.planet.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The type Abstract directory.
 *
 * @param <T> the type parameter
 */
@Slf4j
@Data
public abstract class AbstractDirectory<T> implements Directory<T> {

    private final URL url;

    private volatile boolean destoryed = false;

    private volatile URL consumerUrl;

    private volatile List<Router> routerList;

    private volatile boolean destroyed = false;

    /**
     * Instantiates a new Abstract directory.
     *
     * @param url the url
     */
    public AbstractDirectory(URL url) {
        this(url, null);
    }

    /**
     * Instantiates a new Abstract directory.
     *
     * @param url     the url
     * @param routers the routers
     */
    public AbstractDirectory(URL url, List<Router> routers) {
        this(url, url, routers);
    }

    /**
     * Instantiates a new Abstract directory.
     *
     * @param url         the url
     * @param consumerUrl the consumer url
     * @param routers     the routers
     */
    public AbstractDirectory(URL url, URL consumerUrl, List<Router> routers) {
        if (url == null){
            throw new IllegalArgumentException("url can not be null");
        }
        this.url = url;
        this.consumerUrl = consumerUrl;
        setRouters(routers);
    }

    /**
     * Sets routers.
     *
     * @param routerList the router list
     */
    protected void setRouters(List<Router> routerList) {
        routerList = routerList == null ? new ArrayList<>() : new ArrayList<>(routerList);

        String routerKey = url.getParameter(Constants.ROUTER_KEY);
        if (!StringUtils.isBlank(routerKey)) {
            RouterFactory routerFactory = ExtensionLoader.getExtensionLoader(RouterFactory.class).getExtension(routerKey);
            routerList.add(routerFactory.getRouter(url));
        }
        Collections.sort(routerList);
        this.routerList = routerList;
    }

    @Override
    public void destroy() {
        destroyed = true;
    }

    @Override
    public List<Invoker<T>> list(Invocation invocation) throws RpcException {
        if (destoryed){
            throw new RpcException(String.format("Directory already destroyed url : %s", getUrl()));
        }

        List<Invoker<T>> invokers = doList(invocation);
        List<Router> localRouters = this.routerList;

        if (!CollectionUtils.isEmpty(localRouters)) {
            for (Router router : localRouters) {
                try {
                    if (router.getUrl() == null || router.getUrl().getParameter(Constants.RUNTIME_KEY, true)) {
                        invokers = router.route(invokers, getConsumerUrl(), invocation);
                    }
                } catch (Throwable t) {
                    log.error("Failed to execute router: " + getUrl() + ", cause: " + t.getMessage(), t);
                }
            }
        }
        return invokers;
    }

    /**
     * Do list list.
     *
     * @param invocation the invocation
     * @return the list
     * @throws RpcException the rpc exception
     */
    protected abstract List<Invoker<T>> doList(Invocation invocation) throws RpcException;
}

