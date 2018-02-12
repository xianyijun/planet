package cn.xianyijun.planet.rpc.api.filter;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.rpc.api.Filter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The type Filters.
 * @author xianyijun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Filters {
    /**
     * Gets filters.
     *
     * @param url   the url
     * @param key   the key
     * @param group the group
     * @return the filters
     */
    public static List<Filter> getFilters(URL url, String key, String group) {
        return ExtensionLoader.getExtensionLoader(Filter.class).getActivateExtension(url, key, group);
    }
}
