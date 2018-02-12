package cn.xianyijun.planet.rpc.rpc;

import cn.xianyijun.planet.common.serialize.ObjectInput;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.Decodeable;
import cn.xianyijun.planet.remoting.api.exchange.Response;
import cn.xianyijun.planet.remoting.api.transport.CodecSupport;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.RpcResult;
import cn.xianyijun.planet.utils.RpcUtils;
import cn.xianyijun.planet.utils.StringUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

@Slf4j
public class DecodeableRpcResult extends RpcResult implements Decodeable {

    private Channel channel;

    private byte serializationType;

    @NonNull
    private InputStream inputStream;

    @NonNull
    private Response response;

    @NonNull
    private Invocation invocation;

    private volatile boolean hasDecoded;

    public DecodeableRpcResult(Channel channel, Response response, InputStream is, Invocation invocation, byte id) {
        this.channel = channel;
        this.response = response;
        this.inputStream = is;
        this.invocation = invocation;
        this.serializationType = id;
    }


    @Override
    public void decode() throws Exception {
        if (!hasDecoded && channel != null && inputStream != null) {
            try {
                doDecode(channel, inputStream);
            } catch (Throwable e) {
                if (log.isWarnEnabled()) {
                    log.warn("Decode rpc result failed: " + e.getMessage(), e);
                }
                response.setStatus(Response.CLIENT_ERROR);
                response.setErrorMessage(StringUtils.toString(e));
            } finally {
                hasDecoded = true;
            }
        }
    }

    private Object doDecode(Channel channel, InputStream input) throws IOException{
        ObjectInput in = CodecSupport.getSerialization(channel.getUrl(), serializationType)
                .deserialize(channel.getUrl(), input);

        byte flag = in.readByte();
        switch (flag) {
            case RpcCodec.RESPONSE_NULL_VALUE:
                break;
            case RpcCodec.RESPONSE_VALUE:
                try {
                    Type[] returnType = RpcUtils.getReturnTypes(invocation);
                    setValue(returnType == null || returnType.length == 0 ? in.readObject() :
                            (returnType.length == 1 ? in.readObject((Class<?>) returnType[0])
                                    : in.readObject((Class<?>) returnType[0], returnType[1])));
                } catch (ClassNotFoundException e) {
                    throw new IOException(StringUtils.toString("Read response data failed.", e));
                }
                break;
            case RpcCodec.RESPONSE_WITH_EXCEPTION:
                try {
                    Object obj = in.readObject();
                    if (!(obj instanceof Throwable)){
                        throw new IOException("Response data error, expect Throwable, but get " + obj);
                    }
                    setException((Throwable) obj);
                } catch (ClassNotFoundException e) {
                    throw new IOException(StringUtils.toString("Read response data failed.", e));
                }
                break;
            default:
                throw new IOException("Unknown result flag, expect '0' '1' '2', get " + flag);
        }
        return this;
    }

}
