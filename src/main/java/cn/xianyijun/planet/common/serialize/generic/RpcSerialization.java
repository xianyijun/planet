package cn.xianyijun.planet.common.serialize.generic;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.serialize.ObjectInput;
import cn.xianyijun.planet.common.serialize.ObjectOutput;
import cn.xianyijun.planet.common.serialize.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The type Rpc serialization.
 */
public class RpcSerialization implements Serialization {
    @Override
    public byte getContentTypeId() {
        return 1;
    }

    @Override
    public String getContentType() {
        return "x-application/rpc";
    }

    @Override
    public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
        return new GenericObjectOutput(out);
    }

    @Override
    public ObjectInput deserialize(URL url, InputStream is) throws IOException {
        return new GenericObjectInput(is);
    }
}
