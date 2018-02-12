package cn.xianyijun.planet.container;


import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.container.api.Container;
import cn.xianyijun.planet.utils.ArrayUtils;
import cn.xianyijun.planet.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xianyijun
 */
@Slf4j
public class Main {

    private static final String CONTAINER_KEY = "rpc.container";

    private static final String SHUTDOWN_HOOK_KEY = "rpc.shutdown.hook";

    private static final ExtensionLoader<Container> LOADER = ExtensionLoader.getExtensionLoader(Container.class);

    private static final ReentrantLock LOCK = new ReentrantLock();

    private static final Condition STOP = LOCK.newCondition();


    public static void main(String[] args) {
        try {
            if (ArrayUtils.isEmpty(args)){
                String config = ConfigUtils.getProperty(CONTAINER_KEY, LOADER.getDefaultExtensionName());
                args = Constants.COMMA_SPLIT_PATTERN.split(config);
            }
            final List<Container> containers = new ArrayList<>();
            for (String arg : args) {
                containers.add(LOADER.getExtension(arg));
            }
            log.info("[main] use container type : ( {} ) to run rpcService", Arrays.toString(args));

            if (Boolean.TRUE.toString().equalsIgnoreCase(SHUTDOWN_HOOK_KEY)){
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    for (Container container : containers){
                        try{
                            container.stop();
                            log.info("[shutdownHook] rpc container interfaceClass: {}  stopped",container.getClass().getSimpleName());
                        } catch (Throwable t){
                            log.error(t.getMessage(),t);
                        }
                        try {
                            LOCK.lock();
                            STOP.signal();
                        } finally {
                            LOCK.unlock();
                        }
                    }
                }));
            }
            for (Container container : containers) {
                container.start();
                log.info("[Main] rpc : {} started!", container.getClass().getSimpleName());
            }
        }catch (RuntimeException e){
            log.error("[Main] container start failure , ex: {}",e);
            System.exit(1);
        }
        try {
            LOCK.lock();
            STOP.await();
        } catch (InterruptedException e) {
            log.warn("[Main]rpc service server stopped, interrupted by other thread!", e);
        } finally {
            LOCK.unlock();
        }
    }
}
