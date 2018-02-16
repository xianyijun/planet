package cn.xianyijun.planet.config.spring.schema;

import cn.xianyijun.planet.config.api.ApplicationConfig;
import cn.xianyijun.planet.config.api.ConsumerConfig;
import cn.xianyijun.planet.config.api.ProtocolConfig;
import cn.xianyijun.planet.config.api.ProviderConfig;
import cn.xianyijun.planet.config.api.RegistryConfig;
import cn.xianyijun.planet.config.spring.ClientBean;
import cn.xianyijun.planet.config.spring.ServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author xianyijun
 */
@Slf4j
public class RpcNamespaceHandler extends NamespaceHandlerSupport{
    @Override
    public void init() {
        log.info("[RpcNamespaceHandler] start init ");
        registerBeanDefinitionParser("application", new RpcBeanDefinitionParser(ApplicationConfig.class, true));
        registerBeanDefinitionParser("registry", new RpcBeanDefinitionParser(RegistryConfig.class, true));
        registerBeanDefinitionParser("provider", new RpcBeanDefinitionParser(ProviderConfig.class, true));
        registerBeanDefinitionParser("consumer", new RpcBeanDefinitionParser(ConsumerConfig.class, true));
        registerBeanDefinitionParser("protocol", new RpcBeanDefinitionParser(ProtocolConfig.class, true));
        registerBeanDefinitionParser("service", new RpcBeanDefinitionParser(ServiceBean.class, true));
        registerBeanDefinitionParser("client", new RpcBeanDefinitionParser(ClientBean.class, false));
        registerBeanDefinitionParser("annotation", new AnnotationBeanDefinitionParser());
    }
}
