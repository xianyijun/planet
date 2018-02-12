package cn.xianyijun.planet.exception;

/**
 * Created by xianyijun on 2017/10/29.
 */
public class NoSuchMethodException extends RuntimeException {

    /**
     * Instantiates a new No such method exception.
     */
    public NoSuchMethodException() {
        super();
    }

    /**
     * Instantiates a new No such method exception.
     *
     * @param msg the msg
     */
    public NoSuchMethodException(String msg) {
        super(msg);
    }
}
