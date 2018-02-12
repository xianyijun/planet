package cn.xianyijun.planet.config.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The type Consumer config.
 *
 * @author xianyijun
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ConsumerConfig extends AbstractClientConfig {
    private Boolean isDefault;
}
