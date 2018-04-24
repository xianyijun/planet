package cn.xianyijun.planet.test.demo;

import com.alibaba.fastjson.JSON;

/**
 * Created by xianyijun on 2017/10/22.
 */
public class ProcessServiceImpl implements ProcessService {
    @Override
    public Object test() {
        return JSON.toJSONString("213");
    }
}
