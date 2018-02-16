package cn.xianyijun.planet.config.spring;

import cn.xianyijun.planet.config.annotation.RpcService;
import cn.xianyijun.planet.config.api.ApplicationConfig;
import cn.xianyijun.planet.config.api.ProtocolConfig;
import cn.xianyijun.planet.config.api.ProviderConfig;
import cn.xianyijun.planet.config.api.RegistryConfig;
import cn.xianyijun.planet.config.api.ServiceConfig;
import cn.xianyijun.planet.config.spring.extension.SpringExtensionFactory;
import cn.xianyijun.planet.utils.CollectionUtils;
import cn.xianyijun.planet.utils.MapUtils;
import cn.xianyijun.planet.utils.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xianyijun
 */
@Slf4j
public class ServiceBean<T> extends ServiceConfig<T> implements InitializingBean ,DisposableBean ,ApplicationContextAware ,ApplicationListener<ContextRefreshedEvent>, BeanNameAware{
    private static transient ApplicationContext SPRING_CONTEXT;

    private transient ApplicationContext applicationContext;

    @Getter
    private final transient RpcService service;

    private transient String beanName;

    private transient boolean supportedApplicationListener;


    public ServiceBean() {
        super();
        this.service = null;
    }

    public ServiceBean(RpcService service) {
        super(service);
        this.service = service;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() {
        log.info("[ServiceBean] afterPropertiesSet ");
        if (getProvider()  == null) {
            Map<String, ProviderConfig> providerConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ProviderConfig.class, false, false);
            if (!MapUtils.isEmpty(providerConfigMap)){
                ProviderConfig providerConfig = null;
                for (ProviderConfig config : providerConfigMap.values()) {
                    if (config.getIsDefault() == null || config.getIsDefault()) {
                        if (providerConfig != null) {
                            throw new IllegalStateException("Duplicate provider configs: " + providerConfig + " and " + config);
                        }
                        providerConfig = config;
                    }
                }
                if (providerConfig != null) {
                    setProvider(providerConfig);
                }
            }
        }

        if (getApplication() == null && (getProvider() == null || getProvider().getApplication() == null)) {
            Map<String, ApplicationConfig> applicationConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ApplicationConfig.class, false, false);
            if (!MapUtils.isEmpty(applicationConfigMap)) {
                ApplicationConfig applicationConfig = null;
                for (ApplicationConfig config : applicationConfigMap.values()) {
                    if (config.getIsDefault() == null || config.getIsDefault()) {
                        if (applicationConfig != null) {
                            throw new IllegalStateException("Duplicate application configs: " + applicationConfig + " and " + config);
                        }
                        applicationConfig = config;
                    }
                }
                if (applicationConfig != null) {
                    setApplication(applicationConfig);
                }
            }
        }

        if (CollectionUtils.isEmpty(getRegistries())
                && (getProvider() == null || CollectionUtils.isEmpty(getProvider().getRegistries()))
                && (getApplication() == null || CollectionUtils.isEmpty(getApplication().getRegistries()))) {
            Map<String, RegistryConfig> registryConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RegistryConfig.class, false, false);
            if (registryConfigMap != null && registryConfigMap.size() > 0) {
                List<RegistryConfig> registryConfigs = new ArrayList<RegistryConfig>();
                for (RegistryConfig config : registryConfigMap.values()) {
                    if (config.getIsDefault() == null || config.getIsDefault()) {
                        registryConfigs.add(config);
                    }
                }
                if (!registryConfigs.isEmpty()) {
                    super.setRegistries(registryConfigs);
                }
            }
        }

        if (CollectionUtils.isEmpty(getProtocols())
                || (getProvider() == null || CollectionUtils.isEmpty(getProvider().getProtocols()))){
            Map<String, ProtocolConfig> protocolConfigMap = applicationContext == null ? null : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, ProtocolConfig.class, false, false);
            if (protocolConfigMap != null && protocolConfigMap.size() > 0) {
                List<ProtocolConfig> protocolConfigs = new ArrayList<>();
                for (ProtocolConfig config : protocolConfigMap.values()) {
                    if (config.getIsDefault() == null || config.getIsDefault()) {
                        protocolConfigs.add(config);
                    }
                }
                if (!protocolConfigs.isEmpty()) {
                    super.setProtocols(protocolConfigs);
                }
            }
        }

        if (StringUtils.isBlank(getPath())) {
            if (!StringUtils.isBlank(beanName) && !StringUtils.isBlank(getInterface())
                    && beanName.startsWith(getInterface())) {
                setPath(beanName);
            }
        }

        if (!isDelay()) {
            export();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        SpringExtensionFactory.addApplicationContext(applicationContext);
        if (applicationContext != null) {
            SPRING_CONTEXT = applicationContext;
            try {
                Method method = applicationContext.getClass().getMethod("addApplicationListener", ApplicationListener.class);
                method.invoke(applicationContext, this);
                supportedApplicationListener = true;
            } catch (Throwable t) {
                if (applicationContext instanceof AbstractApplicationContext) {
                    try {
                        Method method = AbstractApplicationContext.class.getDeclaredMethod("addListener", ApplicationListener.class); // backward compatibility to spring 2.0.1
                        if (!method.isAccessible()) {
                            method.setAccessible(true);
                        }
                        method.invoke(applicationContext, this);
                        supportedApplicationListener = true;
                    } catch (Throwable ignored) {
                    }
                }
            }
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (isDelay() && !isExported() && !isUnExported()) {
            export();
        }
    }

    @Override
    public void destroy() {

    }


    @Override
    protected Class getServiceClass(T ref) {
        if (AopUtils.isAopProxy(ref)) {
            return AopUtils.getTargetClass(ref);
        }
        return super.getServiceClass(ref);
    }

    public static ApplicationContext getSpringContext() {
        return SPRING_CONTEXT;
    }


    private boolean isDelay() {
        Integer delay = getDelay();
        ProviderConfig provider = getProvider();
        if (delay == null && provider != null) {
            delay = provider.getDelay();
        }
        return supportedApplicationListener && (delay == null || delay == -1);
    }
}
