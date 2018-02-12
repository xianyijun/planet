package cn.xianyijun.planet.rpc.api;

import cn.xianyijun.planet.common.URL;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * The type Rpc context.
 * @author xianyijun
 */
@Getter
@Setter
public class RpcContext {
    private static final ThreadLocal<RpcContext> LOCAL = ThreadLocal.withInitial(RpcContext::new);

    private final Map<String, String> attachments = new HashMap<>();
    private final Map<String, Object> values = new HashMap<>();
    private Future<?> future;

    private List<URL> urls;

    private URL url;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] arguments;

    private InetSocketAddress localAddress;

    private InetSocketAddress remoteAddress;

    /**
     * Gets context.
     *
     * @return the context
     */
    public static RpcContext getContext() {
        return LOCAL.get();
    }

    /**
     * Remove context.
     */
    public static void removeContext() {
        LOCAL.remove();
    }

    /**
     * Gets attachment.
     *
     * @param key the key
     * @return the attachment
     */
    public String getAttachment(String key) {
        return attachments.get(key);
    }

    /**
     * Sets attachment.
     *
     * @param key   the key
     * @param value the value
     * @return the attachment
     */
    public RpcContext setAttachment(String key, String value) {
        if (value == null) {
            attachments.remove(key);
        } else {
            attachments.put(key, value);
        }
        return this;
    }

    /**
     * Remove attachment rpc context.
     *
     * @param key the key
     * @return the rpc context
     */
    public RpcContext removeAttachment(String key) {
        attachments.remove(key);
        return this;
    }

    /**
     * Gets attachments.
     *
     * @return the attachments
     */
    public Map<String, String> getAttachments() {
        return attachments;
    }


    /**
     * Sets remote address.
     *
     * @param host the host
     * @param port the port
     * @return the remote address
     */
    public RpcContext setRemoteAddress(String host, int port) {
        if (port < 0) {
            port = 0;
        }
        this.remoteAddress = InetSocketAddress.createUnresolved(host, port);
        return this;
    }

    public RpcContext setUrl(URL url){
        if (url != null){
            this.url = url;
        }
        return this;
    }

    public RpcContext setInvocation(Invocation invocation){
        if (invocation != null){
            setMethodName(invocation.getMethodName());
            setParameterTypes(invocation.getParameterTypes());
            setArguments(invocation.getArguments());
        }
        return this;
    }

    public RpcContext setLocalAddress(String host, int port) {
        if (port < 0) {
            port = 0;
        }
        this.localAddress = InetSocketAddress.createUnresolved(host, port);
        return this;
    }

    public RpcContext setAttachments(Map<String, String> attachment) {
        this.attachments.clear();
        if (attachment != null && attachment.size() > 0) {
            this.attachments.putAll(attachment);
        }
        return this;
    }
}
