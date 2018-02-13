package cn.xianyijun.planet.utils;

import cn.xianyijun.planet.rpc.rpc.RpcCodec;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Reflect utils.
 */
public class ReflectUtils {
    /**
     * The constant JAVA_IDENT_REGEX.
     */
    public static final String JAVA_IDENT_REGEX = "(?:[_$a-zA-Z][_$a-zA-Z0-9]*)";

    /**
     * The constant CLASS_DESC.
     */
    public static final String CLASS_DESC = "(?:L" + JAVA_IDENT_REGEX + "(?:\\/" + JAVA_IDENT_REGEX + ")*;)";

    /**
     * The constant ARRAY_DESC.
     */
    public static final String ARRAY_DESC = "(?:\\[+(?:(?:[VZBCDFIJS])|" + CLASS_DESC + "))";

    /**
     * The constant DESC_REGEX.
     */
    public static final String DESC_REGEX = "(?:(?:[VZBCDFIJS])|" + CLASS_DESC + "|" + ARRAY_DESC + ")";

    /**
     * The constant GETTER_METHOD_DESC_PATTERN.
     */
    public static final Pattern GETTER_METHOD_DESC_PATTERN = Pattern.compile("get([A-Z][_a-zA-Z0-9]*)\\(\\)(" + DESC_REGEX + ")");

    /**
     * The constant SETTER_METHOD_DESC_PATTERN.
     */
    public static final Pattern SETTER_METHOD_DESC_PATTERN = Pattern.compile("set([A-Z][_a-zA-Z0-9]*)\\((" + DESC_REGEX + ")\\)V");

    /**
     * The constant IS_HAS_CAN_METHOD_DESC_PATTERN.
     */
    public static final Pattern IS_HAS_CAN_METHOD_DESC_PATTERN = Pattern.compile("(?:is|has|can)([A-Z][_a-zA-Z0-9]*)\\(\\)Z");

    public static final Pattern DESC_PATTERN = Pattern.compile(DESC_REGEX);

    /**
     * The constant JVM_VOID.
     */
    public static final char JVM_VOID = 'V';

    /**
     * The constant JVM_BOOLEAN.
     */
    public static final char JVM_BOOLEAN = 'Z';

    /**
     * The constant JVM_BYTE.
     */
    public static final char JVM_BYTE = 'B';

    /**
     * The constant JVM_CHAR.
     */
    public static final char JVM_CHAR = 'C';

    /**
     * The constant JVM_DOUBLE.
     */
    public static final char JVM_DOUBLE = 'D';

    /**
     * The constant JVM_FLOAT.
     */
    public static final char JVM_FLOAT = 'F';

    /**
     * The constant JVM_INT.
     */
    public static final char JVM_INT = 'I';

    /**
     * The constant JVM_LONG.
     */
    public static final char JVM_LONG = 'J';

    /**
     * The constant JVM_SHORT.
     */
    public static final char JVM_SHORT = 'S';

    private static final ConcurrentMap<String, Class<?>> DESC_CLASS_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Method> Signature_METHODS_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Class<?>> NAME_CLASS_CACHE = new ConcurrentHashMap<>();

    /**
     * Is primitives boolean.
     *
     * @param cls the cls
     * @return the boolean
     */
    public static boolean isPrimitives(Class<?> cls) {
        if (cls.isArray()) {
            return isPrimitive(cls.getComponentType());
        }
        return isPrimitive(cls);
    }

    /**
     * Is primitive boolean.
     *
     * @param cls the cls
     * @return the boolean
     */
    public static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() || cls == String.class || cls == Boolean.class || cls == Character.class
                || Number.class.isAssignableFrom(cls) || Date.class.isAssignableFrom(cls);
    }

    /**
     * Gets desc.
     *
     * @param c the c
     * @return the desc
     */
    public static String getDesc(Class<?> c) {
        StringBuilder ret = new StringBuilder();

        while (c.isArray()) {
            ret.append('[');
            c = c.getComponentType();
        }

        if (c.isPrimitive()) {
            String t = c.getName();
            if ("void".equals(t)) {
                ret.append(JVM_VOID);
            } else if ("boolean".equals(t)) {
                ret.append(JVM_BOOLEAN);
            } else if ("byte".equals(t)) {
                ret.append(JVM_BYTE);
            } else if ("char".equals(t)) {
                ret.append(JVM_CHAR);
            } else if ("double".equals(t)) {
                ret.append(JVM_DOUBLE);
            } else if ("float".equals(t)) {
                ret.append(JVM_FLOAT);
            } else if ("int".equals(t)) {
                ret.append(JVM_INT);
            } else if ("long".equals(t)) {
                ret.append(JVM_LONG);
            } else if ("short".equals(t)) {
                ret.append(JVM_SHORT);
            }
        } else {
            ret.append('L');
            ret.append(c.getName().replace('.', '/'));
            ret.append(';');
        }
        return ret.toString();
    }

    /**
     * Gets desc.
     *
     * @param m the m
     * @return the desc
     */
    public static String getDesc(final Method m) {
        StringBuilder ret = new StringBuilder(m.getName()).append('(');
        Class<?>[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            ret.append(getDesc(parameterTypes[i]));
        }
        ret.append(')').append(getDesc(m.getReturnType()));
        return ret.toString();
    }

    /**
     * Gets desc.
     *
     * @param c the c
     * @return the desc
     */
    public static String getDesc(final Constructor<?> c) {
        StringBuilder ret = new StringBuilder("(");
        Class<?>[] parameterTypes = c.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            ret.append(getDesc(parameterTypes[i]));
        }
        ret.append(')').append('V');
        return ret.toString();
    }


    /**
     * Desc 2 class class.
     *
     * @param desc the desc
     * @return the class
     * @throws ClassNotFoundException the class not found exception
     */
    public static Class<?> desc2class(String desc) throws ClassNotFoundException {
        return desc2class(ClassHelper.getClassLoader(), desc);
    }

    private static Class<?> desc2class(ClassLoader cl, String desc) throws ClassNotFoundException {
        switch (desc.charAt(0)) {
            case JVM_VOID:
                return void.class;
            case JVM_BOOLEAN:
                return boolean.class;
            case JVM_BYTE:
                return byte.class;
            case JVM_CHAR:
                return char.class;
            case JVM_DOUBLE:
                return double.class;
            case JVM_FLOAT:
                return float.class;
            case JVM_INT:
                return int.class;
            case JVM_LONG:
                return long.class;
            case JVM_SHORT:
                return short.class;
            case 'L':
                desc = desc.substring(1, desc.length() - 1).replace('/', '.');
                break;
            case '[':
                desc = desc.replace('/', '.');
                break;
            default:
                throw new ClassNotFoundException("Class not found: " + desc);
        }

        if (cl == null) {
            cl = ClassHelper.getClassLoader();
        }
        Class<?> clazz = DESC_CLASS_CACHE.get(desc);
        if (clazz == null) {
            clazz = Class.forName(desc, true, cl);
            DESC_CLASS_CACHE.put(desc, clazz);
        }
        return clazz;
    }


    /**
     * Gets name.
     *
     * @param c the c
     * @return the name
     */
    public static String getName(Class<?> c) {
        if (c.isArray()) {
            StringBuilder sb = new StringBuilder();
            do {
                sb.append("[]");
                c = c.getComponentType();
            }
            while (c.isArray());

            return c.getName() + sb.toString();
        }
        return c.getName();
    }

    /**
     * Gets name.
     *
     * @param m the m
     * @return the name
     */
    public static String getName(final Method m) {
        StringBuilder ret = new StringBuilder();
        ret.append(getName(m.getReturnType())).append(' ');
        ret.append(m.getName()).append('(');
        Class<?>[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                ret.append(',');
            }
            ret.append(getName(parameterTypes[i]));
        }
        ret.append(')');
        return ret.toString();
    }

    /**
     * Gets desc without method name.
     *
     * @param m the m
     * @return the desc without method name
     */
    public static String getDescWithoutMethodName(Method m) {
        StringBuilder ret = new StringBuilder();
        ret.append('(');
        Class<?>[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++){
            ret.append(getDesc(parameterTypes[i]));
        }
        ret.append(')').append(getDesc(m.getReturnType()));
        return ret.toString();
    }

    /**
     * For name class.
     *
     * @param name the name
     * @return the class
     */
    public static Class<?> forName(String name) {
        try {
            return name2class(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Not found class " + name + ", cause: " + e.getMessage(), e);
        }
    }

    /**
     * Name 2 class class.
     *
     * @param name the name
     * @return the class
     * @throws ClassNotFoundException the class not found exception
     */
    public static Class<?> name2class(String name) throws ClassNotFoundException {
        return name2class(ClassHelper.getClassLoader(), name);
    }

    private static Class<?> name2class(ClassLoader cl, String name) throws ClassNotFoundException {
        int c = 0, index = name.indexOf('[');
        if (index > 0) {
            c = (name.length() - index) / 2;
            name = name.substring(0, index);
        }
        if (c > 0) {
            StringBuilder sb = new StringBuilder();
            while (c-- > 0) {
                sb.append("[");
            }

            if ("void".equals(name)) {
                sb.append(JVM_VOID);
            } else if ("boolean".equals(name)) {
                sb.append(JVM_BOOLEAN);
            } else if ("byte".equals(name)) {
                sb.append(JVM_BYTE);
            } else if ("char".equals(name)) {
                sb.append(JVM_CHAR);
            } else if ("double".equals(name)) {
                sb.append(JVM_DOUBLE);
            } else if ("float".equals(name)) {
                sb.append(JVM_FLOAT);
            } else if ("int".equals(name)) {
                sb.append(JVM_INT);
            } else if ("long".equals(name)) {
                sb.append(JVM_LONG);
            } else if ("short".equals(name)) {
                sb.append(JVM_SHORT);
            } else {
                sb.append('L').append(name).append(';'); // "java.lang.Object" ==> "Ljava.lang.Object;"
            }
            name = sb.toString();
        } else {
            if ("void".equals(name)) {
                return void.class;
            } else if ("boolean".equals(name)) {
                return boolean.class;
            } else if ("byte".equals(name)) {
                return byte.class;
            } else if ("char".equals(name)) {
                return char.class;
            } else if ("double".equals(name)) {
                return double.class;
            } else if ("float".equals(name)) {
                return float.class;
            } else if ("int".equals(name)) {
                return int.class;
            } else if ("long".equals(name)) {
                return long.class;
            } else if ("short".equals(name)) {
                return short.class;
            }
        }

        if (cl == null) {
            cl = ClassHelper.getClassLoader();
        }
        Class<?> clazz = NAME_CLASS_CACHE.get(name);
        if (clazz == null) {
            clazz = Class.forName(name, true, cl);
            NAME_CLASS_CACHE.put(name, clazz);
        }
        return clazz;
    }

    /**
     * Find constructor constructor.
     *
     * @param clazz     the clazz
     * @param paramType the param type
     * @return the constructor
     * @throws NoSuchMethodException the no such method exception
     */
    public static Constructor<?> findConstructor(Class<?> clazz, Class<?> paramType) throws NoSuchMethodException {
        Constructor<?> targetConstructor;
        try {
            targetConstructor = clazz.getConstructor(new Class<?>[]{paramType});
        } catch (NoSuchMethodException e) {
            targetConstructor = null;
            Constructor<?>[] constructors = clazz.getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (Modifier.isPublic(constructor.getModifiers())
                        && constructor.getParameterTypes().length == 1
                        && constructor.getParameterTypes()[0].isAssignableFrom(paramType)) {
                    targetConstructor = constructor;
                    break;
                }
            }
            if (targetConstructor == null) {
                throw e;
            }
        }
        return targetConstructor;
    }


    /**
     * Find method by method name method.
     *
     * @param clazz      the clazz
     * @param methodName the method name
     * @return the method
     * @throws NoSuchMethodException  the no such method exception
     * @throws ClassNotFoundException the class not found exception
     */
    public static Method findMethodByMethodName(Class<?> clazz, String methodName)
            throws NoSuchMethodException, ClassNotFoundException {
        return findMethodByMethodSignature(clazz, methodName, null);
    }

    /**
     * Find method by method signature method.
     *
     * @param clazz          the clazz
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @return the method
     * @throws NoSuchMethodException  the no such method exception
     * @throws ClassNotFoundException the class not found exception
     */
    public static Method findMethodByMethodSignature(Class<?> clazz, String methodName, String[] parameterTypes)
            throws NoSuchMethodException, ClassNotFoundException {
        String signature = clazz.getName() + "." + methodName;
        if (parameterTypes != null && parameterTypes.length > 0) {
            signature += StringUtils.join(parameterTypes);
        }
        Method method = Signature_METHODS_CACHE.get(signature);
        if (method != null) {
            return method;
        }
        if (parameterTypes == null) {
            List<Method> finded = new ArrayList<Method>();
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(methodName)) {
                    finded.add(m);
                }
            }
            if (finded.isEmpty()) {
                throw new NoSuchMethodException("No such method " + methodName + " in class " + clazz);
            }
            if (finded.size() > 1) {
                String msg = String.format("Not unique method for method name(%s) in class(%s), find %d methods.",
                        methodName, clazz.getName(), finded.size());
                throw new IllegalStateException(msg);
            }
            method = finded.get(0);
        } else {
            Class<?>[] types = new Class<?>[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                types[i] = ReflectUtils.name2class(parameterTypes[i]);
            }
            method = clazz.getMethod(methodName, types);

        }
        Signature_METHODS_CACHE.put(signature, method);
        return method;
    }

    public static Class<?>[] desc2classArray(String desc) throws ClassNotFoundException {
        Class<?>[] ret = desc2classArray(ClassHelper.getClassLoader(), desc);
        return ret;
    }

    private static Class<?>[] desc2classArray(ClassLoader cl, String desc) throws ClassNotFoundException {
        if (desc.length() == 0) {
            return RpcCodec.EMPTY_CLASS_ARRAY;
        }
        List<Class<?>> cs = new ArrayList<Class<?>>();
        Matcher m = DESC_PATTERN.matcher(desc);
        while (m.find()){
            cs.add(desc2class(cl, m.group()));
        }
        return cs.toArray(RpcCodec.EMPTY_CLASS_ARRAY);
    }

    public static String getDesc(final Class<?>[] cs) {
        if (cs.length == 0){
            return "";
        }

        StringBuilder sb = new StringBuilder(64);
        for (Class<?> c : cs){
            sb.append(getDesc(c));
        }
        return sb.toString();
    }

    public static Class<?> getBoxedClass(Class<?> c) {
        if (c == int.class)
            c = Integer.class;
        else if (c == boolean.class)
            c = Boolean.class;
        else if (c == long.class)
            c = Long.class;
        else if (c == float.class)
            c = Float.class;
        else if (c == double.class)
            c = Double.class;
        else if (c == char.class)
            c = Character.class;
        else if (c == byte.class)
            c = Byte.class;
        else if (c == short.class)
            c = Short.class;
        return c;
    }


}
