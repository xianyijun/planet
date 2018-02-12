package cn.xianyijun.planet.remoting.api.buffer;

import java.nio.ByteBuffer;

/**
 * The interface Channel buffer factory.
 */
public interface ChannelBufferFactory {

    /**
     * Gets buffer.
     *
     * @param capacity the capacity
     * @return the buffer
     */
    ChannelBuffer getBuffer(int capacity);

    /**
     * Gets buffer.
     *
     * @param array  the array
     * @param offset the offset
     * @param length the length
     * @return the buffer
     */
    ChannelBuffer getBuffer(byte[] array, int offset, int length);

    /**
     * Gets buffer.
     *
     * @param nioBuffer the nio buffer
     * @return the buffer
     */
    ChannelBuffer getBuffer(ByteBuffer nioBuffer);
}
