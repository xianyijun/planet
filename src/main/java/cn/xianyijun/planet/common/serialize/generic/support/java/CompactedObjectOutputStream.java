package cn.xianyijun.planet.common.serialize.generic.support.java;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;

/**
 * The type Compacted object output stream.
 */
public class CompactedObjectOutputStream extends ObjectOutputStream {
    /**
     * Instantiates a new Compacted object output stream.
     *
     * @param out the out
     * @throws IOException the io exception
     */
    public CompactedObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        Class<?> clazz = desc.forClass();
        if (clazz.isPrimitive() || clazz.isArray()) {
            write(0);
            super.writeClassDescriptor(desc);
        } else {
            write(1);
            writeUTF(desc.getName());
        }
    }

}
