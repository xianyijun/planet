package cn.xianyijun.planet.common.serialize;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * The interface Object input.
 */
public interface ObjectInput extends DataInput {
    /**
     * Read object object.
     *
     * @return the object
     * @throws IOException            the io exception
     * @throws ClassNotFoundException the class not found exception
     */
    Object readObject() throws IOException, ClassNotFoundException;

    /**
     * Read object t.
     *
     * @param <T> the type parameter
     * @param cls the cls
     * @return the t
     * @throws IOException            the io exception
     * @throws ClassNotFoundException the class not found exception
     */
    <T> T readObject(Class<T> cls) throws IOException, ClassNotFoundException;

    /**
     * Read object t.
     *
     * @param <T>  the type parameter
     * @param cls  the cls
     * @param type the type
     * @return the t
     * @throws IOException            the io exception
     * @throws ClassNotFoundException the class not found exception
     */
    <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException;

}
