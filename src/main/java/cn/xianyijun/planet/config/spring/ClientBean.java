package cn.xianyijun.planet.config.spring;

import cn.xianyijun.planet.config.api.ClientConfig;
import cn.xianyijun.planet.config.spring.extension.SpringExtensionFactory;
import cn.xianyijun.planet.remoting.api.Client;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author xianyijun
 */
public class ClientBean<T> extends ClientConfig<T> implements FactoryBean, ApplicationContextAware, InitializingBean, DisposableBean{

    private transient ApplicationContext applicationContext;

    public ClientBean(Client client) {
        super(client);
    }


    @Override
    public Object getObject() throws Exception {
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        SpringExtensionFactory.addApplicationContext(applicationContext);
    }

    @Override
    public void destroy() throws Exception {

    }

}
