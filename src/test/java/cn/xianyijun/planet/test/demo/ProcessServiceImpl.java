package cn.xianyijun.planet.test.demo;

/**
 * Created by xianyijun on 2017/10/22.
 */
public class ProcessServiceImpl implements ProcessService {
    @Override
    public Object test(Object object) {
        return "hello ," +object;
    }
}
