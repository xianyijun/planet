package cn.xianyijun.planet.remoting.zookeeper.support;


import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.remoting.zookeeper.ChildListener;
import cn.xianyijun.planet.remoting.zookeeper.StateListener;
import cn.xianyijun.planet.remoting.zookeeper.ZookeeperClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * The type Abstract zookeeper client.
 *
 * @param <TargetChildListener> the type parameter
 * @author xianyijun
 * @date 2018 /2/4
 */
@Slf4j
public abstract class AbstractZookeeperClient<TargetChildListener> implements ZookeeperClient {

    private final URL url;

    @Getter
    private final Set<StateListener> stateListeners = new CopyOnWriteArraySet<>();

    private final ConcurrentMap<String, ConcurrentMap<ChildListener, TargetChildListener>> childListeners = new ConcurrentHashMap<String, ConcurrentMap<ChildListener, TargetChildListener>>();

    private volatile boolean closed = false;

    /**
     * Instantiates a new Abstract zookeeper client.
     *
     * @param url the url
     */
    public AbstractZookeeperClient(URL url) {
        this.url = url;
    }

    @Override
    public URL getUrl() {
        return url;
    }


    @Override
    public void create(String path, boolean ephemeral) {
        int i = path.lastIndexOf('/');
        if (i > 0) {
            String parentPath = path.substring(0, i);
            if (!checkExists(parentPath)) {
                create(parentPath, false);
            }
        }
        if (ephemeral) {
            createEphemeral(path);
        } else {
            createPersistent(path);
        }
    }

    @Override
    public List<String> getChildren(String path) {
        return null;
    }

    @Override
    public List<String> addChildListener(String path, ChildListener listener) {
        ConcurrentMap<ChildListener, TargetChildListener> listeners = childListeners.get(path);
        if (listeners == null) {
            childListeners.putIfAbsent(path, new ConcurrentHashMap<>());
            listeners = childListeners.get(path);
        }
        TargetChildListener targetListener = listeners.get(listener);
        if (targetListener == null) {
            listeners.putIfAbsent(listener, createTargetChildListener(path, listener));
            targetListener = listeners.get(listener);
        }
        return addTargetChildListener(path, targetListener);
    }

    @Override
    public void removeChildListener(String path, ChildListener listener) {
        ConcurrentMap<ChildListener, TargetChildListener> listeners = childListeners.get(path);
        if (listeners != null) {
            TargetChildListener targetListener = listeners.remove(listener);
            if (targetListener != null) {
                removeTargetChildListener(path, targetListener);
            }
        }
    }

    @Override
    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }

    @Override
    public void removeStateListener(StateListener listener) {
        stateListeners.remove(listener);
    }

    @Override
    public void close() {
        if (closed){
            return;
        }

        closed = true;

        try {
            doClose();
        }catch (Throwable e){
            log.warn(e.getMessage(), e);
        }
    }

    /**
     * State changed.
     *
     * @param state the state
     */
    protected void stateChanged(int state) {
        for (StateListener sessionListener : getStateListeners()) {
            sessionListener.stateChanged(state);
        }
    }

    /**
     * Do close.
     */
    protected abstract void doClose();

    /**
     * Create persistent.
     *
     * @param path the path
     */
    protected abstract void createPersistent(String path);

    /**
     * Create ephemeral.
     *
     * @param path the path
     */
    protected abstract void createEphemeral(String path);

    /**
     * Check exists boolean.
     *
     * @param path the path
     * @return the boolean
     */
    protected abstract boolean checkExists(String path);

    /**
     * Create target child listener target child listener.
     *
     * @param path     the path
     * @param listener the listener
     * @return the target child listener
     */
    protected abstract TargetChildListener createTargetChildListener(String path, ChildListener listener);

    /**
     * Add target child listener list.
     *
     * @param path     the path
     * @param listener the listener
     * @return the list
     */
    protected abstract List<String> addTargetChildListener(String path, TargetChildListener listener);

    /**
     * Remove target child listener.
     *
     * @param path     the path
     * @param listener the listener
     */
    protected abstract void removeTargetChildListener(String path, TargetChildListener listener);

}
