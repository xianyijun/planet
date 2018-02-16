package cn.xianyijun.planet.config.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.List;

/**
 * The type Abstract service config.
 *
 * @author xianyijun
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class AbstractServiceConfig extends AbstractInterfaceConfig {

    /**
     * The Version.
     */
    protected String version;

    /**
     * The Group.
     */
    protected String group;

    /**
     * The Deprecated.
     */
    protected Boolean deprecated;

    /**
     * The Delay.
     */
    protected Integer delay;

    /**
     * The Export.
     */
    protected Boolean export;

    /**
     * The Weight.
     */
    protected Integer weight;

    /**
     * The Document.
     */
    protected String document;

    /**
     * The Dynamic.
     */
    protected Boolean dynamic;

    /**
     * The Token.
     */
    protected String token;

    protected String accesslog;

    private Integer callbacks;

    private String scope;

    private Integer executes;

    private Boolean register;

    /**
     * The Protocols.
     */
    protected List<ProtocolConfig> protocols;

    /**
     * Sets protocol.
     *
     * @param protocol the protocol
     */
    public void setProtocol(ProtocolConfig protocol) {
        this.protocols = Arrays.asList(new ProtocolConfig[]{protocol});
    }

    /**
     * Sets protocols.
     *
     * @param protocols the protocols
     */
    public void setProtocols(List<? extends ProtocolConfig> protocols) {
        this.protocols = (List<ProtocolConfig>) protocols;
    }
}
