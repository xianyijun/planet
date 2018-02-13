package cn.xianyijun.planet.config.spring.schema;

import cn.xianyijun.planet.config.api.ApplicationConfig;
import cn.xianyijun.planet.config.api.ClientConfig;
import cn.xianyijun.planet.config.api.ConsumerConfig;
import cn.xianyijun.planet.config.api.ProtocolConfig;
import cn.xianyijun.planet.config.api.ProviderConfig;
import cn.xianyijun.planet.config.api.RegistryConfig;
import cn.xianyijun.planet.config.spring.ServiceBean;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author xianyijun
 */
public class RpcNamespaceHandler extends NamespaceHandlerSupport{
    @Override
    public void init() {
        registerBeanDefinitionParser("application", new RpcBeanDefinitionParser(ApplicationConfig.class, true));
        registerBeanDefinitionParser("registry", new RpcBeanDefinitionParser(RegistryConfig.class, true));
        registerBeanDefinitionParser("provider", new RpcBeanDefinitionParser(ProviderConfig.class, true));
        registerBeanDefinitionParser("consumer", new RpcBeanDefinitionParser(ConsumerConfig.class, true));
        registerBeanDefinitionParser("protocol", new RpcBeanDefinitionParser(ProtocolConfig.class, true));
        registerBeanDefinitionParser("service", new RpcBeanDefinitionParser(ServiceBean.class, true));
        registerBeanDefinitionParser("client", new RpcBeanDefinitionParser(ClientConfig.class, false));
        registerBeanDefinitionParser("annotation", new AnnotationBeanDefinitionParser());
    }
}
