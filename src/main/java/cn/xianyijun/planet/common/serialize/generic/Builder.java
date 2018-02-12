package cn.xianyijun.planet.common.serialize.generic;

import cn.xianyijun.planet.common.bytecode.ClassGenerator;
import cn.xianyijun.planet.common.io.UnsafeByteArrayInputStream;
import cn.xianyijun.planet.common.io.UnsafeByteArrayOutputStream;
import cn.xianyijun.planet.common.serialize.generic.support.ClassDescriptorMapper;
import cn.xianyijun.planet.common.serialize.generic.support.java.CompactedObjectInputStream;
import cn.xianyijun.planet.common.serialize.generic.support.java.CompactedObjectOutputStream;
import cn.xianyijun.planet.utils.ClassHelper;
import cn.xianyijun.planet.utils.IOUtils;
import cn.xianyijun.planet.utils.ReflectUtils;
import cn.xianyijun.planet.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;

/**
 * The type Builder.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class Builder<T> implements GenericDataFlags {
    private static final List<String> descList = new ArrayList<>();

    private static final Map<String, Integer> descMap = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Builder<?>> BuilderMap = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Builder<?>> nonSerializableBuilderMap = new ConcurrentHashMap<Class<?>, Builder<?>>();

    private static final AtomicLong BUILDER_CLASS_COUNTER = new AtomicLong(0);

    private static final String BUILDER_CLASS_NAME = Builder.class.getName();

    private static final String FIELD_CONFIG_SUFFIX = ".fc";

    private static final int MAX_FIELD_CONFIG_FILE_SIZE = 16 * 1024;

    /**
     * Gets type.
     *
     * @return the type
     */
    abstract public Class<T> getType();

    /**
     * Write to.
     *
     * @param obj the obj
     * @param out the out
     * @throws IOException the io exception
     */
    abstract public void writeTo(T obj, GenericObjectOutput out) throws IOException;

    /**
     * Parse from t.
     *
     * @param in the in
     * @return the t
     * @throws IOException the io exception
     */
    abstract public T parseFrom(GenericObjectInput in) throws IOException;

    /**
     * The Serializable builder.
     */
    static final Builder<Serializable> SerializableBuilder = new Builder<Serializable>() {
        @Override
        public Class<Serializable> getType() {
            return Serializable.class;
        }

        @Override
        public void writeTo(Serializable obj, GenericObjectOutput out) throws IOException {
            if (obj == null) {
                out.write0(OBJECT_NULL);
            } else {
                out.write0(OBJECT_STREAM);
                UnsafeByteArrayOutputStream bos = null;
                CompactedObjectOutputStream oos = null;
                try {
                    bos = new UnsafeByteArrayOutputStream();
                    oos = new CompactedObjectOutputStream(bos);
                    oos.writeObject(obj);
                    oos.flush();
                    bos.close();
                    byte[] b = bos.toByteArray();
                    out.writeUInt(b.length);
                    out.write0(b, 0, b.length);
                }finally {
                    bos.close();
                    oos.close();
                }
            }
        }

        @Override
        public Serializable parseFrom(GenericObjectInput in) throws IOException {
            byte b = in.read0();
            if (b == OBJECT_NULL) {
                return null;
            }
            if (b != OBJECT_STREAM) {
                throw new IOException("Input format error, expect OBJECT_NULL|OBJECT_STREAM, get " + b + ".");
            }

            UnsafeByteArrayInputStream bis = null;
            CompactedObjectInputStream ois = null;
            try {
                bis = new UnsafeByteArrayInputStream(in.read0(in.readUInt()));
                ois =new CompactedObjectInputStream(bis);
                return (Serializable) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException(StringUtils.toString(e));
            }finally {
                ois.close();
                bis.close();
            }
        }
    };

    /**
     * The constant DEFAULT_CLASS_DESCRIPTOR_MAPPER.
     */
    public static ClassDescriptorMapper DEFAULT_CLASS_DESCRIPTOR_MAPPER = new ClassDescriptorMapper(){

        @Override
        public String getDescriptor(int index) {
            if (index < 0 || index >= descList.size()) {
                return null;
            }
            return descList.get(index);
        }

        @Override
        public int getDescriptorIndex(String desc) {
            Integer ret = descMap.get(desc);
            return ret == null ? -1 : ret.intValue();
        }
    };

    /**
     * The Generic builder.
     */
    static final Builder<Object> GenericBuilder = new Builder<Object>() {
        @Override
        public Class<Object> getType() {
            return Object.class;
        }

        @Override
        public void writeTo(Object obj, GenericObjectOutput out) throws IOException {
            out.writeObject(obj);
        }

        @Override
        public Object parseFrom(GenericObjectInput in) throws IOException {
            return in.readObject();
        }
    };

    /**
     * The Generic array builder.
     */
    static final Builder<Object[]> GenericArrayBuilder = new AbstractObjectBuilder<Object[]>() {
        @Override
        public Class<Object[]> getType() {
            return Object[].class;
        }

        @Override
        protected Object[] newInstance(GenericObjectInput in) throws IOException {
            return new Object[in.readUInt()];
        }

        @Override
        protected void readObject(Object[] ret, GenericObjectInput in) throws IOException {
            for (int i = 0; i < ret.length; i++) {
                ret[i] = in.readObject();
            }
        }

        @Override
        protected void writeObject(Object[] obj, GenericObjectOutput out) throws IOException {
            out.writeUInt(obj.length);
            for (Object item : obj) {
                out.writeObject(item);
            }
        }
    };

    private static final Comparator<Field> FC = (f1, f2) -> compareFieldName(f1.getName(), f2.getName());

    private static final Comparator<Constructor> CC = (o1, o2) -> o1.getParameterTypes().length - o2.getParameterTypes().length;

    private static final Comparator<String> FNC = (n1, n2) -> compareFieldName(n1, n2);

    private static int compareFieldName(String n1, String n2) {
        int l = Math.min(n1.length(), n2.length());
        for (int i = 0; i < l; i++) {
            int t = n1.charAt(i) - n2.charAt(i);
            if (t != 0) {
                return t;
            }
        }
        return n1.length() - n2.length();
    }

    //========================== register =======================

    /**
     * Register builder.
     *
     * @param <T> the type parameter
     * @param c   the c
     * @return the builder
     */
    public static <T> Builder<T> register(Class<T> c) {
        return register(c, false);
    }

    /**
     * Register.
     *
     * @param <T> the type parameter
     * @param c   the c
     * @param b   the b
     */
    public static <T> void register(Class<T> c, Builder<T> b) {
        if (Serializable.class.isAssignableFrom(c)) {
            BuilderMap.put(c, b);
        } else {
            nonSerializableBuilderMap.put(c, b);
        }
    }

    /**
     * Register builder.
     *
     * @param <T>                    the type parameter
     * @param c                      the c
     * @param isAllowNonSerializable the is allow non serializable
     * @return the builder
     */
    public static <T> Builder<T> register(Class<T> c, boolean isAllowNonSerializable) {
        if (c == Object.class || c.isInterface()) {
            return (Builder<T>) GenericBuilder;
        }
        if (c == Object[].class) {
            return (Builder<T>) GenericArrayBuilder;
        }

        Builder<T> b = (Builder<T>) BuilderMap.get(c);
        if (null != b) {
            return b;
        }

        boolean isSerializable = Serializable.class.isAssignableFrom(c);
        if (!isAllowNonSerializable && !isSerializable) {
            throw new IllegalStateException("Serialized class " + c.getName() +
                    " must implement java.io.Serializable (rpc codec setting: isAllowNonSerializable = false)");
        }

        b = (Builder<T>) nonSerializableBuilderMap.get(c);
        if (null != b) {
            return b;
        }

        b = newBuilder(c);
        if (isSerializable) {
            BuilderMap.put(c, b);
        } else {
            nonSerializableBuilderMap.put(c, b);
        }
        return b;
    }

    private static <T> Builder<T> newBuilder(Class<T> c) {
        if (c.isPrimitive()) {
            throw new RuntimeException("Can not create builder for primitive type: " + c);
        }

        if (log.isInfoEnabled()) {
            log.info("create Builder for class: " + c);
        }

        Builder<?> builder;
        if (c.isArray()) {
            builder = newArrayBuilder(c);
        } else {
            builder = newObjectBuilder(c);
        }
        return (Builder<T>) builder;
    }

    private static Builder<?> newArrayBuilder(Class<?> c) {
        Class<?> cc = c.getComponentType();
        if (cc.isInterface()) {
            return GenericArrayBuilder;
        }

        ClassLoader cl = ClassHelper.getCallerClassLoader(Builder.class);

        String cn = ReflectUtils.getName(c), ccn = ReflectUtils.getName(cc); // get class name as int[][], double[].
        String bcn = BUILDER_CLASS_NAME + "$bc" + BUILDER_CLASS_COUNTER.getAndIncrement();

        int ix = cn.indexOf(']');
        String s1 = cn.substring(0, ix), s2 = cn.substring(ix); // if name='int[][]' then s1='int[', s2='][]'

        StringBuilder cwt = new StringBuilder("public void writeTo(Object obj, ").append(GenericObjectOutput.class.getName()).append(" out) throws java.io.IOException{"); // writeTo code.
        StringBuilder cpf = new StringBuilder("public Object parseFrom(").append(GenericObjectInput.class.getName()).append(" in) throws java.io.IOException{"); // parseFrom code.

        cwt.append("if( $1 == null ){ $2.write0(OBJECT_NULL); return; }");
        cwt.append(cn).append(" v = (").append(cn).append(")$1; int len = v.length; $2.write0(OBJECT_VALUES); $2.writeUInt(len); for(int i=0;i<len;i++){ ");

        cpf.append("byte b = $1.read0(); if( b == OBJECT_NULL ) return null; if( b != OBJECT_VALUES ) throw new java.io.IOException(\"Input format error, expect OBJECT_NULL|OBJECT_VALUES, get \" + b + \".\");");
        cpf.append("int len = $1.readUInt(); if( len == 0 ) return new ").append(s1).append('0').append(s2).append("; ");
        cpf.append(cn).append(" ret = new ").append(s1).append("len").append(s2).append("; for(int i=0;i<len;i++){ ");

        Builder<?> builder = null;
        if (cc.isPrimitive()) {
            if (cc == boolean.class) {
                cwt.append("$2.writeBool(v[i]);");
                cpf.append("ret[i] = $1.readBool();");
            } else if (cc == byte.class) {
                cwt.append("$2.writeByte(v[i]);");
                cpf.append("ret[i] = $1.readByte();");
            } else if (cc == char.class) {
                cwt.append("$2.writeShort((short)v[i]);");
                cpf.append("ret[i] = (char)$1.readShort();");
            } else if (cc == short.class) {
                cwt.append("$2.writeShort(v[i]);");
                cpf.append("ret[i] = $1.readShort();");
            } else if (cc == int.class) {
                cwt.append("$2.writeInt(v[i]);");
                cpf.append("ret[i] = $1.readInt();");
            } else if (cc == long.class) {
                cwt.append("$2.writeLong(v[i]);");
                cpf.append("ret[i] = $1.readLong();");
            } else if (cc == float.class) {
                cwt.append("$2.writeFloat(v[i]);");
                cpf.append("ret[i] = $1.readFloat();");
            } else if (cc == double.class) {
                cwt.append("$2.writeDouble(v[i]);");
                cpf.append("ret[i] = $1.readDouble();");
            }
        } else {
            builder = register(cc);

            cwt.append("builder.writeTo(v[i], $2);");
            cpf.append("ret[i] = (").append(ccn).append(")builder.parseFrom($1);");
        }
        cwt.append(" } }");
        cpf.append(" } return ret; }");

        ClassGenerator cg = ClassGenerator.newInstance(cl);
        cg.setClassName(bcn);
        cg.setSuperClass(Builder.class);
        cg.addDefaultConstructor();
        if (builder != null) {
            cg.addField("public static " + BUILDER_CLASS_NAME + " builder;");
        }
        cg.addMethod("public Class getType(){ return " + cn + ".class; }");
        cg.addMethod(cwt.toString());
        cg.addMethod(cpf.toString());
        try {
            Class<?> wc = cg.toClass();
            // set static field.
            if (builder != null) {
                wc.getField("builder").set(null, builder);
            }
            return (Builder<?>) wc.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            cg.release();
        }
    }

    private static Builder<?> newObjectBuilder(final Class<?> c) {
        if (c.isEnum()) {
            return newEnumBuilder(c);
        }

        if (c.isAnonymousClass()) {
            throw new RuntimeException("Can not instantiation anonymous class: " + c);
        }

        if (c.getEnclosingClass() != null && !Modifier.isStatic(c.getModifiers())) {
            throw new RuntimeException("Can not instantiation inner and non-static class: " + c);
        }

        if (Throwable.class.isAssignableFrom(c)) {
            return SerializableBuilder;
        }

        ClassLoader cl = ClassHelper.getCallerClassLoader(Builder.class);

        // is same package.
        boolean isp;
        String cn = c.getName(), bcn;
        if (c.getClassLoader() == null) // is system class. if( cn.startsWith("java.") || cn.startsWith("javax.") || cn.startsWith("sun.") )
        {
            isp = false;
            bcn = BUILDER_CLASS_NAME + "$bc" + BUILDER_CLASS_COUNTER.getAndIncrement();
        } else {
            isp = true;
            bcn = cn + "$bc" + BUILDER_CLASS_COUNTER.getAndIncrement();
        }

        // is Collection, is Map, is Serializable.
        boolean isc = Collection.class.isAssignableFrom(c);
        boolean ism = !isc && Map.class.isAssignableFrom(c);
        boolean iss = !(isc || ism) && Serializable.class.isAssignableFrom(c);

        // deal with fields.
        String[] fns = null; // fix-order fields names
        InputStream is = c.getResourceAsStream(c.getSimpleName() + FIELD_CONFIG_SUFFIX); // load field-config file.
        if (is != null) {
            try {
                int len = is.available();
                if (len > 0) {
                    if (len > MAX_FIELD_CONFIG_FILE_SIZE) {
                        throw new RuntimeException("Load [" + c.getName() + "] field-config file error: File-size too larger");
                    }

                    String[] lines = IOUtils.readLines(is);
                    if (lines != null && lines.length > 0) {
                        List<String> list = new ArrayList<String>();
                        for (int i = 0; i < lines.length; i++) {
                            fns = lines[i].split(",");
                            Arrays.sort(fns, FNC);
                            for (int j = 0; j < fns.length; j++) {
                                list.add(fns[j]);
                            }
                        }
                        fns = list.toArray(new String[0]);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Load [" + c.getName() + "] field-config file error: " + e.getMessage());
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        Field f, fs[];
        if (fns != null) {
            fs = new Field[fns.length];
            for (int i = 0; i < fns.length; i++) {
                String fn = fns[i];
                try {
                    f = c.getDeclaredField(fn);
                    int mod = f.getModifiers();
                    if (Modifier.isStatic(mod) || (serializeIgnoreFinalModifier(c) && Modifier.isFinal(mod))) {
                        throw new RuntimeException("Field [" + c.getName() + "." + fn + "] is static/final field.");
                    }
                    if (Modifier.isTransient(mod)) {
                        if (iss) {
                            return SerializableBuilder;
                        }
                        throw new RuntimeException("Field [" + c.getName() + "." + fn + "] is transient field.");
                    }
                    f.setAccessible(true);
                    fs[i] = f;
                } catch (SecurityException e) {
                    throw new RuntimeException(e.getMessage());
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("Field [" + c.getName() + "." + fn + "] not found.");
                }
            }
        } else {
            Class<?> t = c;
            List<Field> fl = new ArrayList<Field>();
            do {
                fs = t.getDeclaredFields();
                for (Field tf : fs) {
                    int mod = tf.getModifiers();
                    if (Modifier.isStatic(mod)
                            || (serializeIgnoreFinalModifier(c) && Modifier.isFinal(mod))
                            || "this$0".equals(tf.getName()) // skip static or inner-class's 'this$0' field.
                            || !Modifier.isPublic(tf.getType().getModifiers())) //skip private inner-class field
                    {
                        continue;
                    }
                    if (Modifier.isTransient(mod)) {
                        if (iss) {
                            return SerializableBuilder;
                        }
                        continue;
                    }
                    tf.setAccessible(true);
                    fl.add(tf);
                }
                t = t.getSuperclass();
            }
            while (t != Object.class);

            fs = fl.toArray(new Field[0]);
            if (fs.length > 1) {
                Arrays.sort(fs, FC);
            }
        }

        // deal with constructors.
        Constructor<?>[] cs = c.getDeclaredConstructors();
        if (cs.length == 0) {
            Class<?> t = c;
            do {
                t = t.getSuperclass();
                if (t == null) {
                    throw new RuntimeException("Can not found Constructor?");
                }
                cs = t.getDeclaredConstructors();
            }
            while (cs.length == 0);
        }
        if (cs.length > 1) {
            Arrays.sort(cs, CC);
        }

        // writeObject code.
        StringBuilder cwf = new StringBuilder("protected void writeObject(Object obj, ").append(GenericObjectOutput.class.getName()).append(" out) throws java.io.IOException{");
        cwf.append(cn).append(" v = (").append(cn).append(")$1; ");
        cwf.append("$2.writeInt(fields.length);");

        // readObject code.
        StringBuilder crf = new StringBuilder("protected void readObject(Object ret, ").append(GenericObjectInput.class.getName()).append(" in) throws java.io.IOException{");
        crf.append("int fc = $2.readInt();");
        crf.append("if( fc != ").append(fs.length).append(" ) throw new IllegalStateException(\"Deserialize Class [").append(cn).append("], field count not matched. Expect ").append(fs.length).append(" but get \" + fc +\".\");");
        crf.append(cn).append(" ret = (").append(cn).append(")$1;");

        // newInstance code.
        StringBuilder cni = new StringBuilder("protected Object newInstance(").append(GenericObjectInput.class.getName()).append(" in){ return ");
        Constructor<?> con = cs[0];
        int mod = con.getModifiers();
        boolean dn = Modifier.isPublic(mod) || (isp && !Modifier.isPrivate(mod));
        if (dn) {
            cni.append("new ").append(cn).append("(");
        } else {
            con.setAccessible(true);
            cni.append("constructor.newInstance(new Object[]{");
        }
        Class<?>[] pts = con.getParameterTypes();
        for (int i = 0; i < pts.length; i++) {
            if (i > 0) {
                cni.append(',');
            }
            cni.append(defaultArg(pts[i]));
        }
        if (!dn) {
            cni.append("}"); // close object array.
        }
        cni.append("); }");

        // get bean-style property metadata.
        Map<String, PropertyMetadata> pms = propertyMetadatas(c);
        List<Builder<?>> builders = new ArrayList<Builder<?>>(fs.length);
        String fn, ftn; // field name, field type name.
        Class<?> ft; // field type.
        boolean da; // direct access.
        PropertyMetadata pm;
        for (int i = 0; i < fs.length; i++) {
            f = fs[i];
            fn = f.getName();
            ft = f.getType();
            ftn = ReflectUtils.getName(ft);
            da = isp && (f.getDeclaringClass() == c) && (Modifier.isPrivate(f.getModifiers()) == false);
            if (da) {
                pm = null;
            } else {
                pm = pms.get(fn);
                if (pm != null && (pm.type != ft || pm.setter == null || pm.getter == null)) {
                    pm = null;
                }
            }

            crf.append("if( fc == ").append(i).append(" ) return;");
            if (ft.isPrimitive()) {
                if (ft == boolean.class) {
                    if (da) {
                        cwf.append("$2.writeBool(v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = $2.readBool();");
                    } else if (pm != null) {
                        cwf.append("$2.writeBool(v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("($2.readBool());");
                    } else {
                        cwf.append("$2.writeBool(((Boolean)fields[").append(i).append("].get($1)).booleanValue());");
                        crf.append("fields[").append(i).append("].set(ret, ($w)$2.readBool());");
                    }
                } else if (ft == byte.class) {
                    if (da) {
                        cwf.append("$2.writeByte(v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = $2.readByte();");
                    } else if (pm != null) {
                        cwf.append("$2.writeByte(v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("($2.readByte());");
                    } else {
                        cwf.append("$2.writeByte(((Byte)fields[").append(i).append("].get($1)).byteValue());");
                        crf.append("fields[").append(i).append("].set(ret, ($w)$2.readByte());");
                    }
                } else if (ft == char.class) {
                    if (da) {
                        cwf.append("$2.writeShort((short)v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = (char)$2.readShort();");
                    } else if (pm != null) {
                        cwf.append("$2.writeShort((short)v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("((char)$2.readShort());");
                    } else {
                        cwf.append("$2.writeShort((short)((Character)fields[").append(i).append("].get($1)).charValue());");
                        crf.append("fields[").append(i).append("].set(ret, ($w)((char)$2.readShort()));");
                    }
                } else if (ft == short.class) {
                    if (da) {
                        cwf.append("$2.writeShort(v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = $2.readShort();");
                    } else if (pm != null) {
                        cwf.append("$2.writeShort(v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("($2.readShort());");
                    } else {
                        cwf.append("$2.writeShort(((Short)fields[").append(i).append("].get($1)).shortValue());");
                        crf.append("fields[").append(i).append("].set(ret, ($w)$2.readShort());");
                    }
                } else if (ft == int.class) {
                    if (da) {
                        cwf.append("$2.writeInt(v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = $2.readInt();");
                    } else if (pm != null) {
                        cwf.append("$2.writeInt(v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("($2.readInt());");
                    } else {
                        cwf.append("$2.writeInt(((Integer)fields[").append(i).append("].get($1)).intValue());");
                        crf.append("fields[").append(i).append("].set(ret, ($w)$2.readInt());");
                    }
                } else if (ft == long.class) {
                    if (da) {
                        cwf.append("$2.writeLong(v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = $2.readLong();");
                    } else if (pm != null) {
                        cwf.append("$2.writeLong(v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("($2.readLong());");
                    } else {
                        cwf.append("$2.writeLong(((Long)fields[").append(i).append("].get($1)).longValue());");
                        crf.append("fields[").append(i).append("].set(ret, ($w)$2.readLong());");
                    }
                } else if (ft == float.class) {
                    if (da) {
                        cwf.append("$2.writeFloat(v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = $2.readFloat();");
                    } else if (pm != null) {
                        cwf.append("$2.writeFloat(v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("($2.readFloat());");
                    } else {
                        cwf.append("$2.writeFloat(((Float)fields[").append(i).append("].get($1)).floatValue());");
                        crf.append("fields[").append(i).append("].set(ret, ($w)$2.readFloat());");
                    }
                } else if (ft == double.class) {
                    if (da) {
                        cwf.append("$2.writeDouble(v.").append(fn).append(");");
                        crf.append("ret.").append(fn).append(" = $2.readDouble();");
                    } else if (pm != null) {
                        cwf.append("$2.writeDouble(v.").append(pm.getter).append("());");
                        crf.append("ret.").append(pm.setter).append("($2.readDouble());");
                    } else {
                        cwf.append("$2.writeDouble(((Double)fields[").append(i).append("].get($1)).doubleValue());");
                        crf.append("fields[").append(i).append("].set(ret, ($w)$2.readDouble());");
                    }
                }
            } else if (ft == c) {
                if (da) {
                    cwf.append("this.writeTo(v.").append(fn).append(", $2);");
                    crf.append("ret.").append(fn).append(" = (").append(ftn).append(")this.parseFrom($2);");
                } else if (pm != null) {
                    cwf.append("this.writeTo(v.").append(pm.getter).append("(), $2);");
                    crf.append("ret.").append(pm.setter).append("((").append(ftn).append(")this.parseFrom($2));");
                } else {
                    cwf.append("this.writeTo((").append(ftn).append(")fields[").append(i).append("].get($1), $2);");
                    crf.append("fields[").append(i).append("].set(ret, this.parseFrom($2));");
                }
            } else {
                int bc = builders.size();
                builders.add(register(ft));

                if (da) {
                    cwf.append("builders[").append(bc).append("].writeTo(v.").append(fn).append(", $2);");
                    crf.append("ret.").append(fn).append(" = (").append(ftn).append(")builders[").append(bc).append("].parseFrom($2);");
                } else if (pm != null) {
                    cwf.append("builders[").append(bc).append("].writeTo(v.").append(pm.getter).append("(), $2);");
                    crf.append("ret.").append(pm.setter).append("((").append(ftn).append(")builders[").append(bc).append("].parseFrom($2));");
                } else {
                    cwf.append("builders[").append(bc).append("].writeTo((").append(ftn).append(")fields[").append(i).append("].get($1), $2);");
                    crf.append("fields[").append(i).append("].set(ret, builders[").append(bc).append("].parseFrom($2));");
                }
            }
        }

        // skip any fields.
        crf.append("for(int i=").append(fs.length).append(";i<fc;i++) $2.skipAny();");

        // collection or map
        if (isc) {
            cwf.append("$2.writeInt(v.size()); for(java.util.Iterator it=v.iterator();it.hasNext();){ $2.writeObject(it.next()); }");
            crf.append("int len = $2.readInt(); for(int i=0;i<len;i++) ret.add($2.readObject());");
        } else if (ism) {
            cwf.append("$2.writeInt(v.size()); for(java.util.Iterator it=v.entrySet().iterator();it.hasNext();){ java.util.Map.Entry entry = (java.util.Map.Entry)it.next(); $2.writeObject(entry.getKey()); $2.writeObject(entry.getValue()); }");
            crf.append("int len = $2.readInt(); for(int i=0;i<len;i++) ret.put($2.readObject(), $2.readObject());");
        }
        cwf.append(" }");
        crf.append(" }");

        ClassGenerator cg = ClassGenerator.newInstance(cl);
        cg.setClassName(bcn);
        cg.setSuperClass(AbstractObjectBuilder.class);
        cg.addDefaultConstructor();
        cg.addField("public static java.lang.reflect.Field[] fields;");
        cg.addField("public static " + BUILDER_CLASS_NAME + "[] builders;");
        if (!dn) {
            cg.addField("public static java.lang.reflect.Constructor constructor;");
        }
        cg.addMethod("public Class getType(){ return " + cn + ".class; }");
        cg.addMethod(cwf.toString());
        cg.addMethod(crf.toString());
        cg.addMethod(cni.toString());
        try {
            Class<?> wc = cg.toClass();
            wc.getField("fields").set(null, fs);
            wc.getField("builders").set(null, builders.toArray(new Builder<?>[0]));
            if (!dn) {
                wc.getField("constructor").set(null, con);
            }
            return (Builder<?>) wc.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            cg.release();
        }
    }

    private static Builder<?> newEnumBuilder(Class<?> c) {
        ClassLoader cl = ClassHelper.getCallerClassLoader(Builder.class);

        String cn = c.getName();
        String bcn = BUILDER_CLASS_NAME + "$bc" + BUILDER_CLASS_COUNTER.getAndIncrement();

        StringBuilder cwt = new StringBuilder("public void writeTo(Object obj, ").append(GenericObjectOutput.class.getName()).append(" out) throws java.io.IOException{"); // writeTo code.
        cwt.append(cn).append(" v = (").append(cn).append(")$1;");
        cwt.append("if( $1 == null ){ $2.writeUTF(null); }else{ $2.writeUTF(v.name()); } }");

        StringBuilder cpf = new StringBuilder("public Object parseFrom(").append(GenericObjectInput.class.getName()).append(" in) throws java.io.IOException{"); // parseFrom code.
        cpf.append("String name = $1.readUTF(); if( name == null ) return null; return (").append(cn).append(")Enum.valueOf(").append(cn).append(".class, name); }");

        ClassGenerator cg = ClassGenerator.newInstance(cl);
        cg.setClassName(bcn);
        cg.setSuperClass(Builder.class);
        cg.addDefaultConstructor();
        cg.addMethod("public Class getType(){ return " + cn + ".class; }");
        cg.addMethod(cwt.toString());
        cg.addMethod(cpf.toString());
        try {
            Class<?> wc = cg.toClass();
            return (Builder<?>) wc.newInstance();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            cg.release();
        }
    }

    private static boolean serializeIgnoreFinalModifier(Class cl) {
        return false;
    }


    /**
     * The type Abstract object builder.
     *
     * @param <T> the type parameter
     */
//========================== AbstractObjectBuilder ====================
    public static abstract class AbstractObjectBuilder<T> extends Builder<T> {
        @Override
        abstract public Class<T> getType();

        @Override
        public void writeTo(T obj, GenericObjectOutput out) throws IOException {
            if (obj == null) {
                out.write0(OBJECT_NULL);
            } else {
                int ref = out.getRef(obj);
                if (ref < 0) {
                    out.addRef(obj);
                    out.write0(OBJECT);
                    writeObject(obj, out);
                } else {
                    out.write0(OBJECT_REF);
                    out.writeUInt(ref);
                }
            }
        }

        @Override
        public T parseFrom(GenericObjectInput in) throws IOException {
            byte b = in.read0();
            switch (b) {
                case OBJECT: {
                    T ret = newInstance(in);
                    in.addRef(ret);
                    readObject(ret, in);
                    return ret;
                }
                case OBJECT_REF:
                    return (T) in.getRef(in.readUInt());
                case OBJECT_NULL:
                    return null;
                default:
                    throw new IOException("Input format error, expect OBJECT|OBJECT_REF|OBJECT_NULL, get " + b);
            }
        }

        /**
         * Write object.
         *
         * @param obj the obj
         * @param out the out
         * @throws IOException the io exception
         */
        abstract protected void writeObject(T obj, GenericObjectOutput out) throws IOException;

        /**
         * New instance t.
         *
         * @param in the in
         * @return the t
         * @throws IOException the io exception
         */
        abstract protected T newInstance(GenericObjectInput in) throws IOException;

        /**
         * Read object.
         *
         * @param ret the ret
         * @param in  the in
         * @throws IOException the io exception
         */
        abstract protected void readObject(T ret, GenericObjectInput in) throws IOException;
    }

    /**
     * The type Property metadata.
     */
    static class PropertyMetadata {
        /**
         * The Type.
         */
        Class<?> type;
        /**
         * The Setter.
         */
        String setter, /**
         * The Getter.
         */
        getter;
    }

    private static String defaultArg(Class<?> cl) {
        if (boolean.class == cl) {
            return "false";
        }
        if (int.class == cl) {
            return "0";
        }
        if (long.class == cl) {
            return "0l";
        }
        if (double.class == cl) {
            return "(double)0";
        }
        if (float.class == cl) {
            return "(float)0";
        }
        if (short.class == cl) {
            return "(short)0";
        }
        if (char.class == cl) {
            return "(char)0";
        }
        if (byte.class == cl) {
            return "(byte)0";
        }
        if (byte[].class == cl) {
            return "new byte[]{0}";
        }
        if (!cl.isPrimitive()) {
            return "null";
        }
        throw new UnsupportedOperationException();
    }


    private static Map<String, PropertyMetadata> propertyMetadatas(Class<?> c) {
        Map<String, Method> mm = new HashMap<String, Method>(); // method map.
        Map<String, PropertyMetadata> ret = new HashMap<String, PropertyMetadata>(); // property metadata map.

        for (Method m : c.getMethods()) {
            if (m.getDeclaringClass() == Object.class) // Ignore Object's method.
            {
                continue;
            }
            mm.put(ReflectUtils.getDesc(m), m);
        }

        Matcher matcher;
        for (Map.Entry<String, Method> entry : mm.entrySet()) {
            String desc = entry.getKey();
            Method method = entry.getValue();
            if ((matcher = ReflectUtils.GETTER_METHOD_DESC_PATTERN.matcher(desc)).matches() ||
                    (matcher = ReflectUtils.IS_HAS_CAN_METHOD_DESC_PATTERN.matcher(desc)).matches()) {
                String pn = propertyName(matcher.group(1));
                Class<?> pt = method.getReturnType();
                PropertyMetadata pm = ret.get(pn);
                if (pm == null) {
                    pm = new PropertyMetadata();
                    pm.type = pt;
                    ret.put(pn, pm);
                } else {
                    if (pm.type != pt) {
                        continue;
                    }
                }
                pm.getter = method.getName();
            } else if ((matcher = ReflectUtils.SETTER_METHOD_DESC_PATTERN.matcher(desc)).matches()) {
                String pn = propertyName(matcher.group(1));
                Class<?> pt = method.getParameterTypes()[0];
                PropertyMetadata pm = ret.get(pn);
                if (pm == null) {
                    pm = new PropertyMetadata();
                    pm.type = pt;
                    ret.put(pn, pm);
                } else {
                    if (pm.type != pt) {
                        continue;
                    }
                }
                pm.setter = method.getName();
            }
        }
        return ret;
    }

    private static String propertyName(String s) {
        return s.length() == 1 || Character.isLowerCase(s.charAt(1)) ? Character.toLowerCase(s.charAt(0)) + s.substring(1) : s;
    }
}