package cn.xianyijun.planet.rpc.api.proxy;

import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.RpcInvocation;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 * @author xianyijun
 * @date 2017/10/29
 */
@Slf4j
public class InvokerInvocationHandler implements InvocationHandler {
    private final Invoker<?> invoker;

    /**
     * Instantiates a new Invoker invocation handler.
     *
     * @param handler the handler
     */
    public InvokerInvocationHandler(Invoker<?> handler) {
        this.invoker = handler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("[InvokerInvocationHandler] invoke , proxy className :{} , method :{} ,args :{} , invoker :{} ",proxy.getClass().getName(),method.getName(), JSON.toJSONString(args),invoker);
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, args);
        }
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return invoker.toString();
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return invoker.hashCode();
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return invoker.equals(args[0]);
        }
        return invoker.invoke(new RpcInvocation(method, args)).recreate();
    }
}
