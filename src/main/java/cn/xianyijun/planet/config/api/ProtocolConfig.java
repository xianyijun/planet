package cn.xianyijun.planet.config.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type Protocol config.
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ProtocolConfig extends AbstractConfig{
    private String name;

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

    private Integer heartbeat;

    private String accessLog;

    private String transporter;

    private String exchanger;

    private String dispatcher;

    private String netWorker;

    private String server;

    private String client;

    private String telnet;

    private String prompt;

    private String status;

    private Boolean register;

    private Map<String, String> parameters;

    private Boolean isDefault;

    private static final AtomicBoolean destroyed = new AtomicBoolean(false);

    /**
     * Destroy all.
     */
    public static void destroyAll(){
        if (!destroyed.compareAndSet(false, true)) {
            return;
        }

    }
}
