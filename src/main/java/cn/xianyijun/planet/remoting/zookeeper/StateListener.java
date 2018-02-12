package cn.xianyijun.planet.remoting.zookeeper;

/**
 * Created by xianyijun on 2018/2/4.
 */
public interface StateListener {
    /**
     * The constant DISCONNECTED.
     */
    int DISCONNECTED = 0;

    /**
     * The constant CONNECTED.
     */
    int CONNECTED = 1;

    /**
     * The constant RECONNECTED.
     */
    int RECONNECTED = 2;

    /**
     * State changed.
     *
     * @param connected the connected
     */
    void stateChanged(int connected);
}
