package cn.xianyijun.planet.common.serialize;

import java.io.IOException;

/**
 * The interface Data input.
 */
public interface DataInput {
    /**
     * Read bool boolean.
     *
     * @return the boolean
     * @throws IOException the io exception
     */
    boolean readBool() throws IOException;

    /**
     * Read byte byte.
     *
     * @return the byte
     * @throws IOException the io exception
     */
    byte readByte() throws IOException;

    /**
     * Read short short.
     *
     * @return the short
     * @throws IOException the io exception
     */
    short readShort() throws IOException;

    /**
     * Read int int.
     *
     * @return the int
     * @throws IOException the io exception
     */
    int readInt() throws IOException;

    /**
     * Read long long.
     *
     * @return the long
     * @throws IOException the io exception
     */
    long readLong() throws IOException;

    /**
     * Read float float.
     *
     * @return the float
     * @throws IOException the io exception
     */
    float readFloat() throws IOException;

    /**
     * Read double double.
     *
     * @return the double
     * @throws IOException the io exception
     */
    double readDouble() throws IOException;

    /**
     * Read utf string.
     *
     * @return the string
     * @throws IOException the io exception
     */
    String readUTF() throws IOException;

    /**
     * Read bytes byte [ ].
     *
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    byte[] readBytes() throws IOException;
}