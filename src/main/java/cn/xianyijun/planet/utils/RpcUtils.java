package cn.xianyijun.planet.utils;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.RpcInvocation;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The type Rpc utils.
 * @author xianyijun
 */
@Slf4j
public class RpcUtils {
    private static final AtomicLong INVOKE_ID = new AtomicLong(0);

    /**
     * Attach invocation id if async.
     *
     * @param url the url
     * @param inv the inv
     */
    public static void attachInvocationIdIfAsync(URL url, Invocation inv) {
        if (isAttachInvocationId(url, inv) && getInvocationId(inv) == null && inv instanceof RpcInvocation) {
            ((RpcInvocation) inv).setAttachment(Constants.ID_KEY, String.valueOf(INVOKE_ID.getAndIncrement()));
        }
    }

    private static boolean isAttachInvocationId(URL url, Invocation invocation) {
        String value = url.getMethodParameter(invocation.getMethodName(), Constants.AUTO_ATTACH_INVOCATION_ID_KEY);
        if (value == null) {
            return isAsync(url, invocation);
        } else {
            return Boolean.TRUE.toString().equalsIgnoreCase(value);
        }
    }

    /**
     * Gets invocation id.
     *
     * @param inv the inv
     * @return the invocation id
     */
    public static Long getInvocationId(Invocation inv) {
        String id = inv.getAttachment(Constants.ID_KEY);
        return id == null ? null : new Long(id);
    }

    /**
     * Is async boolean.
     *
     * @param url the url
     * @param inv the inv
     * @return the boolean
     */
    public static boolean isAsync(URL url, Invocation inv) {
        boolean isAsync;
        //如果Java代码中设置优先.
        if (Boolean.TRUE.toString().equals(inv.getAttachment(Constants.ASYNC_KEY))) {
            isAsync = true;
        } else {
            isAsync = url.getMethodParameter(getMethodName(inv), Constants.ASYNC_KEY, false);
        }
        return isAsync;
    }


    /**
     * Is oneway boolean.
     *
     * @param url the url
     * @param inv the inv
     * @return the boolean
     */
    public static boolean isOneway(URL url, Invocation inv) {
        boolean isOneway = Boolean.FALSE.toString().equals(inv.getAttachment(Constants.RETURN_KEY)) || !url.getMethodParameter(getMethodName(inv), Constants.RETURN_KEY, true);
        return isOneway;
    }


    /**
     * Gets method name.
     *
     * @param invocation the invocation
     * @return the method name
     */
    public static String getMethodName(Invocation invocation) {
        if (Constants.$INVOKE.equals(invocation.getMethodName())
                && invocation.getArguments() != null
                && invocation.getArguments().length > 0
                && invocation.getArguments()[0] instanceof String) {
            return (String) invocation.getArguments()[0];
        }
        return invocation.getMethodName();
    }

    /**
     * Get arguments object [ ].
     *
     * @param invocation the invocation
     * @return the object [ ]
     */
    public static Object[] getArguments(Invocation invocation) {
        if (Constants.$INVOKE.equals(invocation.getMethodName())
                && invocation.getArguments() != null
                && invocation.getArguments().length > 2
                && invocation.getArguments()[2] instanceof Object[]) {
            return (Object[]) invocation.getArguments()[2];
        }
        return invocation.getArguments();
    }

    public static Class<?> getReturnType(Invocation invocation) {
        try {
            if (invocation != null && invocation.getInvoker() != null
                    && invocation.getInvoker().getUrl() != null
                    && !invocation.getMethodName().startsWith("$")) {
                String service = invocation.getInvoker().getUrl().getServiceInterface();
                if (service != null && service.length() > 0) {
                    Class<?> cls = ReflectUtils.forName(service);
                    Method method = cls.getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                    if (method.getReturnType() == void.class) {
                        return null;
                    }
                    return method.getReturnType();
                }
            }
        } catch (Throwable t) {
            log.warn(t.getMessage(), t);
        }
        return null;
    }

    public static Type[] getReturnTypes(Invocation invocation) {
        try {
            if (invocation != null && invocation.getInvoker() != null
                    && invocation.getInvoker().getUrl() != null
                    && !invocation.getMethodName().startsWith("$")) {
                String service = invocation.getInvoker().getUrl().getServiceInterface();
                if (service != null && service.length() > 0) {
                    Class<?> cls = ReflectUtils.forName(service);
                    Method method = cls.getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                    if (method.getReturnType() == void.class) {
                        return null;
                    }
                    return new Type[]{method.getReturnType(), method.getGenericReturnType()};
                }
            }
        } catch (Throwable t) {
            log.warn(t.getMessage(), t);
        }
        return null;
    }

}
