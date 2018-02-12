package cn.xianyijun.planet.registry.api.support;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.registry.api.Registry;
import cn.xianyijun.planet.registry.api.RegistryFactory;
import cn.xianyijun.planet.registry.api.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


/**
 * The type Abstract registry factory.
 */
@Slf4j
public abstract class AbstractRegistryFactory implements RegistryFactory {

    private static final ReentrantLock LOCK = new ReentrantLock();

    private static final Map<String, Registry> REGISTRIES = new ConcurrentHashMap<>();

    @Override
    public Registry getRegistry(URL url) {
        url = url.setPath(RegistryService.class.getName())
                .addParameter(Constants.INTERFACE_KEY, RegistryService.class.getName())
                .removeParameters(Constants.EXPORT_KEY, Constants.REFER_KEY);
        String key = url.toServiceString();
        // 锁定注册中心获取过程，保证注册中心单一实例
        LOCK.lock();
        try {
            Registry registry = REGISTRIES.get(key);
            if (registry != null) {
                return registry;
            }
            // todo fix oom
            registry = createRegistry(url);
            if (registry == null) {
                throw new IllegalStateException("Can not create registry " + url);
            }
            REGISTRIES.put(key, registry);
            return registry;
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Gets registries.
     *
     * @return the registries
     */
    public static Collection<Registry> getRegistries() {
        return Collections.unmodifiableCollection(REGISTRIES.values());
    }

    /**
     * Create registry registry.
     *
     * @param url the url
     * @return the registry
     */
    protected abstract Registry createRegistry(URL url);
}
