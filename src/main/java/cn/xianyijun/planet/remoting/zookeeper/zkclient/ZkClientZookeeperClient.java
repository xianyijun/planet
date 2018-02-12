package cn.xianyijun.planet.remoting.zookeeper.zkclient;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.remoting.zookeeper.ChildListener;
import cn.xianyijun.planet.remoting.zookeeper.StateListener;
import cn.xianyijun.planet.remoting.zookeeper.support.AbstractZookeeperClient;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 * The type Zk client zookeeper client.
 *
 * @author xianyijun
 * @date 2018 /2/4
 */
public class ZkClientZookeeperClient extends AbstractZookeeperClient<IZkChildListener> {
    private final ZkClientWrapper client;

    private volatile Watcher.Event.KeeperState state = Watcher.Event.KeeperState.SyncConnected;

    /**
     * Instantiates a new Zk client zookeeper client.
     *
     * @param url the url
     */
    public ZkClientZookeeperClient(URL url) {
        super(url);
        client = new ZkClientWrapper(url.getBackupAddress(), 30000);
        client.addListener(new IZkStateListener() {
            @Override
            public void handleStateChanged(Watcher.Event.KeeperState state) throws Exception {
                ZkClientZookeeperClient.this.state = state;
                if (state == Watcher.Event.KeeperState.Disconnected) {
                    stateChanged(StateListener.DISCONNECTED);
                } else if (state == Watcher.Event.KeeperState.SyncConnected) {
                    stateChanged(StateListener.CONNECTED);
                }
            }
            @Override
            public void handleNewSession() throws Exception {
                stateChanged(StateListener.RECONNECTED);
            }
        });
        client.start();
    }

    @Override
    protected void doClose() {
        client.close();
    }

    @Override
    protected void createPersistent(String path) {
        try {
            client.createPersistent(path);
        } catch (ZkNodeExistsException e) {
        }
    }

    @Override
    protected void createEphemeral(String path) {
        try {
            client.createEphemeral(path);
        } catch (ZkNodeExistsException e) {
        }
    }

    @Override
    protected boolean checkExists(String path) {
        try {
            return client.exists(path);
        } catch (Throwable t) {
        }
        return false;
    }

    @Override
    protected IZkChildListener createTargetChildListener(String path, ChildListener listener) {
        return listener::childChanged;
    }

    @Override
    protected List<String> addTargetChildListener(String path, IZkChildListener listener) {
        return client.subscribeChildChanges(path, listener);
    }

    @Override
    protected void removeTargetChildListener(String path, IZkChildListener listener) {
        client.unSubscribeChildChanges(path, listener);
    }

    @Override
    public void delete(String path) {
        try {
            client.delete(path);
        } catch (ZkNoNodeException e) {
        }
    }

    @Override
    public boolean isConnected() {
        return state == Watcher.Event.KeeperState.SyncConnected;
    }
}
