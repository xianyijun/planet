package cn.xianyijun.plant.config.spring.annoation.provider;

import cn.xianyijun.plant.config.spring.api.DemoService;

public class DemoServiceImpl implements DemoService {
    @Override
    public String sayName(String name) {
        return "Hello," + name;
    }
}
