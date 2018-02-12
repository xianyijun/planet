package cn.xianyijun.planet.rpc.api;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.utils.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author xianyijun
 * @date 2018/1/22
 */
public class StaticContext extends ConcurrentHashMap<Object,Object> {
    private static final String SYSTEM_NAME = "system";
    private static final ConcurrentMap<String, StaticContext> CONTEXT_MAP = new ConcurrentHashMap<String, StaticContext>();
    private String name;

    private StaticContext(String name) {
        super();
        this.name = name;
    }

    /**
     * Gets system context.
     *
     * @return the system context
     */
    public static StaticContext getSystemContext() {
        return getContext(SYSTEM_NAME);
    }

    /**
     * Gets context.
     *
     * @param name the name
     * @return the context
     */
    private static StaticContext getContext(String name) {
        StaticContext appContext = CONTEXT_MAP.get(name);
        if (appContext == null) {
            appContext = CONTEXT_MAP.putIfAbsent(name, new StaticContext(name));
            if (appContext == null) {
                appContext = CONTEXT_MAP.get(name);
            }
        }
        return appContext;
    }

    /**
     * Remove static context.
     *
     * @param name the name
     * @return the static context
     */
    public static StaticContext remove(String name) {
        return CONTEXT_MAP.remove(name);
    }

    /**
     * Gets key.
     *
     * @param url        the url
     * @param methodName the method name
     * @param suffix     the suffix
     * @return the key
     */
    public static String getKey(URL url, String methodName, String suffix) {
        return getKey(url.getServiceKey(), methodName, suffix);
    }

    /**
     * Gets key.
     *
     * @param paras      the paras
     * @param methodName the method name
     * @param suffix     the suffix
     * @return the key
     */
    public static String getKey(Map<String, String> paras, String methodName, String suffix) {
        return getKey(StringUtils.getServiceKey(paras), methodName, suffix);
    }

    private static String getKey(String serviceKey, String methodName, String suffix) {
        StringBuffer sb = new StringBuffer().append(serviceKey).append(".").append(methodName).append(".").append(suffix);
        return sb.toString();
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}