package cn.xianyijun.planet.cluster.support.invoker;

import cn.xianyijun.planet.cluster.api.Directory;
import cn.xianyijun.planet.cluster.api.LoadBalance;
import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.Version;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Result;
import cn.xianyijun.planet.utils.CollectionUtils;
import cn.xianyijun.planet.utils.NetUtils;
import cn.xianyijun.planet.utils.RpcUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Abstract cluster invoker.
 *
 * @author xianyijun
 * @param <T> the type parameter
 */
@Slf4j
public abstract class AbstractClusterInvoker<T> implements Invoker<T> {

    /**
     * The Directory.
     */
    protected final Directory<T> directory;

    /**
     * The Available check.
     */
    private final boolean availableCheck;

    private AtomicBoolean destroyed = new AtomicBoolean(false);

    private volatile Invoker<T> stickyInvoker = null;


    /**
     * Instantiates a new Abstract cluster invoker.
     *
     * @param directory the directory
     */
    public AbstractClusterInvoker(Directory<T> directory) {
        this(directory, directory.getUrl());
    }

    /**
     * Instantiates a new Abstract cluster invoker.
     *
     * @param directory the directory
     * @param url       the url
     */
    public AbstractClusterInvoker(Directory<T> directory, URL url) {
        if (directory == null){
            throw new IllegalArgumentException("service directory == null");
        }
        this.directory = directory;
        this.availableCheck = url.getParameter(Constants.CLUSTER_AVAILABLE_CHECK_KEY, Constants.DEFAULT_CLUSTER_AVAILABLE_CHECK);
    }


    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        checkWhetherDestroyed();

        LoadBalance loadbalance;

        List<Invoker<T>> invokers = list(invocation);

        if (!CollectionUtils.isEmpty(invokers)){
            loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(invokers.get(0).getUrl()
                    .getMethodParameter(invocation.getMethodName(), Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE));
        } else {
            loadbalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(Constants.DEFAULT_LOADBALANCE);
        }
        RpcUtils.attachInvocationIdIfAsync(getUrl(), invocation);
        return doInvoke(invocation, invokers, loadbalance);
    }

    /**
     * Do invoke result.
     *
     * @param invocation  the invocation
     * @param invokers    the invokers
     * @param loadbalance the loadbalance
     * @return the result
     */
    protected abstract Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance);

    /**
     * Select invoker.
     *
     * @param loadbalance the loadbalance
     * @param invocation  the invocation
     * @param invokers    the invokers
     * @param selected    the selected
     * @return the invoker
     * @throws RpcException the rpc exception
     */
    protected Invoker<T> select(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException {
        if (CollectionUtils.isEmpty(invokers)){
            return null;
        }
        String methodName = invocation == null ? "" : invocation.getMethodName();

        boolean sticky = invokers.get(0).getUrl().getMethodParameter(methodName, Constants.CLUSTER_STICKY_KEY, Constants.DEFAULT_CLUSTER_STICKY);
        {
            if (stickyInvoker != null && !invokers.contains(stickyInvoker)) {
                stickyInvoker = null;
            }
            if (sticky && stickyInvoker != null && (selected == null || !selected.contains(stickyInvoker))) {
                if (availableCheck && stickyInvoker.isAvailable()) {
                    return stickyInvoker;
                }
            }
        }
        Invoker<T> invoker = doSelect(loadbalance, invocation, invokers, selected);

        if (sticky) {
            stickyInvoker = invoker;
        }
        return invoker;
    }

    private Invoker<T> doSelect(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException {
        if (CollectionUtils.isEmpty(invokers)){
            return null;
        }
        if (invokers.size() == 1){
            return invokers.get(0);
        }
        if (invokers.size() == 2 && selected != null && selected.size() > 0) {
            return selected.get(0) == invokers.get(0) ? invokers.get(1) : invokers.get(0);
        }
        Invoker<T> invoker = loadbalance.select(invokers, getUrl(), invocation);

        if ((selected != null && selected.contains(invoker)) || (!invoker.isAvailable() && getUrl() != null && availableCheck)) {
            try {
                Invoker<T> reInvoker = reselect(loadbalance, invocation, invokers, selected, availableCheck);
                if (reInvoker != null) {
                    invoker = reInvoker;
                } else {
                    //Check the index of current selected invoker, if it's not the last one, choose the one at index+1.
                    int index = invokers.indexOf(invoker);
                    try {
                        //Avoid collision
                        invoker = index < invokers.size() - 1 ? invokers.get(index + 1) : invoker;
                    } catch (Exception e) {
                        log.warn(e.getMessage() + " may because invokers list dynamic change, ignore.", e);
                    }
                }
            } catch (Throwable t) {
                log.error("clustor relselect fail reason is :" + t.getMessage() + " if can not slove ,you can set cluster.availablecheck=false in url", t);
            }
        }
        return invoker;
    }


    private Invoker<T> reselect(LoadBalance loadbalance, Invocation invocation,
                                List<Invoker<T>> invokers, List<Invoker<T>> selected, boolean availableCheck)
            throws RpcException {

        List<Invoker<T>> reselectInvokers = new ArrayList<Invoker<T>>(invokers.size() > 1 ? (invokers.size() - 1) : invokers.size());

        if (availableCheck) {
            for (Invoker<T> invoker : invokers) {
                if (invoker.isAvailable()) {
                    if (selected == null || !selected.contains(invoker)) {
                        reselectInvokers.add(invoker);
                    }
                }
            }
            if (reselectInvokers.size() > 0) {
                return loadbalance.select(reselectInvokers, getUrl(), invocation);
            }
        } else {
            for (Invoker<T> invoker : invokers) {
                if (selected == null || !selected.contains(invoker)) {
                    reselectInvokers.add(invoker);
                }
            }
            if (reselectInvokers.size() > 0) {
                return loadbalance.select(reselectInvokers, getUrl(), invocation);
            }
        }
        {
            if (selected != null) {
                for (Invoker<T> invoker : selected) {
                    if ((invoker.isAvailable())
                            && !reselectInvokers.contains(invoker)) {
                        reselectInvokers.add(invoker);
                    }
                }
            }
            if (reselectInvokers.size() > 0) {
                return loadbalance.select(reselectInvokers, getUrl(), invocation);
            }
        }
        return null;
    }

    /**
     * Check whether destroyed.
     */
    protected void checkWhetherDestroyed() {
        if (destroyed.get()) {
            throw new RpcException("Rpc cluster invoker for " + getInterface() + " on consumer " + NetUtils.getLocalHost()
                    + " use rpc version " + Version.getVersion()
                    + " is now destroyed! Can not invoke any more.");
        }
    }


    /**
     * Check invokers.
     *
     * @param invokers   the invokers
     * @param invocation the invocation
     */
    protected void checkInvokers(List<Invoker<T>> invokers, Invocation invocation) {
        if (CollectionUtils.isEmpty(invokers)) {
            throw new RpcException("Failed to invoke the method "
                    + invocation.getMethodName() + " in the service " + getInterface().getName()
                    + ". No provider available for the service " + directory.getUrl().getServiceKey()
                    + " from registry " + directory.getUrl().getAddress()
                    + " on the consumer " + NetUtils.getLocalHost()
                    + " using the rpc version " + Version.getVersion()
                    + ". Please check if the providers have been started and registered.");
        }
    }

    /**
     * List list.
     *
     * @param invocation the invocation
     * @return the list
     * @throws RpcException the rpc exception
     */
    protected List<Invoker<T>> list(Invocation invocation) throws RpcException {
        List<Invoker<T>> invokers = directory.list(invocation);
        return invokers;
    }

    @Override
    public Class<T> getInterface() {
        return directory.getInterface();
    }

    @Override
    public URL getUrl() {
        return directory.getUrl();
    }

    @Override
    public boolean isAvailable() {
        Invoker<T> invoker = stickyInvoker;
        if (invoker != null) {
            return invoker.isAvailable();
        }
        return directory.isAvailable();
    }
    @Override
    public void destroy() {
        if (destroyed.compareAndSet(false, true)) {
            directory.destroy();
        }
    }

}
