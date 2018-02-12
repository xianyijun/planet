package cn.xianyijun.planet.remoting.api.exchange.support;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.exception.TimeoutException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.exchange.Request;
import cn.xianyijun.planet.remoting.api.exchange.Response;
import cn.xianyijun.planet.remoting.api.exchange.ResponseCallback;
import cn.xianyijun.planet.remoting.api.exchange.ResponseFuture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The type Default future.
 * @author xianyijun
 */
@Slf4j
@Getter
public class DefaultFuture implements ResponseFuture {

    private static final Map<Long, Channel> CHANNELS = new ConcurrentHashMap<Long, Channel>();

    private static final Map<Long, DefaultFuture> FUTURES = new ConcurrentHashMap<>();

    private final long id;
    private final Channel channel;
    private final Request request;
    private final int timeout;
    private final Lock lock = new ReentrantLock();
    private final Condition done = lock.newCondition();
    private final long start = System.currentTimeMillis();
    private volatile long sent;
    private volatile Response response;
    private volatile ResponseCallback callback;

    static {
        Thread th = new Thread(new RemotingInvocationTimeoutScan(), "RpcResponseTimeoutScanTimer");
        th.setDaemon(true);
        th.start();
    }

    /**
     * Instantiates a new Default future.
     *
     * @param channel the channel
     * @param request the request
     * @param timeout the timeout
     */
    public DefaultFuture(Channel channel, Request request, int timeout) {
        this.channel = channel;
        this.request = request;
        this.id = request.getId();
        this.timeout = timeout > 0 ? timeout : channel.getUrl().getPositiveParameter(Constants.TIMEOUT_KEY, Constants.DEFAULT_TIMEOUT);
        FUTURES.put(id, this);
        CHANNELS.put(id, channel);
    }

    @Override
    public Object get() throws RemotingException {
        return get(timeout);
    }

    /**
     * Has future boolean.
     *
     * @param channel the channel
     * @return the boolean
     */
    public static boolean hasFuture(Channel channel) {
        return CHANNELS.containsValue(channel);
    }

    @Override
    public Object get(int timeout) throws RemotingException {
        if (timeout <= 0){
            timeout = Constants.DEFAULT_TIMEOUT;
        }

        if (!isDone()){
            long start = System.currentTimeMillis();
            lock.lock();
            try {
                while (!isDone()) {
                    done.await(timeout, TimeUnit.MILLISECONDS);
                    if (isDone() || System.currentTimeMillis() - start > timeout) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
            if (!isDone()) {
                throw new TimeoutException(sent > 0, channel, getTimeoutMessage(false));
            }
        }
        return returnFromResponse();
    }

    private Object returnFromResponse() throws RemotingException {
        Response res = response;
        if (res == null) {
            throw new IllegalStateException("response cannot be null");
        }
        if (res.getStatus() == Response.OK) {
            return res.getResult();
        }
        if (res.getStatus() == Response.CLIENT_TIMEOUT || res.getStatus() == Response.SERVER_TIMEOUT) {
            throw new TimeoutException(res.getStatus() == Response.SERVER_TIMEOUT, channel, res.getErrorMsg());
        }
        throw new RemotingException(channel, res.getErrorMsg());
    }

    @Override
    public void setCallback(ResponseCallback callback) {

        if (isDone()) {
            invokeCallback(callback);
        } else {
            boolean isdone = false;
            lock.lock();
            try {
                if (!isDone()) {
                    this.callback = callback;
                } else {
                    isdone = true;
                }
            } finally {
                lock.unlock();
            }
            if (isdone) {
                invokeCallback(callback);
            }
        }
    }

    private void invokeCallback(ResponseCallback c) {
        ResponseCallback callbackCopy = c;
        if (callbackCopy == null) {
            throw new NullPointerException("callback cannot be null.");
        }
        c = null;
        Response res = response;
        if (res == null) {
            throw new IllegalStateException("response cannot be null. url:" + channel.getUrl());
        }

        if (res.getStatus() == Response.OK) {
            try {
                callbackCopy.done(res.getResult());
            } catch (Exception e) {
                log.error("callback invoke error .reasult:" + res.getResult() + ",url:" + channel.getUrl(), e);
            }
        } else if (res.getStatus() == Response.CLIENT_TIMEOUT || res.getStatus() == Response.SERVER_TIMEOUT) {
            try {
                TimeoutException te = new TimeoutException(res.getStatus() == Response.SERVER_TIMEOUT, channel, res.getErrorMsg());
                callbackCopy.caught(te);
            } catch (Exception e) {
                log.error("callback invoke error ,url:" + channel.getUrl(), e);
            }
        } else {
            try {
                RuntimeException re = new RuntimeException(res.getErrorMsg());
                callbackCopy.caught(re);
            } catch (Exception e) {
                log.error("callback invoke error ,url:" + channel.getUrl(), e);
            }
        }
    }

    private boolean isSent() {
        return sent > 0;
    }

    @Override
    public boolean isDone() {
        return response != null;
    }

    /**
     * Sent.
     *
     * @param channel the channel
     * @param request the request
     */
    public static void sent(Channel channel, Request request) {
        DefaultFuture future = FUTURES.get(request.getId());
        if (future != null) {
            future.doSent();
        }
    }

    /**
     * Cancel.
     */
    public void cancel() {
        Response errorResult = new Response(id);
        errorResult.setErrorMsg("request future has been canceled.");
        response = errorResult;
        FUTURES.remove(id);
        CHANNELS.remove(id);
    }

    private String getTimeoutMessage(boolean scan) {
        long nowTimestamp = System.currentTimeMillis();
        return (sent > 0 ? "Waiting server-side response timeout" : "Sending request timeout in client-side")
                + (scan ? " by scan timer" : "") + ". start time: "
                + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(start))) + ", end time: "
                + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())) + ","
                + (sent > 0 ? " client elapsed: " + (sent - start)
                + " ms, server elapsed: " + (nowTimestamp - sent)
                : " elapsed: " + (nowTimestamp - start)) + " ms, timeout: "
                + timeout + " ms, request: " + request + ", channel: " + channel.getLocalAddress()
                + " -> " + channel.getRemoteAddress();
    }

    /**
     * Received.
     *
     * @param channel  the channel
     * @param response the response
     */
    public static void received(Channel channel, Response response) {
        log.info("[DefaultFuture] received channel : {} , response : {}", channel, response);
        try {
            DefaultFuture future = FUTURES.remove(response.getId());
            if (future != null) {
                future.doReceived(response);
            } else {
                log.warn("The timeout response finally returned at "
                        + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()))
                        + ", response " + response
                        + (channel == null ? "" : ", channel: " + channel.getLocalAddress()
                        + " -> " + channel.getRemoteAddress()));
            }
        } finally {
            CHANNELS.remove(response.getId());
        }
    }

    private void doReceived(Response res) {
        lock.lock();
        try {
            response = res;
            if (done != null) {
                done.signal();
            }
        } finally {
            lock.unlock();
        }
        if (callback != null) {
            invokeCallback(callback);
        }
    }

    private void doSent() {
        sent = System.currentTimeMillis();
    }

    private static class RemotingInvocationTimeoutScan implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    for (DefaultFuture future : FUTURES.values()) {
                        log.debug("[RemotingInvocationTimeoutScan] running , future :{} ",future);
                        if (future == null || future.isDone()) {
                            continue;
                        }
                        if (System.currentTimeMillis() - future.getStart() > future.getTimeout()) {
                            Response timeoutResponse = new Response(future.getId());
                            timeoutResponse.setStatus(future.isSent() ? Response.SERVER_TIMEOUT : Response.CLIENT_TIMEOUT);
                            timeoutResponse.setErrorMessage(future.getTimeoutMessage(true));
                            DefaultFuture.received(future.getChannel(), timeoutResponse);
                        }
                    }
                    Thread.sleep(30);
                } catch (Throwable e) {
                    log.error("Exception when scan the timeout invocation of remoting.", e);
                }
            }
        }
    }

    public static DefaultFuture getFuture(long id) {
        return FUTURES.get(id);
    }

}
