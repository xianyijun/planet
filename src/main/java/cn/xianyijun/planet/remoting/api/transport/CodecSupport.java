package cn.xianyijun.planet.remoting.api.transport;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.serialize.Serialization;
import cn.xianyijun.planet.common.serialize.generic.RpcSerialization;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Codec support.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CodecSupport {
    private static Map<Byte, Serialization> ID_SERIALIZATION_MAP = new HashMap<>();

    static {
        Serialization rpcSerialization = new RpcSerialization();
        ID_SERIALIZATION_MAP.put(rpcSerialization.getContentTypeId(), rpcSerialization);
    }

    /**
     * Gets serialization by id.
     *
     * @param id the id
     * @return the serialization by id
     */
    public static Serialization getSerializationById(Byte id) {
        return ID_SERIALIZATION_MAP.get(id);
    }

    /**
     * Gets serialization.
     *
     * @param url the url
     * @return the serialization
     */
    public static Serialization getSerialization(URL url) {
        // todo
        return new RpcSerialization();
    }

    /**
     * Gets serialization.
     *
     * @param url the url
     * @param id  the id
     * @return the serialization
     */
    public static Serialization getSerialization(URL url, Byte id) {
        Serialization result = getSerializationById(id);
        if (result == null) {
            result = getSerialization(url);
        }
        return result;
    }
}
