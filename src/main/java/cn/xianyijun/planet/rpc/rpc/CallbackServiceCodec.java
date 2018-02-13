package cn.xianyijun.planet.rpc.rpc;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.bytecode.Wrapper;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.RpcInvocation;
import cn.xianyijun.planet.rpc.api.proxy.ProxyFactory;
import cn.xianyijun.planet.utils.ConcurrentHashSet;
import cn.xianyijun.planet.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * @author xianyijun
 */
@Slf4j
public class CallbackServiceCodec {

    private static final ProxyFactory PROXY_FACTORY = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension();

    private static final RpcProtocol PROTOCOL = RpcProtocol.getRpcProtocol();
    private static final byte CALLBACK_NONE = 0x0;
    private static final byte CALLBACK_CREATE = 0x1;
    private static final byte CALLBACK_DESTROY = 0x2;
    private static final String INV_ATT_CALLBACK_KEY = "sys_callback_arg-";


    public static Object encodeInvocationArgument(Channel channel, RpcInvocation inv, int paraIndex) throws IOException {
        URL url = inv.getInvoker() == null ? null : inv.getInvoker().getUrl();
        byte callbackStatus = isCallBack(url, inv.getMethodName(), paraIndex);
        Object[] args = inv.getArguments();
        Class<?>[] pts = inv.getParameterTypes();
        switch (callbackStatus) {
            case CallbackServiceCodec.CALLBACK_NONE:
                return args[paraIndex];
            case CallbackServiceCodec.CALLBACK_CREATE:
                inv.setAttachment(INV_ATT_CALLBACK_KEY + paraIndex, exportOrUnExportCallbackService(channel, url, pts[paraIndex], args[paraIndex], true));
                return null;
            case CallbackServiceCodec.CALLBACK_DESTROY:
                inv.setAttachment(INV_ATT_CALLBACK_KEY + paraIndex, exportOrUnExportCallbackService(channel, url, pts[paraIndex], args[paraIndex], false));
                return null;
            default:
                return args[paraIndex];
        }
    }

    static Object decodeInvocationArgument(Channel channel, RpcInvocation inv, Class<?>[] pts, int paraIndex, Object inObject) throws IOException {
        URL url;
        try {
            url = RpcProtocol.getRpcProtocol().getInvoker(channel, inv).getUrl();
        } catch (RemotingException e) {
            return inObject;
        }
        byte callbackStatus = isCallBack(url, inv.getMethodName(), paraIndex);
        switch (callbackStatus) {
            case CallbackServiceCodec.CALLBACK_NONE:
                return inObject;
            case CallbackServiceCodec.CALLBACK_CREATE:
                try {
                    return referOrDestroyCallbackService(channel, url, pts[paraIndex], inv, Integer.parseInt(inv.getAttachment(INV_ATT_CALLBACK_KEY + paraIndex)), true);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new IOException(StringUtils.toString(e));
                }
            case CallbackServiceCodec.CALLBACK_DESTROY:
                try {
                    return referOrDestroyCallbackService(channel, url, pts[paraIndex], inv, Integer.parseInt(inv.getAttachment(INV_ATT_CALLBACK_KEY + paraIndex)), false);
                } catch (Exception e) {
                    throw new IOException(StringUtils.toString(e));
                }
            default:
                return inObject;
        }
    }

    private static Object referOrDestroyCallbackService(Channel channel, URL url, Class<?> clazz, RpcInvocation inv, int instId, boolean isRefer) {

        String invokerCacheKey = getServerSideCallbackInvokerCacheKey(channel , clazz.getName(), instId);

        String proxyCacheKey = getServerSideCallbackServiceCacheKey(channel, clazz.getName(), instId);

        Object proxy = channel.getAttribute(proxyCacheKey);
        String countKey = getServerSideCountKey(channel, clazz.getName());

        if (isRefer){
            if (proxy == null) {
                URL referUrl = URL.valueOf("callback://" + url.getAddress() + "/" + clazz.getName() + "?" + Constants.INTERFACE_KEY + "=" + clazz.getName());
                referUrl = referUrl.addParametersIfAbsent(url.getParameters()).removeParameter(Constants.METHODS_KEY);
                if (isInstancesOverLimit(channel, referUrl, clazz.getName(), instId, true)) {
                    @SuppressWarnings("rawtypes")
                    Invoker<?> invoker = new ChannelWrappedInvoker(clazz, channel, referUrl, String.valueOf(instId));
                    proxy = PROXY_FACTORY.getProxy(invoker);
                    channel.setAttribute(proxyCacheKey, proxy);
                    channel.setAttribute(invokerCacheKey, invoker);
                    increaseInstanceCount(channel, countKey);

                    Set<Invoker<?>> callbackInvokers = (Set<Invoker<?>>) channel.getAttribute(Constants.CHANNEL_CALLBACK_KEY);
                    if (callbackInvokers == null) {
                        callbackInvokers = new ConcurrentHashSet<>(1);
                        callbackInvokers.add(invoker);
                        channel.setAttribute(Constants.CHANNEL_CALLBACK_KEY, callbackInvokers);
                    }
                    log.info("method " + inv.getMethodName() + " include a callback service :" + invoker.getUrl() + ", a proxy :" + invoker + " has been created.");
                }
            }
        }else {
            if (proxy != null) {
                Invoker<?> invoker = (Invoker<?>) channel.getAttribute(invokerCacheKey);
                try {
                    Set<Invoker<?>> callbackInvokers = (Set<Invoker<?>>) channel.getAttribute(Constants.CHANNEL_CALLBACK_KEY);
                    if (callbackInvokers != null) {
                        callbackInvokers.remove(invoker);
                    }
                    invoker.destroy();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                // cancel refer, directly remove from the map
                channel.removeAttribute(proxyCacheKey);
                channel.removeAttribute(invokerCacheKey);
                decreaseInstanceCount(channel, countKey);
            }
        }
        return proxy;
    }

    private static String exportOrUnExportCallbackService(Channel channel, URL url, Class clazz, Object inst, boolean export) {
        int instId = System.identityHashCode(inst);

        Map<String, String> params = new HashMap<String, String>(3);
        params.put(Constants.IS_SERVER_KEY, Boolean.FALSE.toString());
        params.put(Constants.IS_CALLBACK_SERVICE, Boolean.TRUE.toString());
        String group = url.getParameter(Constants.GROUP_KEY);
        if (group != null && group.length() > 0) {
            params.put(Constants.GROUP_KEY, group);
        }
        params.put(Constants.METHODS_KEY, StringUtils.join(Wrapper.getWrapper(clazz).getDeclaredMethodNames(), ","));

        Map<String, String> tmpMap = new HashMap<String, String>(url.getParameters());
        tmpMap.putAll(params);
        tmpMap.remove(Constants.VERSION_KEY);
        tmpMap.put(Constants.INTERFACE_KEY, clazz.getName());
        URL exportUrl = new URL(RpcProtocol.NAME, channel.getLocalAddress().getAddress().getHostAddress(), channel.getLocalAddress().getPort(), clazz.getName() + "." + instId, tmpMap);

        String cacheKey = getClientSideCallbackServiceCacheKey(instId);
        String countKey = getClientSideCountKey(clazz.getName());
        if (export) {
            if (!channel.hasAttribute(cacheKey)) {
                if (isInstancesOverLimit(channel, url, clazz.getName(), instId, false)) {
                    Invoker<?> invoker = PROXY_FACTORY.getInvoker(inst, clazz, exportUrl);
                    Exporter<?> exporter = PROTOCOL.export(invoker);
                    channel.setAttribute(cacheKey, exporter);
                    log.info("export a callback service :" + exportUrl + ", on " + channel + ", url is: " + url);
                    increaseInstanceCount(channel, countKey);
                }
            }
        } else {
            if (channel.hasAttribute(cacheKey)) {
                Exporter<?> exporter = (Exporter<?>) channel.getAttribute(cacheKey);
                exporter.unExport();
                channel.removeAttribute(cacheKey);
                decreaseInstanceCount(channel, cacheKey);
            }
        }
        return String.valueOf(instId);
    }


    private static void increaseInstanceCount(Channel channel, String countKey) {
        try {
            Integer count = (Integer) channel.getAttribute(countKey);
            if (count == null) {
                count = 1;
            } else {
                count++;
            }
            channel.setAttribute(countKey, count);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void decreaseInstanceCount(Channel channel, String countkey) {
        try {
            Integer count = (Integer) channel.getAttribute(countkey);
            if (count == null || count <= 0) {
                return;
            } else {
                count--;
            }
            channel.setAttribute(countkey, count);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static boolean isInstancesOverLimit(Channel channel, URL url, String interfaceClass, int instid, boolean isServer) {
        Integer count = (Integer) channel.getAttribute(isServer ? getServerSideCountKey(channel, interfaceClass) : getClientSideCountKey(interfaceClass));
        int limit = url.getParameter(Constants.CALLBACK_INSTANCES_LIMIT_KEY, Constants.DEFAULT_CALLBACK_INSTANCES);
        if (count != null && count >= limit) {
            throw new IllegalStateException("interface " + interfaceClass + " `s callback instances num exceed providers limit :" + limit
                    + " ,current num: " + (count + 1) + ". The new callback service will not work !!! you can cancle the callback service which exported before. channel :" + channel);
        } else {
            return true;
        }
    }

    private static String getServerSideCallbackInvokerCacheKey(Channel channel, String interfaceClass, int instId) {
        return getServerSideCallbackServiceCacheKey(channel, interfaceClass, instId) + "." + "invoker";
    }

    private static String getServerSideCallbackServiceCacheKey(Channel channel, String interfaceClass, int instid) {
        return Constants.CALLBACK_SERVICE_PROXY_KEY + "." + System.identityHashCode(channel) + "." + interfaceClass + "." + instid;
    }

    private static String getServerSideCountKey(Channel channel, String interfaceClass) {
        return Constants.CALLBACK_SERVICE_PROXY_KEY + "." + System.identityHashCode(channel) + "." + interfaceClass + ".COUNT";
    }

    private static String getClientSideCountKey(String interfaceClass) {
        return Constants.CALLBACK_SERVICE_KEY + "." + interfaceClass + ".COUNT";
    }

    private static String getClientSideCallbackServiceCacheKey(int instid) {
        return Constants.CALLBACK_SERVICE_KEY + "." + instid;
    }


    private static byte isCallBack(URL url, String methodName, int argIndex) {
        byte isCallback = CALLBACK_NONE;
        if (url != null) {
            String callback = url.getParameter(methodName + "." + argIndex + ".callback");
            if (callback != null) {
                if (Boolean.TRUE.toString().equalsIgnoreCase(callback)) {
                    isCallback = CALLBACK_CREATE;
                } else if (Boolean.FALSE.toString().equalsIgnoreCase(callback)) {
                    isCallback = CALLBACK_DESTROY;
                }
            }
        }
        return isCallback;
    }
}
