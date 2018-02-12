/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xianyijun.planet.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * The type Unsafe byte array output stream.
 * @author xianyijun
 */
public class UnsafeByteArrayOutputStream extends OutputStream {
    /**
     * The M buffer.
     */
    protected byte mBuffer[];

    /**
     * The M count.
     */
    protected int mCount;

    /**
     * Instantiates a new Unsafe byte array output stream.
     */
    public UnsafeByteArrayOutputStream() {
        this(32);
    }

    /**
     * Instantiates a new Unsafe byte array output stream.
     *
     * @param size the size
     */
    public UnsafeByteArrayOutputStream(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        mBuffer = new byte[size];
    }

    @Override
    public void write(int b) {
        int newcount = mCount + 1;
        if (newcount > mBuffer.length) {
            mBuffer = Bytes.copyOf(mBuffer, Math.max(mBuffer.length << 1, newcount));
        }
        mBuffer[mCount] = (byte) b;
        mCount = newcount;
    }

    @Override
    public void write(byte b[], int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        int newCount = mCount + len;
        if (newCount > mBuffer.length) {
            mBuffer = Bytes.copyOf(mBuffer, Math.max(mBuffer.length << 1, newCount));
        }
        System.arraycopy(b, off, mBuffer, mCount, len);
        mCount = newCount;
    }

    /**
     * Size int.
     *
     * @return the int
     */
    public int size() {
        return mCount;
    }

    /**
     * Reset.
     */
    public void reset() {
        mCount = 0;
    }

    /**
     * To byte array byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] toByteArray() {
        return Bytes.copyOf(mBuffer, mCount);
    }

    /**
     * To byte buffer byte buffer.
     *
     * @return the byte buffer
     */
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(mBuffer, 0, mCount);
    }

    /**
     * Write to.
     *
     * @param out the out
     * @throws IOException the io exception
     */
    public void writeTo(OutputStream out) throws IOException {
        out.write(mBuffer, 0, mCount);
    }

    @Override
    public String toString() {
        return new String(mBuffer, 0, mCount);
    }

    /**
     * To string string.
     *
     * @param charset the charset
     * @return the string
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public String toString(String charset) throws UnsupportedEncodingException {
        return new String(mBuffer, 0, mCount, charset);
    }

    @Override
    public void close() throws IOException {
    }
}