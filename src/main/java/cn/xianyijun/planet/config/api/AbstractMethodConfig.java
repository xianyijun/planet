package cn.xianyijun.planet.config.api;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The type Abstract method config.
 *
 * @author xianyijun
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AbstractMethodConfig extends AbstractConfig {

    /**
     * The Timeout.
     */
    protected Integer timeout;

    /**
     * The Retries.
     */
    protected Integer retries;

    /**
     * The Actives.
     */
    protected Integer actives;

    /**
     * The Load balance.
     */
    protected String loadBalance;

    /**
     * The Async.
     */
    protected boolean async;

    /**
     * The Merger.
     */
    protected String merger;

    /**
     * The Cache.
     */
    protected String cache;

    /**
     * The Validation.
     */
    protected String validation;

    /**
     * The Parameters.
     */
    protected Map<String, String> parameters;
}
