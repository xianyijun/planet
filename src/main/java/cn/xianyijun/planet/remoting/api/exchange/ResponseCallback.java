package cn.xianyijun.planet.remoting.api.exchange;

/**
 * The interface Response callback.
 */
public interface ResponseCallback {
    /**
     * Done.
     *
     * @param response the response
     */
    void done(Object response);

    /**
     * Caught.
     *
     * @param exception the exception
     */
    void caught(Throwable exception);
}
