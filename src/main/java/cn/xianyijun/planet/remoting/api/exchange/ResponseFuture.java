package cn.xianyijun.planet.remoting.api.exchange;

import cn.xianyijun.planet.exception.RemotingException;

/**
 * The interface Response future.
 */
public interface ResponseFuture {
    /**
     * Get object.
     *
     * @return the object
     * @throws RemotingException the remoting exception
     */
    Object get() throws RemotingException;

    /**
     * Get object.
     *
     * @param timeoutInMillis the timeout in millis
     * @return the object
     * @throws RemotingException the remoting exception
     */
    Object get(int timeoutInMillis) throws RemotingException;

    /**
     * Sets callback.
     *
     * @param callback the callback
     */
    void setCallback(ResponseCallback callback);

    /**
     * Is done boolean.
     *
     * @return the boolean
     */
    boolean isDone();
}

