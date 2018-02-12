package cn.xianyijun.planet.exception;

/**
 * Created by xianyijun on 2017/10/22.
 */
public final class RpcException extends RuntimeException{

    /**
     * The constant UNKNOWN_EXCEPTION.
     */
    public static final int UNKNOWN_EXCEPTION = 0;
    /**
     * The constant NETWORK_EXCEPTION.
     */
    public static final int NETWORK_EXCEPTION = 1;
    /**
     * The constant TIMEOUT_EXCEPTION.
     */
    public static final int TIMEOUT_EXCEPTION = 2;
    /**
     * The constant BIZ_EXCEPTION.
     */
    public static final int BIZ_EXCEPTION = 3;
    /**
     * The constant FORBIDDEN_EXCEPTION.
     */
    public static final int FORBIDDEN_EXCEPTION = 4;
    /**
     * The constant SERIALIZATION_EXCEPTION.
     */
    public static final int SERIALIZATION_EXCEPTION = 5;
    private static final long serialVersionUID = 7815426752583648734L;
    private int code; // RpcException不能有子类，异常类型用ErrorCode表示，以便保持兼容。

    /**
     * Instantiates a new Rpc exception.
     */
    public RpcException() {
        super();
    }

    /**
     * Instantiates a new Rpc exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Rpc exception.
     *
     * @param message the message
     */
    public RpcException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Rpc exception.
     *
     * @param cause the cause
     */
    public RpcException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Rpc exception.
     *
     * @param code the code
     */
    public RpcException(int code) {
        super();
        this.code = code;
    }

    /**
     * Instantiates a new Rpc exception.
     *
     * @param code    the code
     * @param message the message
     * @param cause   the cause
     */
    public RpcException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Instantiates a new Rpc exception.
     *
     * @param code    the code
     * @param message the message
     */
    public RpcException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Instantiates a new Rpc exception.
     *
     * @param code  the code
     * @param cause the cause
     */
    public RpcException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Sets code.
     *
     * @param code the code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Is biz boolean.
     *
     * @return the boolean
     */
    public boolean isBiz() {
        return code == BIZ_EXCEPTION;
    }

    /**
     * Is forbidded boolean.
     *
     * @return the boolean
     */
    public boolean isForbidded() {
        return code == FORBIDDEN_EXCEPTION;
    }

    /**
     * Is timeout boolean.
     *
     * @return the boolean
     */
    public boolean isTimeout() {
        return code == TIMEOUT_EXCEPTION;
    }

    /**
     * Is network boolean.
     *
     * @return the boolean
     */
    public boolean isNetwork() {
        return code == NETWORK_EXCEPTION;
    }

    /**
     * Is serialization boolean.
     *
     * @return the boolean
     */
    public boolean isSerialization() {
        return code == SERIALIZATION_EXCEPTION;
    }
}