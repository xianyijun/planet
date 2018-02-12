package cn.xianyijun.planet.remoting.zookeeper.zkclient;

import cn.xianyijun.planet.common.concurrent.ListenableFutureTask;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.Watcher;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * The type Zk client wrapper.
 *
 * @author xianyijun
 * @date 2018 /2/4
 */
@Slf4j
public class ZkClientWrapper {
    private long timeout;
    private ZkClient client;
    private volatile Watcher.Event.KeeperState state;
    private ListenableFutureTask<ZkClient> listenableFutureTask;
    private volatile boolean started = false;


    /**
     * Instantiates a new Zk client wrapper.
     *
     * @param serverAddr the server addr
     * @param timeout    the timeout
     */
    public ZkClientWrapper(final String serverAddr, long timeout) {
        this.timeout = timeout;
        listenableFutureTask = ListenableFutureTask.create(() -> new ZkClient(serverAddr, Integer.MAX_VALUE));
    }

    /**
     * Start.
     */
    public void start() {
        if (!started) {
            Thread connectThread = new Thread(listenableFutureTask);
            connectThread.setName("RpcZkclientConnector");
            connectThread.setDaemon(true);
            connectThread.start();
            try {
                client = listenableFutureTask.get(timeout, TimeUnit.MILLISECONDS);
            } catch (Throwable t) {
                log.error("Timeout! zookeeper server can not be connected in : " + timeout + "ms!", t);
            }
            started = true;
        } else {
            log.warn("Zkclient has already been started!");
        }
    }

    /**
     * Add listener.
     *
     * @param listener the listener
     */
    public void addListener(final IZkStateListener listener) {
        listenableFutureTask.addListener((Runnable) () -> {
            try {
                client = listenableFutureTask.get();
                client.subscribeStateChanges(listener);
            } catch (InterruptedException e) {
                log.warn(Thread.currentThread().getName() + " was interrupted unexpectedly, which may cause unpredictable exception!");
            } catch (ExecutionException e) {
                log.error("Got an exception when trying to create zkclient instance, can not connect to zookeeper server, please check!", e);
            }
        });
    }

    /**
     * Is connected boolean.
     *
     * @return the boolean
     */
    public boolean isConnected() {
        return client != null && state == Watcher.Event.KeeperState.SyncConnected;
    }

    /**
     * Create persistent.
     *
     * @param path the path
     */
    public void createPersistent(String path) {
        client.createPersistent(path, true);
    }

    /**
     * Create ephemeral.
     *
     * @param path the path
     */
    public void createEphemeral(String path) {
        client.createEphemeral(path);
    }

    /**
     * Delete.
     *
     * @param path the path
     */
    public void delete(String path) {
        client.delete(path);
    }

    /**
     * Gets children.
     *
     * @param path the path
     * @return the children
     */
    public List<String> getChildren(String path) {
        return client.getChildren(path);
    }

    /**
     * Exists boolean.
     *
     * @param path the path
     * @return the boolean
     */
    public boolean exists(String path) {
        return client.exists(path);
    }

    /**
     * Close.
     */
    public void close() {
        client.close();
    }

    /**
     * Subscribe child changes list.
     *
     * @param path     the path
     * @param listener the listener
     * @return the list
     */
    public List<String> subscribeChildChanges(String path, final IZkChildListener listener) {
        return client.subscribeChildChanges(path, listener);
    }

    /**
     * Un subscribe child changes.
     *
     * @param path     the path
     * @param listener the listener
     */
    public void unSubscribeChildChanges(String path, IZkChildListener listener) {
        client.unsubscribeChildChanges(path, listener);
    }
}
