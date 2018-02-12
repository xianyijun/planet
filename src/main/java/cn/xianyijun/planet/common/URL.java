package cn.xianyijun.planet.common;

import cn.xianyijun.planet.utils.StringUtils;
import lombok.Data;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Url.
 */
@Data
public final class URL implements Serializable {

    private final String protocol;

    private final String username;

    private final String password;

    private final String host;

    private final int port;

    private final String path;

    private final Map<String, String> parameters;

    private volatile transient Map<String, Number> numbers;

    private volatile transient Map<String, URL> urls;

    private volatile transient String ip;

    private volatile transient String full;

    private volatile transient String identity;

    private volatile transient String parameter;

    private volatile transient String string;


    /**
     * Instantiates a new Url.
     *
     * @param protocol   the protocol
     * @param host       the host
     * @param port       the port
     * @param path       the path
     * @param parameters the parameters
     */
    public URL(String protocol, String host, int port, String path, Map<String, String> parameters) {
        this(protocol, null, null, host, port, path, parameters);
    }

    /**
     * Instantiates a new Url.
     *
     * @param protocol   the protocol
     * @param host       the host
     * @param port       the port
     * @param parameters the parameters
     */
    public URL(String protocol, String host, int port, Map<String, String> parameters) {
        this(protocol, null, null, host, port, null, parameters);
    }

    /**
     * Instantiates a new Url.
     *
     * @param protocol the protocol
     * @param host     the host
     * @param port     the port
     * @param path     the path
     */
    public URL(String protocol, String host, int port, String path) {
        this(protocol, null, null, host, port, path, (Map<String, String>) null);
    }

    /**
     * Instantiates a new Url.
     *
     * @param protocol   the protocol
     * @param username   the username
     * @param password   the password
     * @param host       the host
     * @param port       the port
     * @param path       the path
     * @param parameters the parameters
     */
    public URL(String protocol, String username, String password, String host, int port, String path, Map<String, String> parameters) {
        if (StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
            throw new IllegalArgumentException("Invalid url, password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        this.path = path;
        if (parameters == null) {
            parameters = new HashMap<>();
        } else {
            parameters = new HashMap<>(parameters);
        }
        this.parameters = Collections.unmodifiableMap(parameters);
    }


    /**
     * To inet socket address inet socket address.
     *
     * @return the inet socket address
     */
    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(host, port);
    }

    /**
     * Sets protocol.
     *
     * @param protocol the protocol
     * @return the protocol
     */
    public URL setProtocol(String protocol) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    /**
     * Sets host.
     *
     * @param host the host
     * @return the host
     */
    public URL setHost(String host) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    /**
     * Sets path.
     *
     * @param path the path
     * @return the path
     */
    public URL setPath(String path) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    /**
     * Sets address.
     *
     * @param address the address
     * @return the address
     */
    public URL setAddress(String address) {
        int i = address.lastIndexOf(':');
        String host;
        int port = this.port;
        if (i >= 0) {
            host = address.substring(0, i);
            port = Integer.parseInt(address.substring(i + 1));
        } else {
            host = address;
        }
        return new URL(protocol, username, password, host, port, path, getParameters());
    }

    /**
     * Gets parameter.
     *
     * @param key the key
     * @return the parameter
     */
    public String getParameter(String key) {
        String value = parameters.get(key);
        if (value == null || value.length() == 0) {
            value = parameters.get(Constants.DEFAULT_KEY_PREFIX + key);
        }
        return value;
    }

    /**
     * Gets parameter.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the parameter
     */
    public long getParameter(String key, long defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.longValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        long l = Long.parseLong(value);
        getNumbers().put(key, l);
        return l;
    }

    /**
     * Gets parameter.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the parameter
     */
    public boolean getParameter(String key, boolean defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Gets parameter.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the parameter
     */
    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Get parameter string [ ].
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the string [ ]
     */
    public String[] getParameter(String key, String[] defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Constants.COMMA_SPLIT_PATTERN.split(value);
    }

    /**
     * Gets parameter.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the parameter
     */
    public int getParameter(String key, int defaultValue) {
        Number n = getNumbers().get(key);
        if (n != null) {
            return n.intValue();
        }
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        getNumbers().put(key, i);
        return i;
    }

    /**
     * Gets service key.
     *
     * @return the service key
     */
    public String getServiceKey() {
        String inf = getServiceInterface();
        if (inf == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        String group = getParameter(Constants.GROUP_KEY);
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(inf);
        String version = getParameter(Constants.VERSION_KEY);
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }

    /**
     * Gets backup address.
     *
     * @return the backup address
     */
    public String getBackupAddress() {
        return getBackupAddress(0);
    }

    /**
     * Gets backup address.
     *
     * @param defaultPort the default port
     * @return the backup address
     */
    public String getBackupAddress(int defaultPort) {
        StringBuilder address = new StringBuilder(appendDefaultPort(getAddress(), defaultPort));
        String[] backups = getParameter(Constants.BACKUP_KEY, new String[0]);
        if (backups != null && backups.length > 0) {
            for (String backup : backups) {
                address.append(",");
                address.append(appendDefaultPort(backup, defaultPort));
            }
        }
        return address.toString();
    }
    private String appendDefaultPort(String address, int defaultPort) {
        if (address != null && address.length() > 0
                && defaultPort > 0) {
            int i = address.indexOf(':');
            if (i < 0) {
                return address + ":" + defaultPort;
            } else if (Integer.parseInt(address.substring(i + 1)) == 0) {
                return address.substring(0, i + 1) + defaultPort;
            }
        }
        return address;
    }

    /**
     * Gets service interface.
     *
     * @return the service interface
     */
    public String getServiceInterface() {
        return getParameter(Constants.INTERFACE_KEY, path);
    }

    /**
     * Gets address.
     *
     * @return the address
     */
    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }

    /**
     * Add parameter if absent url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameterIfAbsent(String key, String value) {
        if (key == null || key.length() == 0
                || value == null || value.length() == 0) {
            return this;
        }
        if (hasParameter(key)) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new URL(protocol, username, password, host, port, path, map);
    }

    /**
     * Has parameter boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean hasParameter(String key) {
        String value = getParameter(key);
        return value != null && value.length() > 0;
    }

    /**
     * Gets positive parameter.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the positive parameter
     */
//======================get positive parameter ============
    public int getPositiveParameter(String key, int defaultValue) {
        if (defaultValue <= 0) {
            throw new IllegalArgumentException("defaultValue <= 0");
        }
        int value = getParameter(key, defaultValue);
        if (value <= 0) {
            return defaultValue;
        }
        return value;
    }

    //======================add parameter ===================

    /**
     * Add parameter and encoded url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameterAndEncoded(String key, String value) {
        if (value == null || value.length() == 0) {
            return this;
        }
        return addParameter(key, encode(value));
    }

    /**
     * Add parameter url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameter(String key, boolean value) {
        return addParameter(key, String.valueOf(value));
    }

    /**
     * Add parameter url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameter(String key, char value) {
        return addParameter(key, String.valueOf(value));
    }

    /**
     * Add parameter url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameter(String key, byte value) {
        return addParameter(key, String.valueOf(value));
    }

    /**
     * Add parameter url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameter(String key, short value) {
        return addParameter(key, String.valueOf(value));
    }

    /**
     * Add parameter url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameter(String key, int value) {
        return addParameter(key, String.valueOf(value));
    }

    /**
     * Add parameter url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameter(String key, long value) {
        return addParameter(key, String.valueOf(value));
    }

    /**
     * Add parameter url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameter(String key, float value) {
        return addParameter(key, String.valueOf(value));
    }

    /**
     * Add parameter url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameter(String key, double value) {
        return addParameter(key, String.valueOf(value));
    }

    /**
     * Add parameter url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameter(String key, Enum<?> value) {
        if (value == null) {
            return this;
        }
        return addParameter(key, String.valueOf(value));
    }

    /**
     * Add parameter url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameter(String key, Number value) {
        if (value == null) {
            return this;
        }
        return addParameter(key, String.valueOf(value));
    }

    /**
     * Add parameter url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameter(String key, CharSequence value) {
        if (value == null || value.length() == 0) {
            return this;
        }
        return addParameter(key, String.valueOf(value));
    }

    /**
     * Add parameter url.
     *
     * @param key   the key
     * @param value the value
     * @return the url
     */
    public URL addParameter(String key, String value) {
        if (key == null || key.length() == 0
                || value == null || value.length() == 0) {
            return this;
        }
        // 如果没有修改，直接返回。
        if (value.equals(getParameters().get(key))) { // value != null
            return this;
        }

        Map<String, String> map = new HashMap<String, String>(getParameters());
        map.put(key, value);
        return new URL(protocol, username, password, host, port, path, map);
    }

    /**
     * Add parameters url.
     *
     * @param parameters the parameters
     * @return the url
     */
    public URL addParameters(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }

        boolean hasAndEqual = true;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String value = getParameters().get(entry.getKey());
            if (value == null) {
                if (entry.getValue() != null) {
                    hasAndEqual = false;
                    break;
                }
            } else {
                if (!value.equals(entry.getValue())) {
                    hasAndEqual = false;
                    break;
                }
            }
        }
        // 如果没有修改，直接返回。
        if (hasAndEqual) {
            return this;
        }

        Map<String, String> map = new HashMap<>(getParameters());
        map.putAll(parameters);
        return new URL(protocol, username, password, host, port, path, map);
    }

    /**
     * Add parameters url.
     *
     * @param pairs the pairs
     * @return the url
     */
    public URL addParameters(String... pairs) {
        if (pairs == null || pairs.length == 0) {
            return this;
        }
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Map pairs can not be odd number.");
        }
        Map<String, String> map = new HashMap<String, String>();
        int len = pairs.length / 2;
        for (int i = 0; i < len; i++) {
            map.put(pairs[2 * i], pairs[2 * i + 1]);
        }
        return addParameters(map);
    }


    /**
     * Encode string.
     *
     * @param value the value
     * @return the string
     */
    public static String encode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Gets authority.
     *
     * @return the authority
     */
    public String getAuthority() {
        if (StringUtils.isBlank(username) && StringUtils.isBlank(password)) {
            return null;
        }
        return (username == null ? "" : username)
                + ":" + (password == null ? "" : password);
    }
    //====================value of ============================

    /**
     * Value of url.
     *
     * @param url the url
     * @return the url
     */
    public static URL valueOf(String url){
        if (url == null || (url = url.trim()).length() ==0){
            throw new  IllegalArgumentException("url can not be null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;

        int port = 0;

        String path = null;

        Map<String,String> parameters = null;

        int index = url.indexOf("?");

        if (index > 0){
            String[] parts = url.substring(index + 1).split("\\&");
            parameters = new HashMap<>();
            for (String part : parts){
                part = part.trim();
                if (part.length() > 0){
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            url = url.substring(0, index);
        }
        index = url.indexOf("://");
        if (index >= 0) {
            if (index == 0) {
                throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            }
            protocol = url.substring(0, index);
            url = url.substring(index + 3);
        } else {
            index = url.indexOf(":/");
            if (index >= 0) {
                if (index == 0){
                    throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                }
                protocol = url.substring(0, index);
                url = url.substring(index + 1);
            }
        }

        index = url.indexOf("/");
        if (index >= 0) {
            path = url.substring(index + 1);
            url = url.substring(0, index);
        }
        index = url.indexOf("@");
        if (index >= 0) {
            username = url.substring(0, index);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            url = url.substring(index + 1);
        }
        index = url.indexOf(":");
        if (index >= 0 && index < url.length() - 1) {
            port = Integer.parseInt(url.substring(index + 1));
            url = url.substring(0, index);
        }
        if (url.length() > 0) {
            host = url;
        }
        return new URL(protocol, username, password, host, port, path, parameters);
    }

    /**
     * To full string string.
     *
     * @return the string
     */
    public String toFullString() {
        if (full != null) {
            return full;
        }
        return full = buildString(true, true);
    }

    /**
     * Is any host boolean.
     *
     * @return the boolean
     */
    public boolean isAnyHost() {
        return Constants.ANY_HOST_VALUE.equals(host) || getParameter(Constants.ANY_HOST_KEY, false);
    }

    private String buildString(boolean appendUser, boolean appendParameter, String... parameters) {
        return buildString(appendUser, appendParameter, false, false, parameters);
    }

    private String buildString(boolean appendUser, boolean appendParameter, boolean useIP, boolean useService, String... parameters) {
        StringBuilder buf = new StringBuilder();
        if (protocol != null && protocol.length() > 0) {
            buf.append(protocol);
            buf.append("://");
        }
        if (appendUser && username != null && username.length() > 0) {
            buf.append(username);
            if (password != null && password.length() > 0) {
                buf.append(":");
                buf.append(password);
            }
            buf.append("@");
        }
        String host;
        if (useIP) {
            host = getIp();
        } else {
            host = getHost();
        }
        if (host != null && host.length() > 0) {
            buf.append(host);
            if (port > 0) {
                buf.append(":");
                buf.append(port);
            }
        }
        String path;
        if (useService) {
            path = getServiceKey();
        } else {
            path = getPath();
        }
        if (path != null && path.length() > 0) {
            buf.append("/");
            buf.append(path);
        }
        if (appendParameter) {
            buildParameters(buf, true, parameters);
        }
        return buf.toString();
    }

    private void buildParameters(StringBuilder buf, boolean concat, String[] parameters) {
        if (getParameters() != null && getParameters().size() > 0) {
            List<String> includes = (parameters == null || parameters.length == 0 ? null : Arrays.asList(parameters));
            boolean first = true;
            for (Map.Entry<String, String> entry : new TreeMap<>(getParameters()).entrySet()) {
                if (entry.getKey() != null && entry.getKey().length() > 0
                        && (includes == null || includes.contains(entry.getKey()))) {
                    if (first) {
                        if (concat) {
                            buf.append("?");
                        }
                        first = false;
                    } else {
                        buf.append("&");
                    }
                    buf.append(entry.getKey());
                    buf.append("=");
                    buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
                }
            }
        }
    }

    //==================getMethodParameter =============================

    /**
     * Gets method parameter.
     *
     * @param method       the method
     * @param key          the key
     * @param defaultValue the default value
     * @return the method parameter
     */
    public String getMethodParameter(String method, String key, String defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Gets method parameter.
     *
     * @param method       the method
     * @param key          the key
     * @param defaultValue the default value
     * @return the method parameter
     */
    public boolean getMethodParameter(String method, String key, boolean defaultValue) {
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Gets method parameter.
     *
     * @param method the method
     * @param key    the key
     * @return the method parameter
     */
    public String getMethodParameter(String method, String key) {
        String value = parameters.get(method + "." + key);
        if (value == null || value.length() == 0) {
            return getParameter(key);
        }
        return value;
    }

    /**
     * Gets method parameter.
     *
     * @param method       the method
     * @param key          the key
     * @param defaultValue the default value
     * @return the method parameter
     */
    public int getMethodParameter(String method, String key, int defaultValue) {
        String methodKey = method + "." + key;
        Number n = getNumbers().get(methodKey);
        if (n != null) {
            return n.intValue();
        }
        String value = getMethodParameter(method, key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        int i = Integer.parseInt(value);
        getNumbers().put(methodKey, i);
        return i;
    }

    /**
     * Gets raw parameter.
     *
     * @param key the key
     * @return the raw parameter
     */
    public String getRawParameter(String key) {
        if ("protocol".equals(key)){
            return protocol;
        }
        if ("username".equals(key)){
            return username;
        }
        if ("password".equals(key)){
            return password;
        }
        if ("host".equals(key)){
            return host;
        }
        if ("port".equals(key)){
            return String.valueOf(port);
        }
        if ("path".equals(key)){
            return path;
        }
        return getParameter(key);
    }

    /**
     * Gets parameter and decoded.
     *
     * @param key the key
     * @return the parameter and decoded
     */
    public String getParameterAndDecoded(String key) {
        return getParameterAndDecoded(key, null);
    }

    /**
     * Gets parameter and decoded.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the parameter and decoded
     */
    public String getParameterAndDecoded(String key, String defaultValue) {
        return decode(getParameter(key, defaultValue));
    }

    /**
     * Decode string.
     *
     * @param value the value
     * @return the string
     */
    public static String decode(String value) {
        if (value == null || value.length() == 0) {
            return "";
        }
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Gets backup urls.
     *
     * @return the backup urls
     */
    public List<URL> getBackupUrls() {
        List<URL> urls = new ArrayList<>();
        urls.add(this);
        String[] backups = getParameter(Constants.BACKUP_KEY, new String[0]);
        if (backups != null && backups.length > 0) {
            for (String backup : backups) {
                urls.add(this.setAddress(backup));
            }
        }
        return urls;
    }


    //===============remove parameter ==============

    /**
     * Remove parameter url.
     *
     * @param key the key
     * @return the url
     */
    public URL removeParameter(String key) {
        if (key == null || key.length() == 0) {
            return this;
        }
        return removeParameters(key);
    }

    /**
     * Remove parameters url.
     *
     * @param keys the keys
     * @return the url
     */
    public URL removeParameters(Collection<String> keys) {
        if (keys == null || keys.size() == 0) {
            return this;
        }
        return removeParameters(keys.toArray(new String[0]));
    }

    /**
     * Remove parameters url.
     *
     * @param keys the keys
     * @return the url
     */
    public URL removeParameters(String... keys) {
        if (keys == null || keys.length == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(getParameters());
        for (String key : keys) {
            map.remove(key);
        }
        if (map.size() == getParameters().size()) {
            return this;
        }
        return new URL(protocol, username, password, host, port, path, map);
    }


    /**
     * Add parameters if absent url.
     *
     * @param parameters the parameters
     * @return the url
     */
    public URL addParametersIfAbsent(Map<String, String> parameters) {
        if (parameters == null || parameters.size() == 0) {
            return this;
        }
        Map<String, String> map = new HashMap<String, String>(parameters);
        map.putAll(getParameters());
        return new URL(protocol, username, password, host, port, path, map);
    }

    /**
     * Sets port.
     *
     * @param port the port
     * @return the port
     */
    public URL setPort(int port) {
        return new URL(protocol, username, password, host, port, path, getParameters());
    }


    private Map<String, Number> getNumbers() {
        if (numbers == null) {
            numbers = new ConcurrentHashMap<>();
        }
        return numbers;
    }

    /**
     * To service string string.
     *
     * @return the string
     */
    public String toServiceString() {
        return buildString(true, false, true, true);
    }

    /**
     * To map map.
     *
     * @return the map
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>(parameters);
        if (protocol != null){
            map.put("protocol", protocol);
        }
        if (username != null){
            map.put("username", username);
        }
        if (password != null){
            map.put("password", password);
        }
        if (host != null){
            map.put("host", host);
        }
        if (port > 0) {
            map.put("port", String.valueOf(port));
        }
        if (path != null){
            map.put("path", path);
        }
        return map;
    }

    /**
     * To parameter string string.
     *
     * @return the string
     */
    public String toParameterString() {
        if (parameter != null) {
            return parameter;
        }
        return parameter = toParameterString(new String[0]);
    }

    /**
     * To parameter string string.
     *
     * @param parameters the parameters
     * @return the string
     */
    public String toParameterString(String... parameters) {
        StringBuilder buf = new StringBuilder();
        buildParameters(buf, false, parameters);
        return buf.toString();
    }

    /**
     * Clear parameters url.
     *
     * @return the url
     */
    public URL clearParameters() {
        return new URL(protocol, username, password, host, port, path, new HashMap<>());
    }

}
