package cn.xianyijun.planet.utils;

/**
 *
 * @author xianyijun
 * @date 2018/1/21
 *
 * @param <T> the type parameter
 */
public class Holder<T> {

    private volatile T value;

    /**
     * Set.
     *
     * @param value the value
     */
    public void set(T value) {
        this.value = value;
    }

    /**
     * Get t.
     *
     * @return the t
     */
    public T get() {
        return value;
    }
}
