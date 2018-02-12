package cn.xianyijun.planet.config.api;

import cn.xianyijun.planet.common.compiler.AdaptiveCompiler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * The type Application config.
 * @author xianyijun
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class ApplicationConfig extends AbstractConfig {

    private String name;

    private String version;

    private String environment;

    private List<RegistryConfig> registries;
    
    private String compiler;

    private Boolean isDefault;

    /**
     * Sets compiler.
     *
     * @param compiler the compiler
     */
    public void setCompiler(String compiler) {
        this.compiler = compiler;
        AdaptiveCompiler.setDefaultCompiler(compiler);
    }
}
