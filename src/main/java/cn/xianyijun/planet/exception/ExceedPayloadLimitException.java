package cn.xianyijun.planet.exception;

import java.io.IOException;

/**
 * The type Exceed payload limit exception.
 *
 * @author xianyijun
 */
public class ExceedPayloadLimitException extends IOException {

    /**
     * Instantiates a new Exceed payload limit exception.
     *
     * @param message the message
     */
    public ExceedPayloadLimitException(String message) {
        super(message);
    }
}
