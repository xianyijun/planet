package cn.xianyijun.planet.remoting.api.transport.codec;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.serialize.ObjectInput;
import cn.xianyijun.planet.common.serialize.ObjectOutput;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.buffer.ChannelBuffer;
import cn.xianyijun.planet.remoting.api.buffer.ChannelBufferInputStream;
import cn.xianyijun.planet.remoting.api.buffer.ChannelBufferOutputStream;
import cn.xianyijun.planet.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Transport codec.
 *
 * @author xianyijun
 */
@Slf4j
public class TransportCodec extends AbstractCodec {

    @Override
    public void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException {
        OutputStream output = new ChannelBufferOutputStream(buffer);
        ObjectOutput objectOutput = getSerialization(channel).serialize(channel.getUrl(), output);
        encodeData(channel, objectOutput, message);
        objectOutput.flushBuffer();
    }

    @Override
    public Object decode(Channel channel, ChannelBuffer buffer) throws IOException {
        InputStream input = new ChannelBufferInputStream(buffer);
        return decodeData(channel, getSerialization(channel).deserialize(channel.getUrl(), input));
    }

    @SuppressWarnings("unchecked")
    protected Object decode(Channel channel, ChannelBuffer buffer, int readable, byte[] message) throws IOException {
        if (isClientSide(channel)) {
            return toString(message, getCharset(channel));
        }
        checkPayload(channel, readable);
        if (message == null || message.length == 0) {
            return DecodeResult.NEED_MORE_INPUT;
        }

        if (message[message.length - 1] == '\b') {
            try {
                boolean doublechar = message.length >= 3 && message[message.length - 3] < 0;
                channel.send(new String(doublechar ? new byte[]{32, 32, 8, 8} : new byte[]{32, 8}, getCharset(channel).name()));
            } catch (RemotingException e) {
                throw new IOException(StringUtils.toString(e));
            }
            return DecodeResult.NEED_MORE_INPUT;
        }

        return toString(message, getCharset(channel));
    }

    /**
     * Encode data.
     *
     * @param channel the channel
     * @param output  the output
     * @param message the message
     * @throws IOException the io exception
     */
    protected void encodeData(Channel channel, ObjectOutput output, Object message) throws IOException {
        encodeData(output, message);
    }

    /**
     * Decode data object.
     *
     * @param channel the channel
     * @param input   the input
     * @return the object
     * @throws IOException the io exception
     */
    protected Object decodeData(Channel channel, ObjectInput input) throws IOException {
        return decodeData(input);
    }

    /**
     * Encode data.
     *
     * @param output  the output
     * @param message the message
     * @throws IOException the io exception
     */
    protected void encodeData(ObjectOutput output, Object message) throws IOException {
        output.writeObject(message);
    }

    /**
     * Decode data object.
     *
     * @param input the input
     * @return the object
     * @throws IOException the io exception
     */
    protected Object decodeData(ObjectInput input) throws IOException {
        try {
            return input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("ClassNotFoundException: " + StringUtils.toString(e));
        }
    }

    private static Charset getCharset(Channel channel) {
        if (channel != null) {
            Object attribute = channel.getAttribute(Constants.CHARSET_KEY);
            if (attribute instanceof String) {
                try {
                    return Charset.forName((String) attribute);
                } catch (Throwable t) {
                    log.warn(t.getMessage(), t);
                }
            } else if (attribute instanceof Charset) {
                return (Charset) attribute;
            }
            URL url = channel.getUrl();
            if (url != null) {
                String parameter = url.getParameter(Constants.CHARSET_KEY);
                if (parameter != null && parameter.length() > 0) {
                    try {
                        return Charset.forName(parameter);
                    } catch (Throwable t) {
                        log.warn(t.getMessage(), t);
                    }
                }
            }
        }
        try {
            return Charset.forName(Constants.DEFAULT_CHARSET);
        } catch (Throwable t) {
            log.warn(t.getMessage(), t);
        }
        return Charset.defaultCharset();
    }

    private static String toString(byte[] message, Charset charset) throws UnsupportedEncodingException {
        byte[] copy = new byte[message.length];
        int index = 0;
        for (int i = 0; i < message.length; i++) {
            byte b = message[i];
            if (b == '\b') {
                if (index > 0) {
                    index--;
                }
                if (i > 2 && message[i - 2] < 0) {
                    if (index > 0) {
                        index--;
                    }
                }
            } else if (b == 27) {
                if (i < message.length - 4 && message[i + 4] == 126) {
                    i = i + 4;
                } else if (i < message.length - 3 && message[i + 3] == 126) {
                    i = i + 3;
                } else if (i < message.length - 2) {
                    i = i + 2;
                }
            } else if (b == -1 && i < message.length - 2
                    && (message[i + 1] == -3 || message[i + 1] == -5)) {
                i = i + 2;
            } else {
                copy[index++] = message[i];
            }
        }
        if (index == 0) {
            return "";
        }
        return new String(copy, 0, index, charset.name()).trim();
    }


}
