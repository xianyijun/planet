package cn.xianyijun.planet.rpc.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Rpc result.
 * @author xianyijun
 */
public class RpcResult  implements Result, Serializable{
    private Object result;

    private Throwable exception;

    private Map<String, String> attachments = new HashMap<>();

    /**
     * Instantiates a new Rpc result.
     */
    public RpcResult() {
    }

    /**
     * Instantiates a new Rpc result.
     *
     * @param result the result
     */
    public RpcResult(Object result) {
        this.result = result;
    }

    /**
     * Instantiates a new Rpc result.
     *
     * @param exception the exception
     */
    public RpcResult(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public Object recreate() throws Throwable {
        if (exception != null) {
            throw exception;
        }
        return result;
    }

    /**
     * Gets result.
     *
     * @return the result
     */
    @Deprecated
    public Object getResult() {
        return getValue();
    }

    /**
     * Sets result.
     *
     * @param result the result
     */
    @Deprecated
    public void setResult(Object result) {
        setValue(result);
    }

    @Override
    public Object getValue() {
        return result;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(Object value) {
        this.result = value;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    /**
     * Sets exception.
     *
     * @param e the e
     */
    public void setException(Throwable e) {
        this.exception = e;
    }

    @Override
    public boolean hasException() {
        return exception != null;
    }

    @Override
    public Map<String, String> getAttachments() {
        return attachments;
    }

    /**
     * Sets attachments.
     *
     * @param map the map
     */
    public void setAttachments(Map<String, String> map) {
        if (map != null && map.size() > 0) {
            attachments.putAll(map);
        }
    }

    @Override
    public String getAttachment(String key) {
        return attachments.get(key);
    }

    @Override
    public String getAttachment(String key, String defaultValue) {
        String result = attachments.get(key);
        if (result == null || result.length() == 0) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Sets attachment.
     *
     * @param key   the key
     * @param value the value
     */
    public void setAttachment(String key, String value) {
        attachments.put(key, value);
    }

    @Override
    public String toString() {
        return "RpcResult [result=" + result + ", exception=" + exception + "]";
    }
}