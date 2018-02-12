package cn.xianyijun.planet.rpc.rpc;

import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.remoting.api.exchange.ResponseFuture;
import cn.xianyijun.planet.rpc.api.Result;
import cn.xianyijun.planet.utils.StringUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The type Future adapter.
 *
 * @param <V> the type parameter
 * @author xianyijun
 */
public class FutureAdapter<V> implements Future<V> {
    private final ResponseFuture future;

    /**
     * Instantiates a new Future adapter.
     *
     * @param future the future
     */
    public FutureAdapter(ResponseFuture future) {
        this.future = future;
    }

    /**
     * Gets future.
     *
     * @return the future
     */
    public ResponseFuture getFuture() {
        return future;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get() throws InterruptedException, ExecutionException {
        try {
            return (V) (((Result) future.get()).recreate());
        } catch (RemotingException e) {
            throw new ExecutionException(e.getMessage(), e);
        } catch (Throwable e) {
            throw new RpcException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        int timeoutInMillis = (int) unit.convert(timeout, TimeUnit.MILLISECONDS);
        try {
            return (V) (((Result) future.get(timeoutInMillis)).recreate());
        } catch (TimeoutException e) {
            throw new TimeoutException(StringUtils.toString(e));
        } catch (RemotingException e) {
            throw new ExecutionException(e.getMessage(), e);
        } catch (Throwable e) {
            throw new RpcException(e);
        }
    }
}
