package cn.xianyijun.planet.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by xianyijun on 2018/1/21.
 */
public class ClassUtils {

    private ClassUtils() {
    }

    /**
     * To string string.
     *
     * @param e the e
     * @return the string
     */
    public static String toString(Throwable e) {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName() + ": ");
        if (e.getMessage() != null) {
            p.print(e.getMessage() + "\n");
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
     * For name class.
     *
     * @param packages  the packages
     * @param className the class name
     * @return the class
     */
    public static Class<?> forName(String[] packages, String className) {
        try {
            return _forName(className);
        } catch (ClassNotFoundException e) {
            if (packages != null && packages.length > 0) {
                for (String pkg : packages) {
                    try {
                        return _forName(pkg + "." + className);
                    } catch (ClassNotFoundException e2) {
                    }
                }
            }
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * For name class.
     *
     * @param className the class name
     * @return the class
     * @throws ClassNotFoundException the class not found exception
     */
    public static Class<?> _forName(String className) throws ClassNotFoundException {
        if ("boolean".equals(className)){
            return boolean.class;
        }
        if ("byte".equals(className)) {
            return byte.class;
        }
        if ("char".equals(className)) {
            return char.class;
        }
        if ("short".equals(className)) {
            return short.class;
        }
        if ("int".equals(className)) {
            return int.class;
        }
        if ("long".equals(className)) {
            return long.class;
        }
        if ("float".equals(className)) {
            return float.class;
        }
        if ("double".equals(className)) {
            return double.class;
        }
        if ("boolean[]".equals(className)) {
            return boolean[].class;
        }
        if ("byte[]".equals(className)) {
            return byte[].class;
        }
        if ("char[]".equals(className)) {
            return char[].class;
        }
        if ("short[]".equals(className)) {
            return short[].class;
        }
        if ("int[]".equals(className)) {
            return int[].class;
        }
        if ("long[]".equals(className)) {
            return long[].class;
        }
        if ("float[]".equals(className)) {
            return float[].class;
        }
        if ("double[]".equals(className)) {
            return double[].class;
        }
        try {
            return arrayForName(className);
        } catch (ClassNotFoundException e) {
            if (className.indexOf('.') == -1) {
                try {
                    return arrayForName("java.lang." + className);
                } catch (ClassNotFoundException e2) {
                    // ignore, let the original exception be thrown
                }
            }
            throw e;
        }
    }
    private static Class<?> arrayForName(String className) throws ClassNotFoundException {
        return Class.forName(className.endsWith("[]")
                ? "[L" + className.substring(0, className.length() - 2) + ";"
                : className, true, Thread.currentThread().getContextClassLoader());
    }

}
