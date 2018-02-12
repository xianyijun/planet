package cn.xianyijun.planet.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * The type Array utils.
 * @author xianyijun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ArrayUtils {
    /**
     * Is empty boolean.
     *
     * @param <T>   the type parameter
     * @param array the array
     * @return the boolean
     */
    public static <T> boolean isEmpty(T[] array){
        return array == null || array.length == 0;
    }
}
