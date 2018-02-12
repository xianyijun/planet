package cn.xianyijun.planet.common.serialize;

import java.io.IOException;

/**
 * The interface Data output.
 * @author xianyijun
 */
public interface DataOutput {
    /**
     * Write bool.
     *
     * @param v the v
     * @throws IOException the io exception
     */
    void writeBool(boolean v) throws IOException;

    /**
     * Write byte.
     *
     * @param v the v
     * @throws IOException the io exception
     */
    void writeByte(byte v) throws IOException;

    /**
     * Write short.
     *
     * @param v the v
     * @throws IOException the io exception
     */
    void writeShort(short v) throws IOException;

    /**
     * Write int.
     *
     * @param v the v
     * @throws IOException the io exception
     */
    void writeInt(int v) throws IOException;

    /**
     * Write long.
     *
     * @param v the v
     * @throws IOException the io exception
     */
    void writeLong(long v) throws IOException;

    /**
     * Write float.
     *
     * @param v the v
     * @throws IOException the io exception
     */
    void writeFloat(float v) throws IOException;

    /**
     * Write double.
     *
     * @param v the v
     * @throws IOException the io exception
     */
    void writeDouble(double v) throws IOException;

    /**
     * Write utf.
     *
     * @param v the v
     * @throws IOException the io exception
     */
    void writeUTF(String v) throws IOException;

    /**
     * Write bytes.
     *
     * @param v the v
     * @throws IOException the io exception
     */
    void writeBytes(byte[] v) throws IOException;

    /**
     * Write bytes.
     *
     * @param v   the v
     * @param off the off
     * @param len the len
     * @throws IOException the io exception
     */
    void writeBytes(byte[] v, int off, int len) throws IOException;

    /**
     * Flush buffer.
     *
     * @throws IOException the io exception
     */
    void flushBuffer() throws IOException;
}