package cn.xianyijun.planet.config.spring;

import cn.xianyijun.planet.config.api.ApplicationConfig;
import cn.xianyijun.planet.config.api.ClientConfig;
import cn.xianyijun.planet.config.api.ConsumerConfig;
import cn.xianyijun.planet.config.api.RegistryConfig;
import cn.xianyijun.planet.config.spring.extension.SpringExtensionFactory;
import cn.xianyijun.planet.remoting.api.Client;
import cn.xianyijun.planet.utils.CollectionUtils;
import cn.xianyijun.planet.utils.MapUtils;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author xianyijun
 */
@NoArgsConstructor
public class ClientBean<T> extends ClientConfig<T> implements FactoryBean, ApplicationContextAware, InitializingBean, DisposableBean{

    private transient ApplicationContext applicationContext;

    public ClientBean(Client client) {
        super(client);
    }


    @Override
    public Object getObject() throws Exception {
        return get();
    }

    @Override
    public Class<?> getObjectType() {
        return getInterfaceClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (getConsumerConfig() == null){
            Map<String, ConsumerConfig> consumerConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ConsumerConfig.class, false, false);
            if (consumerConfigMap != null && consumerConfigMap.size() > 0) {
                ConsumerConfig consumerConfig = null;
                for (ConsumerConfig config : consumerConfigMap.values()) {
                    if (config.getIsDefault() == null || config.getIsDefault()) {
                        if (consumerConfig != null) {
                            throw new IllegalStateException("Duplicate consumer configs: " + consumerConfig + " and " + config);
                        }
                        consumerConfig = config;
                    }
                }
                if (consumerConfig != null) {
                    setConsumerConfig(consumerConfig);
                }
            }
        }

        if (getApplication() == null && (getConsumerConfig() == null || getConsumerConfig().getApplication() == null)) {
            Map<String, ApplicationConfig> applicationConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ApplicationConfig.class, false, false);
            if (!MapUtils.isEmpty(applicationConfigMap)) {
                List<ApplicationConfig> configs = applicationConfigMap.values().stream().filter(config -> config.getIsDefault() == null || config.getIsDefault()).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(configs) && configs.size() == 1){
                    setApplication(configs.get(0));
                }else if (!CollectionUtils.isEmpty(configs)) {
                    throw new IllegalStateException("Duplicate application configs: " + configs);
                }
            }
        }

        if ((getRegistries() == null || getRegistries().isEmpty())
                && (getConsumerConfig() == null || CollectionUtils.isEmpty(getConsumerConfig().getRegistries()))
                && (getApplication() == null || CollectionUtils.isEmpty(getApplication().getRegistries()))) {
            Map<String, RegistryConfig> registryConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RegistryConfig.class, false, false);
            if (!MapUtils.isEmpty(registryConfigMap)) {
                List<RegistryConfig> registryConfigs = registryConfigMap.values().stream().filter(Objects::nonNull).filter(config -> config.getIsDefault() == null || config.getIsDefault()).collect(Collectors.toList());;
                super.setRegistries(registryConfigs);
            }
        }

        Boolean inited = isInit();
        if (inited) {
            getObject();
        }
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
