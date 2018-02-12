package cn.xianyijun.planet.remoting.api.exchange;


import cn.xianyijun.planet.utils.StringUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The type Request.
 */
public class Request {
    /**
     * The constant HEARTBEAT_EVENT.
     */
    public static final String HEARTBEAT_EVENT = null;

    /**
     * The constant READONLY_EVENT.
     */
    public static final String READONLY_EVENT = "R";

    private static final AtomicLong INVOKE_ID = new AtomicLong(0);

    private final long mId;

    private String mVersion;

    private boolean mTwoWay = true;

    private boolean mEvent = false;

    private boolean mBroken = false;

    private Object mData;

    /**
     * Instantiates a new Request.
     */
    public Request() {
        mId = newId();
    }

    /**
     * Instantiates a new Request.
     *
     * @param id the id
     */
    public Request(long id) {
        mId = id;
    }

    private static long newId() {
        return INVOKE_ID.getAndIncrement();
    }

    private static String safeToString(Object data) {
        if (data == null) {
            return null;
        }
        String dataStr;
        try {
            dataStr = data.toString();
        } catch (Throwable e) {
            dataStr = "<Fail toString of " + data.getClass() + ", cause: " +
                    StringUtils.toString(e) + ">";
        }
        return dataStr;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public long getId() {
        return mId;
    }

    /**
     * Gets version.
     *
     * @return the version
     */
    public String getVersion() {
        return mVersion;
    }

    /**
     * Sets version.
     *
     * @param version the version
     */
    public void setVersion(String version) {
        mVersion = version;
    }

    /**
     * Is two way boolean.
     *
     * @return the boolean
     */
    public boolean isTwoWay() {
        return mTwoWay;
    }

    /**
     * Sets two way.
     *
     * @param twoWay the two way
     */
    public void setTwoWay(boolean twoWay) {
        mTwoWay = twoWay;
    }

    /**
     * Is event boolean.
     *
     * @return the boolean
     */
    public boolean isEvent() {
        return mEvent;
    }

    /**
     * Sets event.
     *
     * @param event the event
     */
    public void setEvent(String event) {
        mEvent = true;
        mData = event;
    }

    /**
     * Is broken boolean.
     *
     * @return the boolean
     */
    public boolean isBroken() {
        return mBroken;
    }

    /**
     * Sets broken.
     *
     * @param mBroken the m broken
     */
    public void setBroken(boolean mBroken) {
        this.mBroken = mBroken;
    }

    /**
     * Gets data.
     *
     * @return the data
     */
    public Object getData() {
        return mData;
    }

    /**
     * Sets data.
     *
     * @param msg the msg
     */
    public void setData(Object msg) {
        mData = msg;
    }

    /**
     * Is heartbeat boolean.
     *
     * @return the boolean
     */
    public boolean isHeartbeat() {
        return mEvent && HEARTBEAT_EVENT == mData;
    }

    /**
     * Sets heartbeat.
     *
     * @param isHeartbeat the is heartbeat
     */
    public void setHeartbeat(boolean isHeartbeat) {
        if (isHeartbeat) {
            setEvent(HEARTBEAT_EVENT);
        }
    }

    @Override
    public String toString() {
        return "Request [id=" + mId + ", version=" + mVersion + ", twoway=" + mTwoWay + ", event=" + mEvent
                + ", broken=" + mBroken + ", data=" + (mData == this ? "this" : safeToString(mData)) + "]";
    }
}