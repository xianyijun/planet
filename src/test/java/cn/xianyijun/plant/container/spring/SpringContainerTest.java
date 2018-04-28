package cn.xianyijun.plant.container.spring;

import org.junit.Assert;
import org.junit.Test;

import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.container.api.Container;
import cn.xianyijun.planet.container.spring.SpringContainer;

public class SpringContainerTest {

    @Test
    public void springContainerTest() {
        SpringContainer container = (SpringContainer) ExtensionLoader.getExtensionLoader(Container.class).getExtension("spring");
        container.start();
        Assert.assertEquals(SpringContainer.class, container.context.getBean("container").getClass());
        container.stop();
    }
}
