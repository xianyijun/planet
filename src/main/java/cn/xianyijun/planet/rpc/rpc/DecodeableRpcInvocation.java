package cn.xianyijun.planet.rpc.rpc;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.serialize.ObjectInput;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.Decodeable;
import cn.xianyijun.planet.remoting.api.exchange.Request;
import cn.xianyijun.planet.remoting.api.transport.CodecSupport;
import cn.xianyijun.planet.rpc.api.RpcInvocation;
import cn.xianyijun.planet.utils.ReflectUtils;
import cn.xianyijun.planet.utils.StringUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static cn.xianyijun.planet.rpc.rpc.CallbackServiceCodec.decodeInvocationArgument;


@Slf4j
public class DecodeableRpcInvocation extends RpcInvocation implements Decodeable {

    @NonNull
    private Channel channel;

    @NonNull
    private InputStream inputStream;

    @NonNull
    private Request request;

    private byte serializationType;

    private volatile boolean hasDecoded;

    public DecodeableRpcInvocation(Channel channel, Request request, InputStream is, byte id) {
        this.channel = channel;
        this.request = request;
        this.inputStream = is;
        this.serializationType = id;
    }

    @Override
    public void decode() throws Exception {
        if (!hasDecoded && channel != null && inputStream != null) {
            try {
                decode(channel, inputStream);
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn("Decode rpc invocation failed: " + e.getMessage(), e);
                }
                request.setBroken(true);
                request.setData(e);
            } finally {
                hasDecoded = true;
            }
        }
    }

    public Object decode(Channel channel, InputStream input) throws IOException {
        ObjectInput in = CodecSupport.getSerialization(channel.getUrl(), serializationType)
                .deserialize(channel.getUrl(), input);

        setAttachment(Constants.VERSION_KEY, in.readUTF());
        setAttachment(Constants.PATH_KEY, in.readUTF());
        setAttachment(Constants.VERSION_KEY, in.readUTF());

        setMethodName(in.readUTF());
        try {
            Object[] args;
            Class<?>[] pts;
            String desc = in.readUTF();
            if (desc.length() == 0) {
                pts = RpcCodec.EMPTY_CLASS_ARRAY;
                args = RpcCodec.EMPTY_OBJECT_ARRAY;
            } else {
                pts = ReflectUtils.desc2classArray(desc);
                args = new Object[pts.length];
                for (int i = 0; i < args.length; i++) {
                    try {
                        args[i] = in.readObject(pts[i]);
                    } catch (Exception e) {
                        if (log.isWarnEnabled()) {
                            log.warn("Decode argument failed: " + e.getMessage(), e);
                        }
                    }
                }
            }
            setParameterTypes(pts);

            Map<String, String> map = (Map<String, String>) in.readObject(Map.class);
            if (map != null && map.size() > 0) {
                Map<String, String> attachment = getAttachments();
                if (attachment == null) {
                    attachment = new HashMap<>();
                }
                attachment.putAll(map);
                setAttachments(attachment);
            }
            for (int i = 0; i < args.length; i++) {
                args[i] = decodeInvocationArgument(channel, this, pts, i, args[i]);
            }

            setArguments(args);

        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString("Read invocation data failed.", e));
        }
        return this;
    }
}
