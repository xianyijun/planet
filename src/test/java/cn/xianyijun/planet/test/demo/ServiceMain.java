package cn.xianyijun.planet.test.demo;

import java.io.IOException;

import cn.xianyijun.planet.config.api.ApplicationConfig;
import cn.xianyijun.planet.config.api.ProtocolConfig;
import cn.xianyijun.planet.config.api.RegistryConfig;
import cn.xianyijun.planet.config.api.ServiceConfig;

/**
 * The type Service main.
 */
public class ServiceMain {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        ProcessService processService = new ProcessServiceImpl();

        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("process");

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://127.0.0.1:2181");
        registryConfig.setUsername("aaa");
        registryConfig.setPassword("bbb");

        ProtocolConfig protocolConfig = new ProtocolConfig();
        protocolConfig.setName("http");
        protocolConfig.setPort(20880);
        protocolConfig.setThreads(5);

        ServiceConfig<ProcessService> service = new ServiceConfig<>();
        service.setApplication(applicationConfig);
        service.setRegistry(registryConfig);
        service.setProtocol(protocolConfig);
        service.setInterface(ProcessService.class);
        service.setRef(processService);
        service.setVersion("2.0.0");
        service.setTimeout(100000);
        service.export();

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
