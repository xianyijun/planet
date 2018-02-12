package cn.xianyijun.planet.rpc.api;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Rpc invocation.
 *
 * @author xianyijun
 */
public class RpcInvocation implements Invocation, Serializable{

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] arguments;

    private Map<String, String> attachments;

    private transient Invoker<?> invoker;

    /**
     * Instantiates a new Rpc invocation.
     */
    public RpcInvocation() {
    }

    /**
     * Instantiates a new Rpc invocation.
     *
     * @param invocation the invocation
     * @param invoker    the invoker
     */
    public RpcInvocation(Invocation invocation, Invoker<?> invoker) {
        this(invocation.getMethodName(), invocation.getParameterTypes(),
                invocation.getArguments(), new HashMap<>(invocation.getAttachments()),
                invocation.getInvoker());
        if (invoker != null) {
            URL url = invoker.getUrl();
            setAttachment(Constants.PATH_KEY, url.getPath());
            if (url.hasParameter(Constants.INTERFACE_KEY)) {
                setAttachment(Constants.INTERFACE_KEY, url.getParameter(Constants.INTERFACE_KEY));
            }
            if (url.hasParameter(Constants.GROUP_KEY)) {
                setAttachment(Constants.GROUP_KEY, url.getParameter(Constants.GROUP_KEY));
            }
            if (url.hasParameter(Constants.VERSION_KEY)) {
                setAttachment(Constants.VERSION_KEY, url.getParameter(Constants.VERSION_KEY, "0.0.0"));
            }
            if (url.hasParameter(Constants.TIMEOUT_KEY)) {
                setAttachment(Constants.TIMEOUT_KEY, url.getParameter(Constants.TIMEOUT_KEY));
            }
            if (url.hasParameter(Constants.TOKEN_KEY)) {
                setAttachment(Constants.TOKEN_KEY, url.getParameter(Constants.TOKEN_KEY));
            }
            if (url.hasParameter(Constants.APPLICATION_KEY)) {
                setAttachment(Constants.APPLICATION_KEY, url.getParameter(Constants.APPLICATION_KEY));
            }
        }
    }

    /**
     * Instantiates a new Rpc invocation.
     *
     * @param invocation the invocation
     */
    public RpcInvocation(Invocation invocation) {
        this(invocation.getMethodName(), invocation.getParameterTypes(),
                invocation.getArguments(), invocation.getAttachments(), invocation.getInvoker());
    }

    /**
     * Instantiates a new Rpc invocation.
     *
     * @param method    the method
     * @param arguments the arguments
     */
    public RpcInvocation(Method method, Object[] arguments) {
        this(method.getName(), method.getParameterTypes(), arguments, null, null);
    }

    /**
     * Instantiates a new Rpc invocation.
     *
     * @param method     the method
     * @param arguments  the arguments
     * @param attachment the attachment
     */
    public RpcInvocation(Method method, Object[] arguments, Map<String, String> attachment) {
        this(method.getName(), method.getParameterTypes(), arguments, attachment, null);
    }

    /**
     * Instantiates a new Rpc invocation.
     *
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @param arguments      the arguments
     */
    public RpcInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments) {
        this(methodName, parameterTypes, arguments, null, null);
    }

    /**
     * Instantiates a new Rpc invocation.
     *
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @param arguments      the arguments
     * @param attachments    the attachments
     */
    public RpcInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments, Map<String, String> attachments) {
        this(methodName, parameterTypes, arguments, attachments, null);
    }

    /**
     * Instantiates a new Rpc invocation.
     *
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @param arguments      the arguments
     * @param attachments    the attachments
     * @param invoker        the invoker
     */
    public RpcInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments, Map<String, String> attachments, Invoker<?> invoker) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
        this.arguments = arguments == null ? new Object[0] : arguments;
        this.attachments = attachments == null ? new HashMap<>() : attachments;
        this.invoker = invoker;
    }

    @Override
    public Invoker<?> getInvoker() {
        return invoker;
    }

    /**
     * Sets invoker.
     *
     * @param invoker the invoker
     */
    public void setInvoker(Invoker<?> invoker) {
        this.invoker = invoker;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets method name.
     *
     * @param methodName the method name
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Sets parameter types.
     *
     * @param parameterTypes the parameter types
     */
    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * Sets arguments.
     *
     * @param arguments the arguments
     */
    public void setArguments(Object[] arguments) {
        this.arguments = arguments == null ? new Object[0] : arguments;
    }

    @Override
    public Map<String, String> getAttachments() {
        return attachments;
    }

    /**
     * Sets attachments.
     *
     * @param attachments the attachments
     */
    public void setAttachments(Map<String, String> attachments) {
        this.attachments = attachments == null ? new HashMap<String, String>() : attachments;
    }

    /**
     * Sets attachment.
     *
     * @param key   the key
     * @param value the value
     */
    public void setAttachment(String key, String value) {
        if (attachments == null) {
            attachments = new HashMap<String, String>();
        }
        attachments.put(key, value);
    }

    /**
     * Sets attachment if absent.
     *
     * @param key   the key
     * @param value the value
     */
    public void setAttachmentIfAbsent(String key, String value) {
        if (attachments == null) {
            attachments = new HashMap<String, String>();
        }
        if (!attachments.containsKey(key)) {
            attachments.put(key, value);
        }
    }

    /**
     * Add attachments.
     *
     * @param attachments the attachments
     */
    public void addAttachments(Map<String, String> attachments) {
        if (attachments == null) {
            return;
        }
        if (this.attachments == null) {
            this.attachments = new HashMap<String, String>();
        }
        this.attachments.putAll(attachments);
    }

    /**
     * Add attachments if absent.
     *
     * @param attachments the attachments
     */
    public void addAttachmentsIfAbsent(Map<String, String> attachments) {
        if (attachments == null) {
            return;
        }
        for (Map.Entry<String, String> entry : attachments.entrySet()) {
            setAttachmentIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String getAttachment(String key) {
        if (attachments == null) {
            return null;
        }
        return attachments.get(key);
    }

    @Override
    public String getAttachment(String key, String defaultValue) {
        if (attachments == null) {
            return defaultValue;
        }
        String value = attachments.get(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public String toString() {
        return "RpcInvocation [methodName=" + methodName + ", parameterTypes="
                + Arrays.toString(parameterTypes) + ", arguments=" + Arrays.toString(arguments)
                + ", attachments=" + attachments + "]";
    }

}
