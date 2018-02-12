package cn.xianyijun.planet.common.serialize.generic;

import cn.xianyijun.planet.common.serialize.ObjectOutput;
import cn.xianyijun.planet.common.serialize.generic.support.ClassDescriptorMapper;
import cn.xianyijun.planet.utils.ReflectUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Generic object output.
 */
public class GenericObjectOutput extends GenericDataOutput implements ObjectOutput {
    private final boolean isAllowNonSerializable;
    private ClassDescriptorMapper mMapper;
    private Map<Object, Integer> mRefs = new ConcurrentHashMap<Object, Integer>();

    /**
     * Instantiates a new Generic object output.
     *
     * @param out the out
     */
    public GenericObjectOutput(OutputStream out) {
        this(out, Builder.DEFAULT_CLASS_DESCRIPTOR_MAPPER);
    }

    /**
     * Instantiates a new Generic object output.
     *
     * @param out    the out
     * @param mapper the mapper
     */
    public GenericObjectOutput(OutputStream out, ClassDescriptorMapper mapper) {
        super(out);
        mMapper = mapper;
        isAllowNonSerializable = false;
    }

    /**
     * Instantiates a new Generic object output.
     *
     * @param out      the out
     * @param buffSize the buff size
     */
    public GenericObjectOutput(OutputStream out, int buffSize) {
        this(out, buffSize, Builder.DEFAULT_CLASS_DESCRIPTOR_MAPPER, false);
    }

    /**
     * Instantiates a new Generic object output.
     *
     * @param out      the out
     * @param buffSize the buff size
     * @param mapper   the mapper
     */
    public GenericObjectOutput(OutputStream out, int buffSize, ClassDescriptorMapper mapper) {
        this(out, buffSize, mapper, false);
    }

    /**
     * Instantiates a new Generic object output.
     *
     * @param out                    the out
     * @param buffSize               the buff size
     * @param mapper                 the mapper
     * @param isAllowNonSerializable the is allow non serializable
     */
    public GenericObjectOutput(OutputStream out, int buffSize, ClassDescriptorMapper mapper, boolean isAllowNonSerializable) {
        super(out, buffSize);
        mMapper = mapper;
        this.isAllowNonSerializable = isAllowNonSerializable;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void writeObject(Object obj) throws IOException {
        if (obj == null) {
            write0(OBJECT_NULL);
            return;
        }

        Class<?> c = obj.getClass();
        if (c == Object.class) {
            write0(OBJECT_DUMMY);
        } else {
            String desc = ReflectUtils.getDesc(c);
            int index = mMapper.getDescriptorIndex(desc);
            if (index < 0) {
                write0(OBJECT_DESC);
                writeUTF(desc);
            } else {
                write0(OBJECT_DESC_ID);
                writeUInt(index);
            }
            Builder b = Builder.register(c, isAllowNonSerializable);
            b.writeTo(obj, this);
        }
    }

    /**
     * Add ref.
     *
     * @param obj the obj
     */
    public void addRef(Object obj) {
        mRefs.put(obj, mRefs.size());
    }

    /**
     * Gets ref.
     *
     * @param obj the obj
     * @return the ref
     */
    public int getRef(Object obj) {
        Integer ref = mRefs.get(obj);
        if (ref == null) {
            return -1;
        }
        return ref.intValue();
    }
}
