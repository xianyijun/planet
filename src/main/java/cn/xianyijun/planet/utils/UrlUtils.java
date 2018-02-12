package cn.xianyijun.planet.utils;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Url utils.
 *
 * @author xianyijun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtils {
    /**
     * Parse ur ls list.
     *
     * @param address  the address
     * @param defaults the defaults
     * @return the list
     */
    public static List<URL> parseURLs(String address, Map<String, String> defaults) {
        if (StringUtils.isBlank(address)) {
            return null;
        }
        String[] addresses = Constants.REGISTRY_SPLIT_PATTERN.split(address);
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        List<URL> registries = new ArrayList<>();
        for (String addr : addresses) {
            registries.add(parseURL(addr, defaults));
        }
        return registries;
    }

    /**
     * Parse url url.
     *
     * @param address  the address
     * @param defaults the defaults
     * @return the url
     */
    public static URL parseURL(String address, Map<String, String> defaults) {
        if (address == null || address.length() == 0) {
            return null;
        }
        String url;
        if (address.contains("://")) {
            url = address;
        } else {
            String[] addresses = Constants.COMMA_SPLIT_PATTERN.split(address);
            url = addresses[0];
            if (addresses.length > 1) {
                StringBuilder backup = new StringBuilder();
                for (int i = 1; i < addresses.length; i++) {
                    if (i > 1) {
                        backup.append(",");
                    }
                    backup.append(addresses[i]);
                }
                url += "?" + Constants.BACKUP_KEY + "=" + backup.toString();
            }
        }
        String defaultProtocol = defaults == null ? null : defaults.get("protocol");
        if (StringUtils.isBlank(defaultProtocol)) {
            defaultProtocol = "rpc";
        }
        String defaultUsername = defaults == null ? null : defaults.get("username");
        String defaultPassword = defaults == null ? null : defaults.get("password");
        int defaultPort = StringUtils.parseInteger(defaults == null ? null : defaults.get("port"));
        String defaultPath = defaults == null ? null : defaults.get("path");
        Map<String, String> defaultParameters = defaults == null ? null : new HashMap<>(defaults);
        if (defaultParameters != null) {
            defaultParameters.remove("protocol");
            defaultParameters.remove("username");
            defaultParameters.remove("password");
            defaultParameters.remove("host");
            defaultParameters.remove("port");
            defaultParameters.remove("path");
        }
        URL u = URL.valueOf(url);
        boolean changed = false;
        String protocol = u.getProtocol();
        String username = u.getUsername();
        String password = u.getPassword();
        String host = u.getHost();
        int port = u.getPort();
        String path = u.getPath();
        Map<String, String> parameters = new HashMap<>(u.getParameters());
        if (StringUtils.isBlank(protocol) && !StringUtils.isBlank(defaultProtocol)) {
            changed = true;
            protocol = defaultProtocol;
        }
        if (StringUtils.isBlank(username) && !StringUtils.isBlank(defaultUsername)) {
            changed = true;
            username = defaultUsername;
        }
        if (StringUtils.isBlank(password) && !StringUtils.isBlank(defaultPassword)) {
            changed = true;
            password = defaultPassword;
        }
        if (port <= 0) {
            if (defaultPort > 0) {
                changed = true;
                port = defaultPort;
            } else {
                changed = true;
                port = 9090;
            }
        }
        if (path == null || path.length() == 0) {
            if (defaultPath != null && defaultPath.length() > 0) {
                changed = true;
                path = defaultPath;
            }
        }
        if (defaultParameters != null && defaultParameters.size() > 0) {
            for (Map.Entry<String, String> entry : defaultParameters.entrySet()) {
                String key = entry.getKey();
                String defaultValue = entry.getValue();
                if (defaultValue != null && defaultValue.length() > 0) {
                    String value = parameters.get(key);
                    if (value == null || value.length() == 0) {
                        changed = true;
                        parameters.put(key, defaultValue);
                    }
                }
            }
        }
        if (changed) {
            u = new URL(protocol, username, password, host, port, path, parameters);
        }
        return u;
    }


    //===================global match ==================

    /**
     * Is match glob pattern boolean.
     *
     * @param pattern the pattern
     * @param value   the value
     * @param param   the param
     * @return the boolean
     */
    public static boolean isMatchGlobPattern(String pattern, String value, URL param) {
        if (param != null && pattern.startsWith("$")) {
            pattern = param.getRawParameter(pattern.substring(1));
        }
        return isMatchGlobPattern(pattern, value);
    }

    /**
     * Is match glob pattern boolean.
     *
     * @param pattern the pattern
     * @param value   the value
     * @return the boolean
     */
    public static boolean isMatchGlobPattern(String pattern, String value) {
        if ("*".equals(pattern)){
            return true;
        }
        if (StringUtils.isBlank(pattern) && StringUtils.isBlank(value)) {
            return true;
        }
        if (StringUtils.isBlank(pattern) && StringUtils.isBlank(value)) {
            return false;
        }

        int i = pattern.lastIndexOf('*');
        // 没有找到星号
        if (i == -1) {
            return value.equals(pattern);
        }
        // 星号在末尾
        else if (i == pattern.length() - 1) {
            return value.startsWith(pattern.substring(0, i));
        }
        // 星号的开头
        else if (i == 0) {
            return value.endsWith(pattern.substring(i + 1));
        }
        // 星号的字符串的中间
        else {
            String prefix = pattern.substring(0, i);
            String suffix = pattern.substring(i + 1);
            return value.startsWith(prefix) && value.endsWith(suffix);
        }
    }

    /**
     * Is match boolean.
     *
     * @param consumerUrl the consumer url
     * @param providerUrl the provider url
     * @return the boolean
     */
    public static boolean isMatch(URL consumerUrl, URL providerUrl) {
        String consumerInterface = consumerUrl.getServiceInterface();
        String providerInterface = providerUrl.getServiceInterface();
        if (!(Constants.ANY_VALUE.equals(consumerInterface) || StringUtils.isEquals(consumerInterface, providerInterface))){
            return false;
        }

        if (!isMatchCategory(providerUrl.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY),
                consumerUrl.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY))) {
            return false;
        }
        if (!providerUrl.getParameter(Constants.ENABLED_KEY, true)
                && !Constants.ANY_VALUE.equals(consumerUrl.getParameter(Constants.ENABLED_KEY))) {
            return false;
        }

        String consumerGroup = consumerUrl.getParameter(Constants.GROUP_KEY);
        String consumerVersion = consumerUrl.getParameter(Constants.VERSION_KEY);
        String consumerClassifier = consumerUrl.getParameter(Constants.CLASSIFIER_KEY, Constants.ANY_VALUE);

        String providerGroup = providerUrl.getParameter(Constants.GROUP_KEY);
        String providerVersion = providerUrl.getParameter(Constants.VERSION_KEY);
        String providerClassifier = providerUrl.getParameter(Constants.CLASSIFIER_KEY, Constants.ANY_VALUE);
        return (Constants.ANY_VALUE.equals(consumerGroup) || StringUtils.isEquals(consumerGroup, providerGroup) || StringUtils.isContains(consumerGroup, providerGroup))
                && (Constants.ANY_VALUE.equals(consumerVersion) || StringUtils.isEquals(consumerVersion, providerVersion))
                && (consumerClassifier == null || Constants.ANY_VALUE.equals(consumerClassifier) || StringUtils.isEquals(consumerClassifier, providerClassifier));
    }

    /**
     * Is match category boolean.
     *
     * @param category   the category
     * @param categories the categories
     * @return the boolean
     */
    public static boolean isMatchCategory(String category, String categories) {
        if (categories == null || categories.length() == 0) {
            return Constants.DEFAULT_CATEGORY.equals(category);
        } else if (categories.contains(Constants.ANY_VALUE)) {
            return true;
        } else if (categories.contains(Constants.REMOVE_VALUE_PREFIX)) {
            return !categories.contains(Constants.REMOVE_VALUE_PREFIX + category);
        } else {
            return categories.contains(category);
        }
    }

    /**
     * Is service key match boolean.
     *
     * @param pattern the pattern
     * @param value   the value
     * @return the boolean
     */
    public static boolean isServiceKeyMatch(URL pattern, URL value) {
        return pattern.getParameter(Constants.INTERFACE_KEY).equals(
                value.getParameter(Constants.INTERFACE_KEY))
                && isItemMatch(pattern.getParameter(Constants.GROUP_KEY),
                value.getParameter(Constants.GROUP_KEY))
                && isItemMatch(pattern.getParameter(Constants.VERSION_KEY),
                value.getParameter(Constants.VERSION_KEY));
    }


    /**
     * Is item match boolean.
     *
     * @param pattern the pattern
     * @param value   the value
     * @return the boolean
     */
    static boolean isItemMatch(String pattern, String value) {
        if (pattern == null) {
            return value == null;
        } else {
            return "*".equals(pattern) || pattern.equals(value);
        }
    }

}
