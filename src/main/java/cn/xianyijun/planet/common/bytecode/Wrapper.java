package cn.xianyijun.planet.common.bytecode;

import cn.xianyijun.planet.exception.NoSuchPropertyException;
import cn.xianyijun.planet.utils.ClassHelper;
import cn.xianyijun.planet.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;

/**
 * The type Wrapper.
 * @author xianyijun
 */
public abstract class Wrapper {
    private static final Map<Class<?>, Wrapper> WRAPPER_MAP = new ConcurrentHashMap<>();
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String[] OBJECT_METHODS = new String[]{"getClass", "hashCode", "toString", "equals"};
    private static final Wrapper OBJECT_WRAPPER = new Wrapper() {
        @Override
        public String[] getMethodNames() {
            return OBJECT_METHODS;
        }

        @Override
        public String[] getDeclaredMethodNames() {
            return OBJECT_METHODS;
        }

        @Override
        public String[] getPropertyNames() {
            return EMPTY_STRING_ARRAY;
        }

        @Override
        public Class<?> getPropertyType(String pn) {
            return null;
        }

        @Override
        public Object getPropertyValue(Object instance, String pn) throws NoSuchPropertyException {
            throw new NoSuchPropertyException("Property [" + pn + "] not found.");
        }

        @Override
        public void setPropertyValue(Object instance, String pn, Object pv) throws NoSuchPropertyException {
            throw new NoSuchPropertyException("Property [" + pn + "] not found.");
        }

        @Override
        public boolean hasProperty(String name) {
            return false;
        }

        @Override
        public Object invokeMethod(Object instance, String mn, Class<?>[] types, Object[] args) throws NoSuchMethodException {
            if ("getClass".equals(mn)) {
                return instance.getClass();
            }
            if ("hashCode".equals(mn)) {
                return instance.hashCode();
            }
            if ("toString".equals(mn)) {
                return instance.toString();
            }
            if ("equals".equals(mn)) {
                if (args.length == 1) {
                    return instance.equals(args[0]);
                }
                throw new IllegalArgumentException("Invoke method [" + mn + "] argument number error.");
            }
            throw new NoSuchMethodException("Method [" + mn + "] not found.");
        }
    };
    private static AtomicLong WRAPPER_CLASS_COUNTER = new AtomicLong(0);

    /**
     * Gets wrapper.
     *
     * @param c the c
     * @return the wrapper
     */
    public static Wrapper getWrapper(Class<?> c) {
        while (ClassGenerator.isDynamicClass(c)) {
            c = c.getSuperclass();
        }

        if (c == Object.class) {
            return OBJECT_WRAPPER;
        }

        Wrapper ret = WRAPPER_MAP.get(c);
        if (ret == null) {
            ret = makeWrapper(c);
            WRAPPER_MAP.put(c, ret);
        }
        return ret;
    }

    private static Wrapper makeWrapper(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            throw new IllegalArgumentException("Can not create wrapper for primitive type: " + clazz);
        }

        String name = clazz.getName();
        ClassLoader cl = ClassHelper.getClassLoader(clazz);

        StringBuilder c1 = new StringBuilder("public void setPropertyValue(Object o, String n, Object v){ ");
        StringBuilder c2 = new StringBuilder("public Object getPropertyValue(Object o, String n){ ");
        StringBuilder c3 = new StringBuilder("public Object invokeMethod(Object o, String n, Class[] p, Object[] v) throws " + InvocationTargetException.class.getName() + "{ ");

        c1.append(name).append(" w; try{ w = ((").append(name).append(")$1); }catch(Throwable e){ throw new IllegalArgumentException(e); }");
        c2.append(name).append(" w; try{ w = ((").append(name).append(")$1); }catch(Throwable e){ throw new IllegalArgumentException(e); }");
        c3.append(name).append(" w; try{ w = ((").append(name).append(")$1); }catch(Throwable e){ throw new IllegalArgumentException(e); }");

        Map<String, Class<?>> propertiesNameTypes = new HashMap<String, Class<?>>();
        Map<String, Method> ms = new LinkedHashMap<>();
        List<String> methodNames = new ArrayList<>();
        List<String> declaringMethodNames = new ArrayList<>();

        for (Field f : clazz.getFields()) {
            String fn = f.getName();
            Class<?> ft = f.getType();
            if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) {
                continue;
            }

            c1.append(" if( $2.equals(\"").append(fn).append("\") ){ w.").append(fn).append("=").append(arg(ft, "$3")).append("; return; }");
            c2.append(" if( $2.equals(\"").append(fn).append("\") ){ return ($w)w.").append(fn).append("; }");
            propertiesNameTypes.put(fn, ft);
        }

        Method[] methods = clazz.getMethods();
        // get all public method.
        boolean hasMethod = hasMethods(methods);
        if (hasMethod) {
            c3.append(" try{");
        }
        for (Method m : methods) {
            if (m.getDeclaringClass() == Object.class) {
                continue;
            }

            String mn = m.getName();
            c3.append(" if( \"").append(mn).append("\".equals( $2 ) ");
            int len = m.getParameterTypes().length;
            c3.append(" && ").append(" $3.length == ").append(len);

            boolean override = false;
            for (Method m2 : methods) {
                if (m != m2 && m.getName().equals(m2.getName())) {
                    override = true;
                    break;
                }
            }
            if (override) {
                if (len > 0) {
                    for (int l = 0; l < len; l++) {
                        c3.append(" && ").append(" $3[").append(l).append("].getName().equals(\"")
                                .append(m.getParameterTypes()[l].getName()).append("\")");
                    }
                }
            }

            c3.append(" ) { ");

            if (m.getReturnType() == Void.TYPE) {
                c3.append(" w.").append(mn).append('(').append(args(m.getParameterTypes(), "$4")).append(");").append(" return null;");
            } else {
                c3.append(" return ($w)w.").append(mn).append('(').append(args(m.getParameterTypes(), "$4")).append(");");
            }

            c3.append(" }");

            methodNames.add(mn);
            if (m.getDeclaringClass() == clazz) {
                declaringMethodNames.add(mn);
            }
            ms.put(ReflectUtils.getDesc(m), m);
        }
        if (hasMethod) {
            c3.append(" } catch(Throwable e) { ");
            c3.append("     throw new java.lang.reflect.InvocationTargetException(e); ");
            c3.append(" }");
        }

        c3.append(" throw new ").append(NoSuchMethodException.class.getName()).append("(\"Not found method \\\"\"+$2+\"\\\" in class ").append(clazz.getName()).append(".\"); }");

        Matcher matcher;
        for (Map.Entry<String, Method> entry : ms.entrySet()) {
            String md = entry.getKey();
            Method method = entry.getValue();
            if ((matcher = ReflectUtils.GETTER_METHOD_DESC_PATTERN.matcher(md)).matches()) {
                String pn = propertyName(matcher.group(1));
                c2.append(" if( $2.equals(\"").append(pn).append("\") ){ return ($w)w.").append(method.getName()).append("(); }");
                propertiesNameTypes.put(pn, method.getReturnType());
            } else if ((matcher = ReflectUtils.IS_HAS_CAN_METHOD_DESC_PATTERN.matcher(md)).matches()) {
                String pn = propertyName(matcher.group(1));
                c2.append(" if( $2.equals(\"").append(pn).append("\") ){ return ($w)w.").append(method.getName()).append("(); }");
                propertiesNameTypes.put(pn, method.getReturnType());
            } else if ((matcher = ReflectUtils.SETTER_METHOD_DESC_PATTERN.matcher(md)).matches()) {
                Class<?> pt = method.getParameterTypes()[0];
                String pn = propertyName(matcher.group(1));
                c1.append(" if( $2.equals(\"").append(pn).append("\") ){ w.").append(method.getName()).append("(").append(arg(pt, "$3")).append("); return; }");
                propertiesNameTypes.put(pn, pt);
            }
        }
        c1.append(" throw new ").append(NoSuchPropertyException.class.getName()).append("(\"Not found property \\\"\"+$2+\"\\\" filed or setter method in class ").append(clazz.getName()).append(".\"); }");
        c2.append(" throw new ").append(NoSuchPropertyException.class.getName()).append("(\"Not found property \\\"\"+$2+\"\\\" filed or setter method in class ").append(clazz.getName()).append(".\"); }");

        long id = WRAPPER_CLASS_COUNTER.getAndIncrement();
        ClassGenerator cc = ClassGenerator.newInstance(cl);
        cc.setClassName((Modifier.isPublic(clazz.getModifiers()) ? Wrapper.class.getName() : clazz.getName() + "$sw") + id);
        cc.setSuperClass(Wrapper.class);

        cc.addDefaultConstructor();
        cc.addField("public static String[] pns;"); // property name array.
        cc.addField("public static " + Map.class.getName() + " pts;"); // property type map.
        cc.addField("public static String[] mns;"); // all method name array.
        cc.addField("public static String[] dmns;"); // declared method name array.
        for (int i = 0, len = ms.size(); i < len; i++) {
            cc.addField("public static Class[] mts" + i + ";");
        }

        cc.addMethod("public String[] getPropertyNames(){ return pns; }");
        cc.addMethod("public boolean hasProperty(String n){ return pts.containsKey($1); }");
        cc.addMethod("public Class getPropertyType(String n){ return (Class)pts.get($1); }");
        cc.addMethod("public String[] getMethodNames(){ return mns; }");
        cc.addMethod("public String[] getDeclaredMethodNames(){ return dmns; }");
        cc.addMethod(c1.toString());
        cc.addMethod(c2.toString());
        cc.addMethod(c3.toString());

        try {
            Class<?> wc = cc.toClass();
            wc.getField("pts").set(null, propertiesNameTypes);
            wc.getField("pns").set(null, propertiesNameTypes.keySet().toArray(new String[0]));
            wc.getField("mns").set(null, methodNames.toArray(new String[0]));
            wc.getField("dmns").set(null, declaringMethodNames.toArray(new String[0]));
            int ix = 0;
            for (Method m : ms.values()) {
                wc.getField("mts" + ix++).set(null, m.getParameterTypes());
            }
            return (Wrapper) wc.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            cc.release();
            ms.clear();
            methodNames.clear();
            declaringMethodNames.clear();
        }
    }

    private static String arg(Class<?> cl, String name) {
        if (cl.isPrimitive()) {
            if (cl == Boolean.TYPE) {
                return "((Boolean)" + name + ").booleanValue()";
            }
            if (cl == Byte.TYPE) {
                return "((Byte)" + name + ").byteValue()";
            }
            if (cl == Character.TYPE) {
                return "((Character)" + name + ").charValue()";
            }
            if (cl == Double.TYPE) {
                return "((Number)" + name + ").doubleValue()";
            }
            if (cl == Float.TYPE) {
                return "((Number)" + name + ").floatValue()";
            }
            if (cl == Integer.TYPE) {
                return "((Number)" + name + ").intValue()";
            }
            if (cl == Long.TYPE) {
                return "((Number)" + name + ").longValue()";
            }
            if (cl == Short.TYPE) {
                return "((Number)" + name + ").shortValue()";
            }
            throw new RuntimeException("Unknown primitive type: " + cl.getName());
        }
        return "(" + ReflectUtils.getName(cl) + ")" + name;
    }

    private static String args(Class<?>[] cs, String name) {
        int len = cs.length;
        if (len == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(arg(cs[i], name + "[" + i + "]"));
        }
        return sb.toString();
    }

    private static String propertyName(String pn) {
        return pn.length() == 1 || Character.isLowerCase(pn.charAt(1)) ? Character.toLowerCase(pn.charAt(0)) + pn.substring(1) : pn;
    }

    private static boolean hasMethods(Method[] methods) {
        if (methods == null || methods.length == 0) {
            return false;
        }
        for (Method m : methods) {
            if (m.getDeclaringClass() != Object.class) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get property names string [ ].
     *
     * @return the string [ ]
     */
    abstract public String[] getPropertyNames();

    /**
     * Gets property type.
     *
     * @param pn the pn
     * @return the property type
     */
    abstract public Class<?> getPropertyType(String pn);

    /**
     * Has property boolean.
     *
     * @param name the name
     * @return the boolean
     */
    abstract public boolean hasProperty(String name);

    /**
     * Gets property value.
     *
     * @param instance the instance
     * @param pn       the pn
     * @return the property value
     * @throws NoSuchPropertyException  the no such property exception
     * @throws IllegalArgumentException the illegal argument exception
     */
    abstract public Object getPropertyValue(Object instance, String pn) throws NoSuchPropertyException, IllegalArgumentException;

    /**
     * Sets property value.
     *
     * @param instance the instance
     * @param pn       the pn
     * @param pv       the pv
     * @throws NoSuchPropertyException  the no such property exception
     * @throws IllegalArgumentException the illegal argument exception
     */
    abstract public void setPropertyValue(Object instance, String pn, Object pv) throws NoSuchPropertyException, IllegalArgumentException;

    /**
     * Get property values object [ ].
     *
     * @param instance the instance
     * @param pns      the pns
     * @return the object [ ]
     * @throws NoSuchPropertyException  the no such property exception
     * @throws IllegalArgumentException the illegal argument exception
     */
    public Object[] getPropertyValues(Object instance, String[] pns) throws NoSuchPropertyException, IllegalArgumentException {
        Object[] ret = new Object[pns.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = getPropertyValue(instance, pns[i]);
        }
        return ret;
    }

    /**
     * Sets property values.
     *
     * @param instance the instance
     * @param pns      the pns
     * @param pvs      the pvs
     * @throws NoSuchPropertyException  the no such property exception
     * @throws IllegalArgumentException the illegal argument exception
     */
    public void setPropertyValues(Object instance, String[] pns, Object[] pvs) throws NoSuchPropertyException, IllegalArgumentException {
        if (pns.length != pvs.length) {
            throw new IllegalArgumentException("pns.length != pvs.length");
        }

        for (int i = 0; i < pns.length; i++) {
            setPropertyValue(instance, pns[i], pvs[i]);
        }
    }

    /**
     * Get method names string [ ].
     *
     * @return the string [ ]
     */
    abstract public String[] getMethodNames();

    /**
     * Get declared method names string [ ].
     *
     * @return the string [ ]
     */
    abstract public String[] getDeclaredMethodNames();

    /**
     * Has method boolean.
     *
     * @param name the name
     * @return the boolean
     */
    public boolean hasMethod(String name) {
        for (String mn : getMethodNames()) {
            if (mn.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Invoke method object.
     *
     * @param instance the instance
     * @param mn       the mn
     * @param types    the types
     * @param args     the args
     * @return the object
     * @throws NoSuchMethodException     the no such method exception
     * @throws InvocationTargetException the invocation target exception
     */
    abstract public Object invokeMethod(Object instance, String mn, Class<?>[] types, Object[] args) throws NoSuchMethodException, InvocationTargetException;
}
