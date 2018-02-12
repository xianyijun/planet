package cn.xianyijun.planet.config.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

/**
 * The type Registry config.
 */
@Data
@ToString
@EqualsAndHashCode(callSuper=false)
public class RegistryConfig extends AbstractConfig {

    /**
     * The constant NO_AVAILABLE.
     */
    public static final String NO_AVAILABLE = "N/A";

    private String address;

    private String username;

    private String password;

    private Integer port;

    private String protocol;

    private String transporter;

    private String server;

    private String client;

    private String group;

    private String version;

    private Integer timeout;

    private Integer sessionTime;

    private String file;

    private Integer waitTime;

    private Boolean check;

    private Boolean dynamic;

    private Boolean register;

    private Boolean subscribe;

    private Map<String,String> parameters;

    private Boolean isDefault;

}
