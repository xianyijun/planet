package cn.xianyijun.planet.common.serialize;

import java.io.IOException;

/**
 * The interface Object output.
 */
public interface ObjectOutput extends DataOutput {

    /**
     * Write object.
     *
     * @param obj the obj
     * @throws IOException the io exception
     */
    void writeObject(Object obj) throws IOException;

}
