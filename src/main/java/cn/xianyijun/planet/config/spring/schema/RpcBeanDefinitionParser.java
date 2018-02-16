package cn.xianyijun.planet.config.spring.schema;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.config.api.ArgumentConfig;
import cn.xianyijun.planet.config.api.ConsumerConfig;
import cn.xianyijun.planet.config.api.MethodConfig;
import cn.xianyijun.planet.config.api.ProtocolConfig;
import cn.xianyijun.planet.config.api.ProviderConfig;
import cn.xianyijun.planet.config.api.RegistryConfig;
import cn.xianyijun.planet.config.spring.ClientBean;
import cn.xianyijun.planet.config.spring.ServiceBean;
import cn.xianyijun.planet.rpc.api.Protocol;
import cn.xianyijun.planet.utils.ReflectUtils;
import cn.xianyijun.planet.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import static cn.xianyijun.planet.utils.ReflectUtils.isPrimitive;

/**
 * @author xianyijun
 */
@Slf4j
@RequiredArgsConstructor
public class RpcBeanDefinitionParser implements BeanDefinitionParser{

    private final Class<?> beanClass;
    private final boolean required;

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        log.info("[parse] start parse element, element: {} , beanClass:{} required :{}", element, beanClass ,required);
        return parse(element, parserContext, beanClass, required);
    }

    private static BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass, boolean required) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);

        String id = element.getAttribute("id");

        if (StringUtils.isBlank(id) && required){
            String generatedBeanName = element.getAttribute("name");
            if (StringUtils.isBlank(generatedBeanName)){
                if (ProtocolConfig.class.equals(beanClass)){
                    generatedBeanName = "rpc";
                }else {
                    generatedBeanName = element.getAttribute("interface");
                }
            }
            if (StringUtils.isBlank(generatedBeanName)){
                generatedBeanName = beanClass.getName();
            }
            id = generatedBeanName;
            int counter = 2;
            while (parserContext.getRegistry().containsBeanDefinition(id)){
                id = generatedBeanName + (counter ++);
            }
        }
        if (!StringUtils.isBlank(id)){
            if(parserContext.getRegistry().containsBeanDefinition(id)){
              throw new IllegalStateException("Duplicate Spring Bean id : " + id);
            }
            parserContext.getRegistry().registerBeanDefinition(id,beanDefinition);
            beanDefinition.getPropertyValues().addPropertyValue("id",id);
        }
        if (ProtocolConfig.class.equals(beanClass)){
            for (String name : parserContext.getRegistry().getBeanDefinitionNames()){
                BeanDefinition definition = parserContext.getRegistry().getBeanDefinition(name);
                PropertyValue propertyValue = definition.getPropertyValues().getPropertyValue(Constants.PROTOCOL_KEY);
                if (propertyValue != null){
                    Object value = propertyValue.getValue();
                    if (value instanceof ProtocolConfig && id.equals(((ProtocolConfig)value).getName())){
                        definition.getPropertyValues().addPropertyValue(Constants.PROTOCOL_KEY, new RuntimeBeanReference(id));
                    }
                }
            }
        } else if (ServiceBean.class.equals(beanClass)){
            String className = element.getAttribute("class");
            if (!StringUtils.isBlank(className)){
                RootBeanDefinition classDefinition = new RootBeanDefinition();
                classDefinition.setBeanClass(ReflectUtils.forName(className));
                classDefinition.setLazyInit(false);
                parseProperties(element.getChildNodes(), classDefinition);
                beanDefinition.getPropertyValues().addPropertyValue("ref", new BeanDefinitionHolder(classDefinition,id + "Impl"));
            }
        } else if (ProviderConfig.class.equals(beanClass)) {
            parseNested(element, parserContext, ServiceBean.class, true, "service", "provider", id, beanDefinition);
        } else if (ConsumerConfig.class.equals(beanClass)) {
            parseNested(element, parserContext, ClientBean.class, false, "reference", "consumer", id, beanDefinition);
        }

        Set<String> props = new HashSet<>();
        ManagedMap<String, TypedStringValue> parameters = null;
        for (Method setter : beanClass.getMethods()){
            String name = setter.getName();
            log.info("[RpcBeanDefinitionParser] parse, beanClass:{}  , method: {}",beanClass, name);
            if (name.length() > 3 && name.startsWith("set")
                    && Modifier.isPublic(setter.getModifiers())
                    && setter.getParameterTypes().length == 1) {
                Class<?> type = setter.getParameterTypes()[0];
                String property = StringUtils.camelToSplitName(name.substring(3, 4).toLowerCase() + name.substring(4), "-");
                props.add(property);
                Method getter = null;
                try {
                    getter = beanClass.getMethod("get" + name.substring(3));
                } catch (NoSuchMethodException e) {
                    try {
                        getter = beanClass.getMethod("is" + name.substring(3));
                    } catch (NoSuchMethodException ignored) {
                    }
                }
                if (getter == null
                        || !Modifier.isPublic(getter.getModifiers())
                        || !type.equals(getter.getReturnType())) {
                    continue;
                }
                if ("parameters".equals(property)) {
                    parameters = parseParameters(element.getChildNodes(), beanDefinition);
                } else if ("methods".equals(property)) {
                    parseMethods(id, element.getChildNodes(), beanDefinition, parserContext);
                } else if ("arguments".equals(property)) {
                    parseArguments(id, element.getChildNodes(), beanDefinition, parserContext);
                } else {
                    String value = element.getAttribute(property);
                    if (value != null) {
                        value = value.trim();
                        if (value.length() > 0) {
                            if ("registry".equals(property) && RegistryConfig.NO_AVAILABLE.equalsIgnoreCase(value)) {
                                RegistryConfig registryConfig = new RegistryConfig();
                                registryConfig.setAddress(RegistryConfig.NO_AVAILABLE);
                                beanDefinition.getPropertyValues().addPropertyValue(property, registryConfig);
                            } else if ("registry".equals(property) && value.indexOf(',') != -1) {
                                parseMultiRef("registries", value, beanDefinition, parserContext);
                            } else if ("provider".equals(property) && value.indexOf(',') != -1) {
                                parseMultiRef("providers", value, beanDefinition, parserContext);
                            } else if ("protocol".equals(property) && value.indexOf(',') != -1) {
                                parseMultiRef("protocols", value, beanDefinition, parserContext);
                            } else {
                                Object reference;
                                if (isPrimitive(type)) {
                                    reference = value;
                                } else if (Constants.PROTOCOL_KEY.equals(property)
                                        && ExtensionLoader.getExtensionLoader(Protocol.class).hasExtension(value)
                                        && (!parserContext.getRegistry().containsBeanDefinition(value)
                                        || !ProtocolConfig.class.getName().equals(parserContext.getRegistry().getBeanDefinition(value).getBeanClassName()))) {
                                    if ("rpc:provider".equals(element.getTagName())) {
                                        log.warn("Recommended replace <rpc:provider protocol=\"" + value + "\" ... /> to <rpc:protocol name=\"" + value + "\" ... />");
                                    }
                                    ProtocolConfig protocol = new ProtocolConfig();
                                    protocol.setName(value);
                                    reference = protocol;
                                } else if ("onReturn".equals(property)) {
                                    int index = value.lastIndexOf(".");
                                    String returnRef = value.substring(0, index);
                                    String returnMethod = value.substring(index + 1);
                                    reference = new RuntimeBeanReference(returnRef);
                                    beanDefinition.getPropertyValues().addPropertyValue("onreturnMethod", returnMethod);
                                } else if ("onThrow".equals(property)) {
                                    int index = value.lastIndexOf(".");
                                    String throwRef = value.substring(0, index);
                                    String throwMethod = value.substring(index + 1);
                                    reference = new RuntimeBeanReference(throwRef);
                                    beanDefinition.getPropertyValues().addPropertyValue("onthrowMethod", throwMethod);
                                } else {
                                    if ("ref".equals(property) && parserContext.getRegistry().containsBeanDefinition(value)) {
                                        BeanDefinition refBean = parserContext.getRegistry().getBeanDefinition(value);
                                        if (!refBean.isSingleton()) {
                                            throw new IllegalStateException("The exported service ref " + value + " must be singleton! Please set the " + value + " bean scope to singleton, eg: <bean id=\"" + value + "\" scope=\"singleton\" ...>");
                                        }
                                    }
                                    reference = new RuntimeBeanReference(value);
                                }
                                beanDefinition.getPropertyValues().addPropertyValue(property, reference);
                            }
                        }
                    }
                }
            }
        }
        NamedNodeMap attributes = element.getAttributes();
        int len = attributes.getLength();
        for (int i = 0; i < len; i++) {
            Node node = attributes.item(i);
            String name = node.getLocalName();
            if (!props.contains(name)) {
                if (parameters == null) {
                    parameters = new ManagedMap<>();
                }
                String value = node.getNodeValue();
                parameters.put(name, new TypedStringValue(value, String.class));
            }
        }
        if (parameters != null) {
            beanDefinition.getPropertyValues().addPropertyValue("parameters", parameters);
        }
        return beanDefinition;
    }

    private static void parseProperties(NodeList nodeList, RootBeanDefinition beanDefinition) {
        if (nodeList == null || nodeList.getLength() <= 0){
            return;
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                if ("property".equals(node.getNodeName())
                        || "property".equals(node.getLocalName())) {
                    String name = ((Element) node).getAttribute("name");
                    if (name != null && name.length() > 0) {
                        String value = ((Element) node).getAttribute("value");
                        String ref = ((Element) node).getAttribute("ref");
                        if (!StringUtils.isBlank(value)) {
                            beanDefinition.getPropertyValues().addPropertyValue(name, value);
                        } else if (!StringUtils.isBlank(ref)) {
                            beanDefinition.getPropertyValues().addPropertyValue(name, new RuntimeBeanReference(ref));
                        } else {
                            throw new UnsupportedOperationException("Unsupported <property name=\"" + name + "\"> sub tag, Only supported <property name=\"" + name + "\" ref=\"...\" /> or <property name=\"" + name + "\" value=\"...\" />");
                        }
                    }
                }
            }
        }
    }

    private static void parseNested(Element element, ParserContext parserContext, Class<?> beanClass, boolean required, String tag, String property, String ref, BeanDefinition beanDefinition) {
        NodeList nodeList = element.getChildNodes();
        if (nodeList != null && nodeList.getLength() > 0) {
            boolean first = true;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    if (tag.equals(node.getNodeName())
                            || tag.equals(node.getLocalName())) {
                        if (first) {
                            first = false;
                            String isDefault = element.getAttribute("default");
                            if (isDefault == null || isDefault.length() == 0) {
                                beanDefinition.getPropertyValues().addPropertyValue("default", "false");
                            }
                        }
                        BeanDefinition subDefinition = parse((Element) node, parserContext, beanClass, required);
                        if (subDefinition != null && ref != null && ref.length() > 0) {
                            subDefinition.getPropertyValues().addPropertyValue(property, new RuntimeBeanReference(ref));
                        }
                    }
                }
            }
        }
    }

    private static ManagedMap<String, TypedStringValue> parseParameters(NodeList nodeList, RootBeanDefinition beanDefinition) {
        if (nodeList == null || nodeList.getLength() == 0){
            return null;
        }
        ManagedMap<String, TypedStringValue> parameters = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                if ("parameter".equals(node.getNodeName())
                        || "parameter".equals(node.getLocalName())) {
                    if (parameters == null) {
                        parameters = new ManagedMap<>();
                    }
                    String key = ((Element) node).getAttribute("key");
                    String value = ((Element) node).getAttribute("value");
                    boolean hide = "true".equals(((Element) node).getAttribute("hide"));
                    if (hide) {
                        key = Constants.HIDE_KEY_PREFIX + key;
                    }
                    parameters.put(key, new TypedStringValue(value, String.class));
                }
            }
        }
        return parameters;
    }

    private static void parseMethods(String id, NodeList nodeList, RootBeanDefinition beanDefinition,
                                     ParserContext parserContext) {
        if (nodeList != null && nodeList.getLength() > 0) {
            ManagedList<BeanDefinitionHolder> methods = null;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    if ("method".equals(node.getNodeName()) || "method".equals(node.getLocalName())) {
                        String methodName = element.getAttribute("name");
                        if (methodName == null || methodName.length() == 0) {
                            throw new IllegalStateException("<rpc:method> name attribute == null");
                        }
                        if (methods == null) {
                            methods = new ManagedList<>();
                        }
                        BeanDefinition methodBeanDefinition = parse(((Element) node),
                                parserContext, MethodConfig.class, false);
                        String name = id + "." + methodName;
                        BeanDefinitionHolder methodBeanDefinitionHolder = new BeanDefinitionHolder(
                                methodBeanDefinition, name);
                        methods.add(methodBeanDefinitionHolder);
                    }
                }
            }
            if (methods != null) {
                beanDefinition.getPropertyValues().addPropertyValue("methods", methods);
            }
        }
    }


    private static void parseMultiRef(String property, String value, RootBeanDefinition beanDefinition,
                                      ParserContext parserContext) {
        String[] values = value.split("\\s*[,]+\\s*");
        ManagedList<RuntimeBeanReference> list = null;
        for (String v : values) {
            if (v != null && v.length() > 0) {
                if (list == null) {
                    list = new ManagedList<>();
                }
                list.add(new RuntimeBeanReference(v));
            }
        }
        beanDefinition.getPropertyValues().addPropertyValue(property, list);
    }

    @SuppressWarnings("unchecked")
    private static void parseArguments(String id, NodeList nodeList, RootBeanDefinition beanDefinition,
                                       ParserContext parserContext) {
        if (nodeList != null && nodeList.getLength() > 0) {
            ManagedList<BeanDefinitionHolder> arguments = null;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    if ("argument".equals(node.getNodeName()) || "argument".equals(node.getLocalName())) {
                        String argumentIndex = element.getAttribute("index");
                        if (arguments == null) {
                            arguments = new ManagedList<>();
                        }
                        BeanDefinition argumentBeanDefinition = parse(((Element) node),
                                parserContext, ArgumentConfig.class, false);
                        String name = id + "." + argumentIndex;
                        BeanDefinitionHolder argumentBeanDefinitionHolder = new BeanDefinitionHolder(
                                argumentBeanDefinition, name);
                        arguments.add(argumentBeanDefinitionHolder);
                    }
                }
            }
            if (arguments != null) {
                beanDefinition.getPropertyValues().addPropertyValue("arguments", arguments);
            }
        }
    }
}
