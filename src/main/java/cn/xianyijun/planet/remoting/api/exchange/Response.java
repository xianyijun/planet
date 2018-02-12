package cn.xianyijun.planet.remoting.api.exchange;

import lombok.Data;
import lombok.ToString;

/**
 * The type Response.
 */
@Data
@ToString
public class Response {
    /**
     * The constant HEARTBEAT_EVENT.
     */
    public static final String HEARTBEAT_EVENT = null;

    /**
     * The constant READONLY_EVENT.
     */
    public static final String READONLY_EVENT = "R";

    /**
     * The constant OK.
     */
    public static final byte OK = 20;

    /**
     * The constant CLIENT_TIMEOUT.
     */
    public static final byte CLIENT_TIMEOUT = 30;

    /**
     * The constant SERVER_TIMEOUT.
     */
    public static final byte SERVER_TIMEOUT = 31;

    /**
     * The constant BAD_REQUEST.
     */
    public static final byte BAD_REQUEST = 40;

    /**
     * The constant BAD_RESPONSE.
     */
    public static final byte BAD_RESPONSE = 50;

    /**
     * The constant SERVICE_NOT_FOUND.
     */
    public static final byte SERVICE_NOT_FOUND = 60;

    /**
     * The constant SERVICE_ERROR.
     */
    public static final byte SERVICE_ERROR = 70;

    /**
     * The constant SERVER_ERROR.
     */
    public static final byte SERVER_ERROR = 80;

    /**
     * The constant CLIENT_ERROR.
     */
    public static final byte CLIENT_ERROR      = 90;

    /**
     * The constant SERVER_THREAD_POOL_EXHAUSTED_ERROR.
     */
    public static final byte SERVER_THREAD_POOL_EXHAUSTED_ERROR      = 100;

    private long id = 0;

    private String version;

    private byte status = OK;

    private boolean event = false;

    private String errorMsg;

    private Object result;


    /**
     * Instantiates a new Response.
     *
     * @param id the id
     */
    public Response(long id) {
        this.id = id;
    }

    /**
     * Instantiates a new Response.
     *
     * @param id      the id
     * @param version the version
     */
    public Response(long id, String version) {
        this.id = id;
        this.version = version;
    }

    /**
     * Sets event.
     *
     * @param event the event
     */
    public void setEvent(String event) {
        this.event = true;
        result = event;
    }

    /**
     * Is heartbeat boolean.
     *
     * @return the boolean
     */
    public boolean isHeartbeat() {
        return event && HEARTBEAT_EVENT == result;
    }

    /**
     * Sets error message.
     *
     * @param msg the msg
     */
    public void setErrorMessage(String msg) {
        this.errorMsg = msg;
    }
}
