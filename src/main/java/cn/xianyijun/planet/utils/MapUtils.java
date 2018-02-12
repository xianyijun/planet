package cn.xianyijun.planet.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by xianyijun on 2018/1/28.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapUtils {

    /**
     * Is empty boolean.
     *
     * @param map the map
     * @return the boolean
     */
    public static boolean isEmpty(Map map){
        return map == null || map.isEmpty();
    }
}
