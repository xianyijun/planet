package cn.xianyijun.planet.rpc.api.listener;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Invoker;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Depercated invoker listener.
 *
 * @author xianyijun
 */
@Slf4j
public class DepercatedInvokerListener extends InvokerListenerAdapter{
    @Override
    public void referred(Invoker<?> invoker) throws RpcException {
        if (invoker.getUrl().getParameter(Constants.DEPRECATED_KEY, false)) {
            log.error("The service " + invoker.getInterface().getName() + " is DEPRECATED! Declare from " + invoker.getUrl());
        }
    }

}
