package cn.xianyijun.planet.config.spring.schema;

import cn.xianyijun.planet.config.spring.beans.factory.annotation.ClientAnnotationBeanPostProcessor;
import cn.xianyijun.planet.config.spring.beans.factory.annotation.ServiceAnnotationBeanPostProcessor;
import cn.xianyijun.planet.config.spring.util.BeanRegistrar;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import static org.springframework.util.StringUtils.commaDelimitedListToStringArray;
import static org.springframework.util.StringUtils.trimArrayElements;

/**
 * @author xianyijun
 */
public class AnnotationBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        String packageToScan = element.getAttribute("package");

        String[] packagesToScan = trimArrayElements(commaDelimitedListToStringArray(packageToScan));

        builder.addConstructorArgValue(packagesToScan);

        builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        registerReferenceAnnotationBeanPostProcessor(parserContext.getRegistry());

    }

    @Override
    protected boolean shouldGenerateIdAsFallback() {
        return true;
    }

    private void registerReferenceAnnotationBeanPostProcessor(BeanDefinitionRegistry registry) {

        BeanRegistrar.registerInfrastructureBean(registry,
                ClientAnnotationBeanPostProcessor.BEAN_NAME, ClientAnnotationBeanPostProcessor.class);

    }

    @Override
    protected Class<?> getBeanClass(Element element) {
        return ServiceAnnotationBeanPostProcessor.class;
    }

}

