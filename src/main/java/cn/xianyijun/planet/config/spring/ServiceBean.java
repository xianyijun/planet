package cn.xianyijun.planet.config.spring;

import cn.xianyijun.planet.config.api.ServiceConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author xianyijun
 */ //TODO
public class ServiceBean<T> extends ServiceConfig<T> implements InitializingBean ,DisposableBean ,ApplicationContextAware ,ApplicationListener<ContextRefreshedEvent>, BeanNameAware{
    @Override
    public void setBeanName(String s) {

    }

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

    }
}
