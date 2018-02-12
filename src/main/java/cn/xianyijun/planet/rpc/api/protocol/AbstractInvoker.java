package cn.xianyijun.planet.rpc.api.protocol;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.Version;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Result;
import cn.xianyijun.planet.rpc.api.RpcContext;
import cn.xianyijun.planet.rpc.api.RpcInvocation;
import cn.xianyijun.planet.rpc.api.RpcResult;
import cn.xianyijun.planet.utils.NetUtils;
import cn.xianyijun.planet.utils.RpcUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Abstract invoker.
 *
 * @param <T> the type parameter
 */
@Slf4j
@Data
public abstract class AbstractInvoker<T> implements Invoker<T> {

    private final Class<T> type;

    private final URL url;

    private final Map<String, String> attachment;

    private volatile boolean available = true;

    private AtomicBoolean destroyed = new AtomicBoolean(false);

    /**
     * Instantiates a new Abstract invoker.
     *
     * @param type the type
     * @param url  the url
     */
    public AbstractInvoker(Class<T> type, URL url) {
        this(type, url, (Map<String, String>) null);
    }

    /**
     * Instantiates a new Abstract invoker.
     *
     * @param type the type
     * @param url  the url
     * @param keys the keys
     */
    public AbstractInvoker(Class<T> type, URL url, String[] keys) {
        this(type, url, convertAttachment(url, keys));
    }

    /**
     * Instantiates a new Abstract invoker.
     *
     * @param type       the type
     * @param url        the url
     * @param attachment the attachment
     */
    public AbstractInvoker(Class<T> type, URL url, Map<String, String> attachment) {
        if (type == null) {
            throw new IllegalArgumentException("service type == null");
        }
        if (url == null) {
            throw new IllegalArgumentException("service url == null");
        }
        this.type = type;
        this.url = url;
        this.attachment = attachment == null ? null : Collections.unmodifiableMap(attachment);
    }


    private static Map<String, String> convertAttachment(URL url, String[] keys) {
        if (keys == null || keys.length == 0) {
            return null;
        }
        Map<String, String> attachment = new HashMap<>();
        for (String key : keys) {
            String value = url.getParameter(key);
            if (value != null && value.length() > 0) {
                attachment.put(key, value);
            }
        }
        return attachment;
    }

    /**
     * Is destroyed boolean.
     *
     * @return the boolean
     */
    public boolean isDestroyed() {
        return destroyed.get();
    }

    @Override
    public void destroy() {
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }
        setAvailable(false);
    }

    @Override
    public Class<T> getInterface() {
        return type;
    }

    @Override
    public Result invoke(Invocation inv) throws RpcException {
        if (destroyed.get()){
            throw new RpcException("Rpc invoker for service " + this + " on consumer " + NetUtils.getLocalHost()
                    + " use rpc version " + Version.getVersion()
                    + " is DESTROYED, can not be invoked any more!");
        }
        RpcInvocation invocation = (RpcInvocation) inv;

        invocation.setInvoker(this);
        if (attachment != null && attachment.size() > 0) {
            invocation.addAttachmentsIfAbsent(attachment);
        }
        Map<String, String> context = RpcContext.getContext().getAttachments();
        if (context != null) {
            invocation.addAttachmentsIfAbsent(context);
        }
        if (getUrl().getMethodParameter(invocation.getMethodName(), Constants.ASYNC_KEY, false)) {
            invocation.setAttachment(Constants.ASYNC_KEY, Boolean.TRUE.toString());
        }

        RpcUtils.attachInvocationIdIfAsync(getUrl(), invocation);

        try {
            return doInvoke(invocation);
        } catch (InvocationTargetException e) { // biz exception
            Throwable te = e.getTargetException();
            if (te == null) {
                return new RpcResult(e);
            } else {
                if (te instanceof RpcException) {
                    ((RpcException) te).setCode(RpcException.BIZ_EXCEPTION);
                }
                return new RpcResult(te);
            }
        } catch (RpcException e) {
            if (e.isBiz()) {
                return new RpcResult(e);
            } else {
                throw e;
            }
        } catch (Throwable e) {
            return new RpcResult(e);
        }
    }

    @Override
    public String toString() {
        return getInterface() + " -> " + (getUrl() == null ? "" : getUrl().toString());
    }

    /**
     * Do invoke result.
     *
     * @param invocation the invocation
     * @return the result
     * @throws Throwable the throwable
     */
    protected abstract Result doInvoke(Invocation invocation) throws Throwable;
}
