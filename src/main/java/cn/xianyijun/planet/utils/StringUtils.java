package cn.xianyijun.planet.utils;


import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.io.UnsafeStringWriter;
import com.alibaba.fastjson.JSON;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type String utils.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

    /**
     * The constant EMPTY_STRING_ARRAY.
     */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final Pattern KVP_PATTERN = Pattern.compile("([_.a-zA-Z0-9][-_.a-zA-Z0-9]*)[=](.*)"); //key value pair pattern.

    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");

    /**
     * Is blank boolean.
     *
     * @param str the str
     * @return the boolean
     */
    public static boolean isBlank(String str) {
        if (str == null || str.length() == 0){
            return true;
        }
        return false;
    }

    /**
     * Is empty boolean.
     *
     * @param str the str
     * @return the boolean
     */
    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * Is not empty boolean.
     *
     * @param str the str
     * @return the boolean
     */
    public static boolean isNotEmpty(String str) {
        return str != null && str.length() > 0;
    }

    /**
     * Is equals boolean.
     *
     * @param s1 the s 1
     * @param s2 the s 2
     * @return the boolean
     */
    public static boolean isEquals(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }

    /**
     * Is integer boolean.
     *
     * @param str the str
     * @return the boolean
     */
    public static boolean isInteger(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        return INT_PATTERN.matcher(str).matches();
    }

    /**
     * Parse integer int.
     *
     * @param str the str
     * @return the int
     */
    public static int parseInteger(String str) {
        if (!isInteger(str)) {
            return 0;
        }
        return Integer.parseInt(str);
    }

    /**
     * Is java identifier boolean.
     *
     * @param s the s
     * @return the boolean
     */
    public static boolean isJavaIdentifier(String s) {
        if (s.length() == 0 || !Character.isJavaIdentifierStart(s.charAt(0))) {
            return false;
        }
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is contains boolean.
     *
     * @param values the values
     * @param value  the value
     * @return the boolean
     */
    public static boolean isContains(String values, String value) {
        if (values == null || values.length() == 0) {
            return false;
        }
        return isContains(Constants.COMMA_SPLIT_PATTERN.split(values), value);
    }

    /**
     * Is contains boolean.
     *
     * @param values the values
     * @param value  the value
     * @return the boolean
     */
    public static boolean isContains(String[] values, String value) {
        if (value != null && value.length() > 0 && values != null && values.length > 0) {
            for (String v : values) {
                if (value.equals(v)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Is numeric boolean.
     *
     * @param str the str
     * @return the boolean
     */
    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * To string string.
     *
     * @param e the e
     * @return the string
     */
    public static String toString(Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName());
        if (e.getMessage() != null) {
            p.print(": " + e.getMessage());
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

    /**
     * To string string.
     *
     * @param msg the msg
     * @param e   the e
     * @return the string
     */
    public static String toString(String msg, Throwable e) {
        UnsafeStringWriter w = new UnsafeStringWriter();
        w.write(msg + "\n");
        PrintWriter p = new PrintWriter(w);
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

    /**
     * Translat string.
     *
     * @param src  the src
     * @param from the from
     * @param to   the to
     * @return the string
     */
    public static String translat(String src, String from, String to) {
        if (isEmpty(src)) {
            return src;
        }
        StringBuilder sb = null;
        int ix;
        char c;
        for (int i = 0, len = src.length(); i < len; i++) {
            c = src.charAt(i);
            ix = from.indexOf(c);
            if (ix == -1) {
                if (sb != null) {
                    sb.append(c);
                }
            } else {
                if (sb == null) {
                    sb = new StringBuilder(len);
                    sb.append(src, 0, i);
                }
                if (ix < to.length()) {
                    sb.append(to.charAt(ix));
                }
            }
        }
        return sb == null ? src : sb.toString();
    }

    /**
     * Split string [ ].
     *
     * @param str the str
     * @param ch  the ch
     * @return the string [ ]
     */
    public static String[] split(String str, char ch) {
        List<String> list = null;
        char c;
        int ix = 0, len = str.length();
        for (int i = 0; i < len; i++) {
            c = str.charAt(i);
            if (c == ch) {
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(str.substring(ix, i));
                ix = i + 1;
            }
        }
        if (ix > 0) {
            list.add(str.substring(ix));
        }
        return list == null ? EMPTY_STRING_ARRAY : (String[]) list.toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Join string.
     *
     * @param array the array
     * @return the string
     */
    public static String join(String[] array) {
        if (array.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : array) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * Join string.
     *
     * @param array the array
     * @param split the split
     * @return the string
     */
    public static String join(String[] array, char split) {
        if (array.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(split);
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }

    /**
     * Join string.
     *
     * @param array the array
     * @param split the split
     * @return the string
     */
    public static String join(String[] array, String split) {
        if (array.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(split);
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }

    /**
     * Join string.
     *
     * @param coll  the coll
     * @param split the split
     * @return the string
     */
    public static String join(Collection<String> coll, String split) {
        if (coll.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String s : coll) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(split);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private static Map<String, String> parseKeyValuePair(String str, String itemSeparator) {
        String[] tmp = str.split(itemSeparator);
        Map<String, String> map = new HashMap<String, String>(tmp.length);
        for (int i = 0; i < tmp.length; i++) {
            Matcher matcher = KVP_PATTERN.matcher(tmp[i]);
            if (matcher.matches() == false) {
                continue;
            }
            map.put(matcher.group(1), matcher.group(2));
        }
        return map;
    }

    /**
     * Gets query string value.
     *
     * @param qs  the qs
     * @param key the key
     * @return the query string value
     */
    public static String getQueryStringValue(String qs, String key) {
        Map<String, String> map = StringUtils.parseQueryString(qs);
        return map.get(key);
    }

    /**
     * Parse query string map.
     *
     * @param qs the qs
     * @return the map
     */
    public static Map<String, String> parseQueryString(String qs) {
        if (qs == null || qs.length() == 0) {
            return new HashMap<String, String>();
        }
        return parseKeyValuePair(qs, "\\&");
    }

    /**
     * Gets service key.
     *
     * @param ps the ps
     * @return the service key
     */
    public static String getServiceKey(Map<String, String> ps) {
        StringBuilder buf = new StringBuilder();
        String group = ps.get(Constants.GROUP_KEY);
        if (group != null && group.length() > 0) {
            buf.append(group).append("/");
        }
        buf.append(ps.get(Constants.INTERFACE_KEY));
        String version = ps.get(Constants.VERSION_KEY);
        if (version != null && version.length() > 0) {
            buf.append(":").append(version);
        }
        return buf.toString();
    }

    /**
     * To query string string.
     *
     * @param ps the ps
     * @return the string
     */
    public static String toQueryString(Map<String, String> ps) {
        StringBuilder buf = new StringBuilder();
        if (ps != null && ps.size() > 0) {
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(ps).entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null && key.length() > 0
                        && value != null && value.length() > 0) {
                    if (buf.length() > 0) {
                        buf.append("&");
                    }
                    buf.append(key);
                    buf.append("=");
                    buf.append(value);
                }
            }
        }
        return buf.toString();
    }

    /**
     * Camel to split name string.
     *
     * @param camelName the camel name
     * @param split     the split
     * @return the string
     */
    public static String camelToSplitName(String camelName, String split) {
        if (camelName == null || camelName.length() == 0) {
            return camelName;
        }
        StringBuilder buf = null;
        for (int i = 0; i < camelName.length(); i++) {
            char ch = camelName.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                if (buf == null) {
                    buf = new StringBuilder();
                    if (i > 0) {
                        buf.append(camelName.substring(0, i));
                    }
                }
                if (i > 0) {
                    buf.append(split);
                }
                buf.append(Character.toLowerCase(ch));
            } else if (buf != null) {
                buf.append(ch);
            }
        }
        return buf == null ? camelName : buf.toString();
    }

    /**
     * To argument string string.
     *
     * @param args the args
     * @return the string
     */
    public static String toArgumentString(Object[] args) {
        StringBuilder buf = new StringBuilder();
        for (Object arg : args) {
            if (buf.length() > 0) {
                buf.append(Constants.COMMA_SEPARATOR);
            }
            if (arg == null || ReflectUtils.isPrimitives(arg.getClass())) {
                buf.append(arg);
            } else {
                buf.append(JSON.toJSON(arg));
            }
        }
        return buf.toString();
    }
}