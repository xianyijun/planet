package cn.xianyijun.planet.remoting.api.buffer;

import java.io.IOException;
import java.io.InputStream;

/**
 * The type Channel buffer input stream.
 */
public class ChannelBufferInputStream extends InputStream {
    private final ChannelBuffer buffer;
    private final int startIndex;
    private final int endIndex;

    /**
     * Instantiates a new Channel buffer input stream.
     *
     * @param buffer the buffer
     */
    public ChannelBufferInputStream(ChannelBuffer buffer) {
        this(buffer, buffer.readableBytes());
    }

    /**
     * Instantiates a new Channel buffer input stream.
     *
     * @param buffer the buffer
     * @param length the length
     */
    public ChannelBufferInputStream(ChannelBuffer buffer, int length) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        if (length < 0) {
            throw new IllegalArgumentException("length: " + length);
        }
        if (length > buffer.readableBytes()) {
            throw new IndexOutOfBoundsException();
        }

        this.buffer = buffer;
        startIndex = buffer.readerIndex();
        endIndex = startIndex + length;
        buffer.markReaderIndex();
    }

    /**
     * Read bytes int.
     *
     * @return the int
     */
    public int readBytes() {
        return buffer.readerIndex() - startIndex;
    }

    @Override
    public int available() throws IOException {
        return endIndex - buffer.readerIndex();
    }

    @Override
    public void mark(int readlimit) {
        buffer.markReaderIndex();
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public int read() throws IOException {
        if (!buffer.readable()) {
            return -1;
        }
        return buffer.readByte() & 0xff;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int available = available();
        if (available == 0) {
            return -1;
        }

        len = Math.min(available, len);
        buffer.readBytes(b, off, len);
        return len;
    }

    @Override
    public void reset() throws IOException {
        buffer.resetReaderIndex();
    }

    @Override
    public long skip(long n) throws IOException {
        if (n > Integer.MAX_VALUE) {
            return skipBytes(Integer.MAX_VALUE);
        } else {
            return skipBytes((int) n);
        }
    }

    private int skipBytes(int n) throws IOException {
        int nBytes = Math.min(available(), n);
        buffer.skipBytes(nBytes);
        return nBytes;
    }

}
