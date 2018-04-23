package cn.xianyijun.planet.test.demo;


import cn.xianyijun.planet.config.api.ApplicationConfig;
import cn.xianyijun.planet.config.api.ClientConfig;
import cn.xianyijun.planet.config.api.RegistryConfig;

/**
 * Created by xianyijun on 2017/10/22.
 */
public class ClientMain {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        ApplicationConfig application = new ApplicationConfig();
        application.setName("process");

        RegistryConfig registry = new RegistryConfig();
        registry.setAddress("zookeeper://127.0.0.1:2181");
        registry.setUsername("aaa");
        registry.setPassword("bbb");

        ClientConfig<ProcessService> reference = new ClientConfig<>();
        reference.setProtocol("http");
        reference.setApplication(application);
        reference.setRegistry(registry); // 多个注册中心可以用setRegistries()
        reference.setInterface(ProcessService.class);
        reference.setVersion("2.0.0");

        ProcessService demoService = reference.get();
        System.out.println(demoService.test("hello"));
    }
}
