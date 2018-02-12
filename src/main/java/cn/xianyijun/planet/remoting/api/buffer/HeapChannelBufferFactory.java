package cn.xianyijun.planet.remoting.api.buffer;

import java.nio.ByteBuffer;

/**
 * Created by xianyijun on 2017/10/29.
 */
public class HeapChannelBufferFactory implements ChannelBufferFactory{
    private static final HeapChannelBufferFactory INSTANCE = new HeapChannelBufferFactory();

    /**
     * Instantiates a new Heap channel buffer factory.
     */
    public HeapChannelBufferFactory() {
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
        return ChannelBuffers.buffer(capacity);
    }

    @Override
    public ChannelBuffer getBuffer(byte[] array, int offset, int length) {
        return ChannelBuffers.wrappedBuffer(array, offset, length);
    }

    @Override
    public ChannelBuffer getBuffer(ByteBuffer nioBuffer) {
        if (nioBuffer.hasArray()) {
            return ChannelBuffers.wrappedBuffer(nioBuffer);
        }

        ChannelBuffer buf = getBuffer(nioBuffer.remaining());
        int pos = nioBuffer.position();
        buf.writeBytes(nioBuffer);
        nioBuffer.position(pos);
        return buf;
    }

}
