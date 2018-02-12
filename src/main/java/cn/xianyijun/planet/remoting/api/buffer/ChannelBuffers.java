package cn.xianyijun.planet.remoting.api.buffer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;

/**
 * The type Channel buffers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChannelBuffers {
    /**
     * The constant EMPTY_BUFFER.
     */
    public static final ChannelBuffer EMPTY_BUFFER = new HeapChannelBuffer(0);

    /**
     * Dynamic buffer channel buffer.
     *
     * @return the channel buffer
     */
    public static ChannelBuffer dynamicBuffer() {
        return dynamicBuffer(256);
    }

    /**
     * Dynamic buffer channel buffer.
     *
     * @param capacity the capacity
     * @return the channel buffer
     */
    public static ChannelBuffer dynamicBuffer(int capacity) {
        return new DynamicChannelBuffer(capacity);
    }

    /**
     * Dynamic buffer channel buffer.
     *
     * @param capacity the capacity
     * @param factory  the factory
     * @return the channel buffer
     */
    public static ChannelBuffer dynamicBuffer(int capacity,
                                              ChannelBufferFactory factory) {
        return new DynamicChannelBuffer(capacity, factory);
    }

    /**
     * Buffer channel buffer.
     *
     * @param capacity the capacity
     * @return the channel buffer
     */
    public static ChannelBuffer buffer(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity can not be negative");
        }
        if (capacity == 0) {
            return EMPTY_BUFFER;
        }
        return new HeapChannelBuffer(capacity);
    }

    /**
     * Wrapped buffer channel buffer.
     *
     * @param array  the array
     * @param offset the offset
     * @param length the length
     * @return the channel buffer
     */
    public static ChannelBuffer wrappedBuffer(byte[] array, int offset, int length) {
        if (array == null) {
            throw new NullPointerException("array == null");
        }
        byte[] dest = new byte[length];
        System.arraycopy(array, offset, dest, 0, length);
        return wrappedBuffer(dest);
    }

    /**
     * Wrapped buffer channel buffer.
     *
     * @param array the array
     * @return the channel buffer
     */
    public static ChannelBuffer wrappedBuffer(byte[] array) {
        if (array == null) {
            throw new NullPointerException("array == null");
        }
        if (array.length == 0) {
            return EMPTY_BUFFER;
        }
        return new HeapChannelBuffer(array);
    }

    /**
     * Wrapped buffer channel buffer.
     *
     * @param buffer the buffer
     * @return the channel buffer
     */
    public static ChannelBuffer wrappedBuffer(ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            return EMPTY_BUFFER;
        }
        if (buffer.hasArray()) {
            return wrappedBuffer(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
        } else {
            return new ByteBufferBackedChannelBuffer(buffer);
        }
    }

    /**
     * Direct buffer channel buffer.
     *
     * @param capacity the capacity
     * @return the channel buffer
     */
    public static ChannelBuffer directBuffer(int capacity) {
        if (capacity == 0) {
            return EMPTY_BUFFER;
        }

        ChannelBuffer buffer = new ByteBufferBackedChannelBuffer(
                ByteBuffer.allocateDirect(capacity));
        buffer.clear();
        return buffer;
    }

    /**
     * Equals boolean.
     *
     * @param bufferA the buffer a
     * @param bufferB the buffer b
     * @return the boolean
     */
    public static boolean equals(ChannelBuffer bufferA, ChannelBuffer bufferB) {
        final int aLen = bufferA.readableBytes();
        if (aLen != bufferB.readableBytes()) {
            return false;
        }

        final int byteCount = aLen & 7;

        int aIndex = bufferA.readerIndex();
        int bIndex = bufferB.readerIndex();

        for (int i = byteCount; i > 0; i--) {
            if (bufferA.getByte(aIndex) != bufferB.getByte(bIndex)) {
                return false;
            }
            aIndex++;
            bIndex++;
        }

        return true;
    }

    /**
     * Compare int.
     *
     * @param bufferA the buffer a
     * @param bufferB the buffer b
     * @return the int
     */
    public static int compare(ChannelBuffer bufferA, ChannelBuffer bufferB) {
        final int aLen = bufferA.readableBytes();
        final int bLen = bufferB.readableBytes();
        final int minLength = Math.min(aLen, bLen);

        int aIndex = bufferA.readerIndex();
        int bIndex = bufferB.readerIndex();

        for (int i = minLength; i > 0; i--) {
            byte va = bufferA.getByte(aIndex);
            byte vb = bufferB.getByte(bIndex);
            if (va > vb) {
                return 1;
            } else if (va < vb) {
                return -1;
            }
            aIndex++;
            bIndex++;
        }

        return aLen - bLen;
    }
}
