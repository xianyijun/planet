package cn.xianyijun.planet.common.compiler;


import cn.xianyijun.planet.common.extension.Adaptive;
import cn.xianyijun.planet.common.extension.ExtensionLoader;

/**
 *
 * @author xianyijun
 * @date 2018/1/21
 */
@Adaptive
public class AdaptiveCompiler implements Compiler {
    private static volatile String DEFAULT_COMPILER;

    /**
     * Sets default compiler.
     *
     * @param compiler the compiler
     */
    public static void setDefaultCompiler(String compiler) {
        DEFAULT_COMPILER = compiler;
    }

    @Override
    public Class<?> compile(String code, ClassLoader classLoader) {
        Compiler compiler;
        ExtensionLoader<Compiler> loader = ExtensionLoader.getExtensionLoader(Compiler.class);
        String name = DEFAULT_COMPILER;
        if (name != null && name.length() > 0) {
            compiler = loader.getExtension(name);
        } else {
            compiler = loader.getDefaultExtension();
        }
        return compiler.compile(code, classLoader);
    }
}
