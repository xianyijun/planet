package cn.xianyijun.planet.remoting.api.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * The interface Channel buffer.
 */
public interface ChannelBuffer extends Comparable<ChannelBuffer> {
    /**
     * Capacity int.
     *
     * @return the int
     */
    int capacity();

    /**
     * Clear.
     */
    void clear();

    /**
     * Copy channel buffer.
     *
     * @return the channel buffer
     */
    ChannelBuffer copy();

    /**
     * Copy channel buffer.
     *
     * @param index  the index
     * @param length the length
     * @return the channel buffer
     */
    ChannelBuffer copy(int index, int length);

    /**
     * Discard read bytes.
     */
    void discardReadBytes();

    /**
     * Ensure writable bytes.
     *
     * @param writableBytes the writable bytes
     */
    void ensureWritableBytes(int writableBytes);

    @Override
    public boolean equals(Object o);

    /**
     * Factory channel buffer factory.
     *
     * @return the channel buffer factory
     */
    ChannelBufferFactory factory();

    /**
     * Gets byte.
     *
     * @param index the index
     * @return the byte
     */
    byte getByte(int index);

    /**
     * Gets bytes.
     *
     * @param index the index
     * @param dst   the dst
     */
    void getBytes(int index, byte[] dst);

    /**
     * Gets bytes.
     *
     * @param index    the index
     * @param dst      the dst
     * @param dstIndex the dst index
     * @param length   the length
     */
    void getBytes(int index, byte[] dst, int dstIndex, int length);

    /**
     * Gets bytes.
     *
     * @param index the index
     * @param dst   the dst
     */
    void getBytes(int index, ByteBuffer dst);

    /**
     * Gets bytes.
     *
     * @param index the index
     * @param dst   the dst
     */
    void getBytes(int index, ChannelBuffer dst);

    /**
     * Gets bytes.
     *
     * @param index  the index
     * @param dst    the dst
     * @param length the length
     */
    void getBytes(int index, ChannelBuffer dst, int length);

    /**
     * Gets bytes.
     *
     * @param index    the index
     * @param dst      the dst
     * @param dstIndex the dst index
     * @param length   the length
     */
    void getBytes(int index, ChannelBuffer dst, int dstIndex, int length);

    /**
     * Gets bytes.
     *
     * @param index  the index
     * @param dst    the dst
     * @param length the length
     * @throws IOException the io exception
     */
    void getBytes(int index, OutputStream dst, int length) throws IOException;

    /**
     * Is direct boolean.
     *
     * @return the boolean
     */
    boolean isDirect();

    /**
     * Mark reader index.
     */
    void markReaderIndex();

    /**
     * Mark writer index.
     */
    void markWriterIndex();

    /**
     * Readable boolean.
     *
     * @return the boolean
     */
    boolean readable();

    /**
     * Readable bytes int.
     *
     * @return the int
     */
    int readableBytes();

    /**
     * Read byte byte.
     *
     * @return the byte
     */
    byte readByte();

    /**
     * Read bytes.
     *
     * @param dst the dst
     */
    void readBytes(byte[] dst);

    /**
     * Read bytes.
     *
     * @param dst      the dst
     * @param dstIndex the dst index
     * @param length   the length
     */
    void readBytes(byte[] dst, int dstIndex, int length);

    /**
     * Read bytes.
     *
     * @param dst the dst
     */
    void readBytes(ByteBuffer dst);

    /**
     * Read bytes.
     *
     * @param dst the dst
     */
    void readBytes(ChannelBuffer dst);

    /**
     * Read bytes.
     *
     * @param dst    the dst
     * @param length the length
     */
    void readBytes(ChannelBuffer dst, int length);

    /**
     * Read bytes.
     *
     * @param dst      the dst
     * @param dstIndex the dst index
     * @param length   the length
     */
    void readBytes(ChannelBuffer dst, int dstIndex, int length);

    /**
     * Read bytes channel buffer.
     *
     * @param length the length
     * @return the channel buffer
     */
    ChannelBuffer readBytes(int length);

    /**
     * Reset reader index.
     */
    void resetReaderIndex();

    /**
     * Reset writer index.
     */
    void resetWriterIndex();

    /**
     * Reader index int.
     *
     * @return the int
     */
    int readerIndex();

    /**
     * Reader index.
     *
     * @param readerIndex the reader index
     */
    void readerIndex(int readerIndex);

    /**
     * Read bytes.
     *
     * @param dst    the dst
     * @param length the length
     * @throws IOException the io exception
     */
    void readBytes(OutputStream dst, int length) throws IOException;

    /**
     * Sets byte.
     *
     * @param index the index
     * @param value the value
     */
    void setByte(int index, int value);

    /**
     * Sets bytes.
     *
     * @param index the index
     * @param src   the src
     */
    void setBytes(int index, byte[] src);

    /**
     * Sets bytes.
     *
     * @param index    the index
     * @param src      the src
     * @param srcIndex the src index
     * @param length   the length
     */
    void setBytes(int index, byte[] src, int srcIndex, int length);

    /**
     * Sets bytes.
     *
     * @param index the index
     * @param src   the src
     */
    void setBytes(int index, ByteBuffer src);

    /**
     * Sets bytes.
     *
     * @param index the index
     * @param src   the src
     */
    void setBytes(int index, ChannelBuffer src);

    /**
     * Sets bytes.
     *
     * @param index  the index
     * @param src    the src
     * @param length the length
     */
    void setBytes(int index, ChannelBuffer src, int length);

    /**
     * Sets bytes.
     *
     * @param index    the index
     * @param src      the src
     * @param srcIndex the src index
     * @param length   the length
     */
    void setBytes(int index, ChannelBuffer src, int srcIndex, int length);

    /**
     * Sets bytes.
     *
     * @param index  the index
     * @param src    the src
     * @param length the length
     * @return the bytes
     * @throws IOException the io exception
     */
    int setBytes(int index, InputStream src, int length) throws IOException;

    /**
     * Sets index.
     *
     * @param readerIndex the reader index
     * @param writerIndex the writer index
     */
    void setIndex(int readerIndex, int writerIndex);

    /**
     * Skip bytes.
     *
     * @param length the length
     */
    void skipBytes(int length);

    /**
     * To byte buffer byte buffer.
     *
     * @return the byte buffer
     */
    ByteBuffer toByteBuffer();

    /**
     * To byte buffer byte buffer.
     *
     * @param index  the index
     * @param length the length
     * @return the byte buffer
     */
    ByteBuffer toByteBuffer(int index, int length);

    /**
     * Writable boolean.
     *
     * @return the boolean
     */
    boolean writable();

    /**
     * Writable bytes int.
     *
     * @return the int
     */
    int writableBytes();

    /**
     * Write byte.
     *
     * @param value the value
     */
    void writeByte(int value);

    /**
     * Write bytes.
     *
     * @param src the src
     */
    void writeBytes(byte[] src);

    /**
     * Write bytes.
     *
     * @param src    the src
     * @param index  the index
     * @param length the length
     */
    void writeBytes(byte[] src, int index, int length);

    /**
     * Write bytes.
     *
     * @param src the src
     */
    void writeBytes(ByteBuffer src);

    /**
     * Write bytes.
     *
     * @param src the src
     */
    void writeBytes(ChannelBuffer src);

    /**
     * Write bytes.
     *
     * @param src    the src
     * @param length the length
     */
    void writeBytes(ChannelBuffer src, int length);

    /**
     * Write bytes.
     *
     * @param src      the src
     * @param srcIndex the src index
     * @param length   the length
     */
    void writeBytes(ChannelBuffer src, int srcIndex, int length);

    /**
     * Write bytes int.
     *
     * @param src    the src
     * @param length the length
     * @return the int
     * @throws IOException the io exception
     */
    int writeBytes(InputStream src, int length) throws IOException;

    /**
     * Writer index int.
     *
     * @return the int
     */
    int writerIndex();

    /**
     * Writer index.
     *
     * @param writerIndex the writer index
     */
    void writerIndex(int writerIndex);

    /**
     * Array byte [ ].
     *
     * @return the byte [ ]
     */
    byte[] array();

    /**
     * Has array boolean.
     *
     * @return the boolean
     */
    boolean hasArray();

    /**
     * Array offset int.
     *
     * @return the int
     */
    int arrayOffset();
}
