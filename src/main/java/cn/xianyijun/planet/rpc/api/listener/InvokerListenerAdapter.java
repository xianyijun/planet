package cn.xianyijun.planet.rpc.api.listener;

import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.InvokerListener;

/**
 * Created by xianyijun on 2017/11/5.
 */
public abstract class InvokerListenerAdapter implements InvokerListener {
    @Override
    public void referred(Invoker<?> invoker) throws RpcException {

    }

    @Override
    public void destroyed(Invoker<?> invoker) {

    }
}
