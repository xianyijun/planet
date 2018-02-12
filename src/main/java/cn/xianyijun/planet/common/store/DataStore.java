package cn.xianyijun.planet.common.store;

import cn.xianyijun.planet.common.extension.SPI;

import java.util.Map;

/**
 * The interface Data store.
 */
@SPI("simple")
public interface DataStore {
    /**
     * Get map.
     *
     * @param componentName the component name
     * @return the map
     */
    Map<String, Object> get(String componentName);

    /**
     * Get object.
     *
     * @param componentName the component name
     * @param key           the key
     * @return the object
     */
    Object get(String componentName, String key);

    /**
     * Put.
     *
     * @param componentName the component name
     * @param key           the key
     * @param value         the value
     */
    void put(String componentName, String key, Object value);

    /**
     * Remove.
     *
     * @param componentName the component name
     * @param key           the key
     */
    void remove(String componentName, String key);
}
