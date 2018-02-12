package cn.xianyijun.planet.rpc.api;

import java.util.Map;

/**
 * The interface Rpc result.
 */
public interface Result {

    /**
     * Gets value.
     *
     * @return the value
     */
    Object getValue();

    /**
     * Gets exception.
     *
     * @return the exception
     */
    Throwable getException();

    /**
     * Has exception boolean.
     *
     * @return the boolean
     */
    boolean hasException();

    /**
     * Recreate object.
     *
     * @return the object
     * @throws Throwable the throwable
     */
    Object recreate() throws Throwable;

    /**
     * Gets attachments.
     *
     * @return the attachments
     */
    Map<String, String> getAttachments();

    /**
     * Gets attachment.
     *
     * @param key the key
     * @return the attachment
     */
    String getAttachment(String key);

    /**
     * Gets attachment.
     *
     * @param key          the key
     * @param defaultValue the default value
     * @return the attachment
     */
    String getAttachment(String key, String defaultValue);
}
