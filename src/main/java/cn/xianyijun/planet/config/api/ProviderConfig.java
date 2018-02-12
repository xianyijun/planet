package cn.xianyijun.planet.config.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The type Provider config.
 *
 * @author xianyijun
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ProviderConfig extends AbstractServiceConfig {

    private String host;

    private Integer port;

    private String contextPath;

    private String threadPool;

    private Integer threads;

    private Integer ioThreads;

    private Integer queues;

    private Integer accepts;

    private String codec;

    private String serialization;

    private String charset;

    private Integer payload;

    private Integer buffer;

    private String transporter;

    private String exchanger;

    private String dispatcher;

    private String netWorker;

    private String server;

    private String client;

    private String telnet;

    private String prompt;

    private String status;

    private Integer wait;

    private Boolean isDefault;

}
