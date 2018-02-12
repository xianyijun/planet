package cn.xianyijun.planet.common.serialize;


import cn.xianyijun.planet.common.URL;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The interface Serialization.
 * @author xianyijun
 */
public interface Serialization {
    /**
     * Gets content type id.
     *
     * @return the content type id
     */
    byte getContentTypeId();

    /**
     * Gets content type.
     *
     * @return the content type
     */
    String getContentType();

    /**
     * Serialize object output.
     *
     * @param url    the url
     * @param output the output
     * @return the object output
     * @throws IOException the io exception
     */
    ObjectOutput serialize(URL url, OutputStream output) throws IOException;

    /**
     * Deserialize object input.
     *
     * @param url   the url
     * @param input the input
     * @return the object input
     * @throws IOException the io exception
     */
    ObjectInput deserialize(URL url, InputStream input) throws IOException;

}
