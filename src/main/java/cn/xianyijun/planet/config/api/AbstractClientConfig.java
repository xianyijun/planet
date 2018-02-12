package cn.xianyijun.planet.config.api;

import cn.xianyijun.planet.utils.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The type Abstract client config.
 *
 * @author xianyijun
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class AbstractClientConfig extends AbstractInterfaceConfig{

    /**
     * The Check.
     */
    protected Boolean check;

    /**
     * The Init.
     */
    protected boolean init;

    /**
     * The In jvm.
     */
    protected Boolean inJVM;

    /**
     * The Lazy.
     */
    protected Boolean lazy;

    /**
     * The Re connect.
     */
    protected String reConnect;

    /**
     * The Sticky.
     */
    protected Boolean sticky;

    /**
     * The Version.
     */
    protected String version;

    /**
     * The Group.
     */
    protected String group;

    protected Boolean stubEvent;


    @Override
    public void setOnConnect(String onConnect) {
        if (!StringUtils.isBlank(onConnect)) {
            this.stubEvent = true;
        }
        super.setOnConnect(onConnect);
    }
}
