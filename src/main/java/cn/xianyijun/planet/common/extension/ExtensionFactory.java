package cn.xianyijun.planet.common.extension;

/**
 * The interface Extension factory.
 * @author xianyijun
 */
@SPI
public interface ExtensionFactory {
    /**
     * Gets extension.
     *
     * @param <T>  the type parameter
     * @param type the type
     * @param name the name
     * @return the extension
     */
    <T> T getExtension(Class<T> type, String name);

}
