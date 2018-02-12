package cn.xianyijun.planet.utils;

import java.util.Collection;

/**
 * Created by xianyijun on 2018/1/21.
 */
public class CollectionUtils {

    /**
     * Is empty boolean.
     *
     * @param collection the collection
     * @return the boolean
     */
    public static boolean isEmpty(Collection<?> collection){
        return collection == null || collection.isEmpty();
    }
}
