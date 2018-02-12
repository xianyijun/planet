package cn.xianyijun.planet.common.compiler;

import cn.xianyijun.planet.common.extension.SPI;

/**
 * The interface Compiler.
 */
@SPI("javassist")
public interface Compiler {

    /**
     * Compile class.
     *
     * @param code        the code
     * @param classLoader the class loader
     * @return the class
     */
    Class<?> compile(String code, ClassLoader classLoader);

}
