package cn.xianyijun.planet.config.api;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * The type Argument config.
 */
public class ArgumentConfig implements Serializable {

    @Setter
    private Integer index = -1;
    @Setter
    private String type;

    @Getter
    @Setter
    private Boolean callback;

    /**
     * Gets index.
     *
     * @return the index
     */
    public Integer getIndex() {
        return index;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }
}
