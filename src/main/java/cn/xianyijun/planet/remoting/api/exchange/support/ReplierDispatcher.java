package cn.xianyijun.planet.remoting.api.exchange.support;

import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Replier dispatcher.
 */
public class ReplierDispatcher implements Replier<Object> {

    private final Replier<?> defaultReplier;

    private final Map<Class<?>, Replier<?>> repliers = new ConcurrentHashMap<Class<?>, Replier<?>>();

    /**
     * Instantiates a new Replier dispatcher.
     */
    public ReplierDispatcher() {
        this(null, null);
    }

    /**
     * Instantiates a new Replier dispatcher.
     *
     * @param defaultReplier the default replier
     */
    public ReplierDispatcher(Replier<?> defaultReplier) {
        this(defaultReplier, null);
    }

    /**
     * Instantiates a new Replier dispatcher.
     *
     * @param defaultReplier the default replier
     * @param repliers       the repliers
     */
    public ReplierDispatcher(Replier<?> defaultReplier, Map<Class<?>, Replier<?>> repliers) {
        this.defaultReplier = defaultReplier;
        if (repliers != null && repliers.size() > 0) {
            this.repliers.putAll(repliers);
        }
    }
    @Override
    public Object reply(ExchangeChannel channel, Object request) throws RemotingException {
        return ((Replier) getReplier(request.getClass())).reply(channel, request);
    }


    /**
     * Add replier replier dispatcher.
     *
     * @param <T>     the type parameter
     * @param type    the type
     * @param replier the replier
     * @return the replier dispatcher
     */
    public <T> ReplierDispatcher addReplier(Class<T> type, Replier<T> replier) {
        repliers.put(type, replier);
        return this;
    }

    /**
     * Remove replier replier dispatcher.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @return the replier dispatcher
     */
    public <T> ReplierDispatcher removeReplier(Class<T> type) {
        repliers.remove(type);
        return this;
    }

    private Replier<?> getReplier(Class<?> type) {
        for (Map.Entry<Class<?>, Replier<?>> entry : repliers.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
                return entry.getValue();
            }
        }
        if (defaultReplier != null) {
            return defaultReplier;
        }
        throw new IllegalStateException("Replier not found, Unsupported message object: " + type);
    }
}
