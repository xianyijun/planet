package cn.xianyijun.planet.rpc.api;

import java.util.Map;

/**
 * The interface Invocation.
 */
public interface Invocation {

    /**
     * Gets method name.
     *
     * @return the method name
     */
    String getMethodName();

    /**
     * Get parameter types class [ ].
     *
     * @return the class [ ]
     */
    Class<?>[] getParameterTypes();

    /**
     * Get arguments object [ ].
     *
     * @return the object [ ]
     */
    Object[] getArguments();

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

    /**
     * Gets invoker.
     *
     * @return the invoker
     */
    Invoker<?> getInvoker();

}