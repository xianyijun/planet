package cn.xianyijun.planet.test.demo;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class NameSpaceHandlerTest {

    @Test
    public void testProvider() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] {"application-provider.xml"});
        context.start();
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConsumer() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] {"application-consumer.xml"});
        context.start();
        ProcessService processService = (ProcessService) context.getBean("processService");
        System.out.println(processService.test("test"));
    }
}

