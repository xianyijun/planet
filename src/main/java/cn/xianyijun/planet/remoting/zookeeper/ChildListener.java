package cn.xianyijun.planet.remoting.zookeeper;

import java.util.List;

/**
 * The interface Child listener.
 *
 * @author xianyijun
 */
public interface ChildListener {

    /**
     * Child changed.
     *
     * @param path     the path
     * @param children the children
     */
    void childChanged(String path, List<String> children);
}