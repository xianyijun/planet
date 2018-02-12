package cn.xianyijun.planet.config.api;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The type Method config.
 */
@Setter
public class MethodConfig extends AbstractMethodConfig {
    private String name;

    @Getter
    private Integer stat;

    @Getter
    private Boolean retry;

    @Getter
    private Boolean reliable;

    @Getter
    private Integer executes;

    @Getter
    private Boolean deprecated;

    @Getter
    private Boolean sticky;

    @Getter
    private Boolean isReturn;

    private Object onInvoke;

    private String onInvokeMethod;

    private Object onReturn;

    private String onReturnMethod;

    private Object onThrow;

    private String onThrowMethod;

    private List<ArgumentConfig> arguments;

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets onreturn.
     *
     * @return the onreturn
     */
    public Object getOnreturn() {
        return onReturn;
    }

    /**
     * Gets onreturn method.
     *
     * @return the onreturn method
     */
    public String getOnreturnMethod() {
        return onReturnMethod;
    }

    /**
     * Gets on throw.
     *
     * @return the on throw
     */
    public Object getOnThrow() {
        return onThrow;
    }

    /**
     * Gets on throw method.
     *
     * @return the on throw method
     */
    public String getOnThrowMethod() {
        return onThrowMethod;
    }

    /**
     * Gets on invoke.
     *
     * @return the on invoke
     */
    public Object getOnInvoke() {
        return onInvoke;
    }

    /**
     * Gets on invoke method.
     *
     * @return the on invoke method
     */
    public String getOnInvokeMethod() {
        return onInvokeMethod;
    }
}
