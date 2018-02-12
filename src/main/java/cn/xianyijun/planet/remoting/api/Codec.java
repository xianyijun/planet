package cn.xianyijun.planet.remoting.api;

import cn.xianyijun.planet.remoting.api.buffer.ChannelBuffer;

import java.io.IOException;

/**
 * The interface Codec.
 */
public interface Codec {
    /**
     * Encode.
     *
     * @param channel the channel
     * @param buffer  the buffer
     * @param message the message
     * @throws IOException the io exception
     */
    void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException;

    /**
     * Decode object.
     *
     * @param channel the channel
     * @param buffer  the buffer
     * @return the object
     * @throws IOException the io exception
     */
    Object decode(Channel channel, ChannelBuffer buffer) throws IOException;

    /**
     * The enum Decode result.
     */
    enum DecodeResult {
        /**
         * Need more input decode result.
         */
        NEED_MORE_INPUT, /**
         * Skip some input decode result.
         */
        SKIP_SOME_INPUT
    }
}
