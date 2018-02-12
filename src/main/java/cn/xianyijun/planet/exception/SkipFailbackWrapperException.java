package cn.xianyijun.planet.exception;

/**
 * Created by xianyijun on 2018/1/27.
 */
public class SkipFailbackWrapperException extends RuntimeException {
    /**
     * Instantiates a new Skip failback wrapper exception.
     *
     * @param cause the cause
     */
    public SkipFailbackWrapperException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
