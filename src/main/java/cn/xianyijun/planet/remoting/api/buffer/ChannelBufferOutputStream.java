package cn.xianyijun.planet.remoting.api.buffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The type Channel buffer output stream.
 */
public class ChannelBufferOutputStream extends OutputStream {

    private final ChannelBuffer buffer;
    private final int startIndex;

    /**
     * Instantiates a new Channel buffer output stream.
     *
     * @param buffer the buffer
     */
    public ChannelBufferOutputStream(ChannelBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException("buffer");
        }
        this.buffer = buffer;
        startIndex = buffer.writerIndex();
    }

    /**
     * Written bytes int.
     *
     * @return the int
     */
    public int writtenBytes() {
        return buffer.writerIndex() - startIndex;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        buffer.writeBytes(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        buffer.writeBytes(b);
    }

    @Override
    public void write(int b) throws IOException {
        buffer.writeByte((byte) b);
    }

    /**
     * Buffer channel buffer.
     *
     * @return the channel buffer
     */
    public ChannelBuffer buffer() {
        return buffer;
    }
}