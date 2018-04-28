package cn.xianyijun.planet.config.api;

import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The type Abstract service config.
 *
 * @author xianyijun
 */
@Data
@EqualsAndHashCode(callSuper = false)
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
     * The Dynamic.
     */
    protected Boolean dynamic;

    /**
     * The Token.
     */
    protected String token;

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
        this.protocols = Collections.singletonList(protocol);
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
