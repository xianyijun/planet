package cn.xianyijun.planet.utils;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;

/**
 * The type Protocol utils.
 */
public class ProtocolUtils {

    /**
     * Service key string.
     *
     * @param url the url
     * @return the string
     */
    public static String serviceKey(URL url) {
        return serviceKey(url.getPort(), url.getPath(), url.getParameter(Constants.VERSION_KEY),
                url.getParameter(Constants.GROUP_KEY));
    }

    /**
     * Service key string.
     *
     * @param port           the port
     * @param serviceName    the service name
     * @param serviceVersion the service version
     * @param serviceGroup   the service group
     * @return the string
     */
    public static String serviceKey(int port, String serviceName, String serviceVersion, String serviceGroup) {
        StringBuilder buf = new StringBuilder();
        if (serviceGroup != null && serviceGroup.length() > 0) {
            buf.append(serviceGroup);
            buf.append("/");
        }
        buf.append(serviceName);
        if (serviceVersion != null && serviceVersion.length() > 0 && !"0.0.0".equals(serviceVersion)) {
            buf.append(":");
            buf.append(serviceVersion);
        }
        buf.append(":");
        buf.append(port);
        return buf.toString();
    }
}
