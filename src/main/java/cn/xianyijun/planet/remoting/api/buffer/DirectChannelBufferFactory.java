package cn.xianyijun.planet.remoting.api.buffer;


import java.nio.ByteBuffer;

/**
 * The type Direct channel buffer factory.
 */
public class DirectChannelBufferFactory implements ChannelBufferFactory {

    private static final DirectChannelBufferFactory INSTANCE = new DirectChannelBufferFactory();

    /**
     * Instantiates a new Direct channel buffer factory.
     */
    public DirectChannelBufferFactory() {
        super();
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static ChannelBufferFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public ChannelBuffer getBuffer(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity: " + capacity);
        }
        if (capacity == 0) {
            return ChannelBuffers.EMPTY_BUFFER;
        }
        return ChannelBuffers.directBuffer(capacity);
    }

    @Override
    public ChannelBuffer getBuffer(byte[] array, int offset, int length) {
        if (array == null) {
            throw new NullPointerException("array");
        }
        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset: " + offset);
        }
        if (length == 0) {
            return ChannelBuffers.EMPTY_BUFFER;
        }
        if (offset + length > array.length) {
            throw new IndexOutOfBoundsException("length: " + length);
        }

        ChannelBuffer buf = getBuffer(length);
        buf.writeBytes(array, offset, length);
        return buf;
    }

    @Override
    public ChannelBuffer getBuffer(ByteBuffer nioBuffer) {
        if (!nioBuffer.isReadOnly() && nioBuffer.isDirect()) {
            return ChannelBuffers.wrappedBuffer(nioBuffer);
        }

        ChannelBuffer buf = getBuffer(nioBuffer.remaining());
        int pos = nioBuffer.position();
        buf.writeBytes(nioBuffer);
        nioBuffer.position(pos);
        return buf;
    }

}
