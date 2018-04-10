package cn.xianyijun.planet.config.spring.extension;

import org.springframework.context.ApplicationContext;

import java.util.Set;

import cn.xianyijun.planet.common.extension.ExtensionFactory;
import cn.xianyijun.planet.utils.ConcurrentHashSet;

/**
 * @author xianyijun
 */
public class SpringExtensionFactory implements ExtensionFactory {
    private static final Set<ApplicationContext> CONTEXTS = new ConcurrentHashSet<ApplicationContext>();

    public static void addApplicationContext(ApplicationContext context) {
        CONTEXTS.add(context);
    }

    public static void removeApplicationContext(ApplicationContext context) {
        CONTEXTS.remove(context);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExtension(Class<T> type, String name) {
        for (ApplicationContext context : CONTEXTS) {
            if (context.containsBean(name)) {
                Object bean = context.getBean(name);
                if (type.isInstance(bean)) {
                    return (T) bean;
                }
            }
        }
        return null;
    }
}
