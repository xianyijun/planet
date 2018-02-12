package cn.xianyijun.planet.container.spring;

import cn.xianyijun.planet.container.api.Container;
import cn.xianyijun.planet.utils.ConfigUtils;
import cn.xianyijun.planet.utils.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author xianyijun
 */
@Slf4j
public class SpringContainer implements Container {
    private static final String SPRING_CONFIG = "rpc.spring.config";
    private static final String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/spring/*.xml";
    @Getter
    static ClassPathXmlApplicationContext context;

    @Override
    public void start() {
        log.info("[SpringContainer] start");
        String configPath = ConfigUtils.getProperty(SPRING_CONFIG);

        if (StringUtils.isBlank(configPath)){
            configPath = DEFAULT_SPRING_CONFIG;
        }
        context = new ClassPathXmlApplicationContext(configPath);
        context.start();
    }

    @Override
    public void stop() {
        try {
            if (context != null) {
                context.stop();
                context.close();
                context = null;
            }
        } catch (Throwable e) {
            log.error("[SpringContainer] stop failure , message: {} , ex: {}",e.getMessage() ,e);
        }
    }
}
