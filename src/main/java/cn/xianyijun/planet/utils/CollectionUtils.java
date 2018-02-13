package cn.xianyijun.planet.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public static Map<String, String> toStringMap(String... pairs) {
        Map<String, String> parameters = new HashMap<>(32);
        if (pairs.length > 0) {
            if (pairs.length % 2 != 0) {
                throw new IllegalArgumentException("pairs must be even.");
            }
            for (int i = 0; i < pairs.length; i = i + 2) {
                parameters.put(pairs[i], pairs[i + 1]);
            }
        }
        return parameters;
    }

}
