package cn.xianyijun.planet.rpc.api.filter;


import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.extension.Activate;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Filter;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Result;
import cn.xianyijun.planet.rpc.api.RpcContext;
import cn.xianyijun.planet.rpc.api.RpcInvocation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xianyijun
 */
@Activate(group = Constants.PROVIDER, order = -1000)
public class ContextFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Map<String, String> attachments = invocation.getAttachments();
        if (attachments != null){
            attachments = new HashMap<>(attachments);
            attachments.remove(Constants.PATH_KEY);
            attachments.remove(Constants.GROUP_KEY);
            attachments.remove(Constants.VERSION_KEY);
            attachments.remove(Constants.RPC_VERSION_KEY);
            attachments.remove(Constants.TOKEN_KEY);
            attachments.remove(Constants.TIMEOUT_KEY);
            attachments.remove(Constants.ASYNC_KEY);
        }
        RpcContext.getContext()
                .setUrl(invoker.getUrl())
                .setInvocation(invocation)
                .setLocalAddress(invoker.getUrl().getHost(),
                        invoker.getUrl().getPort());

        if (attachments != null) {
            if (RpcContext.getContext().getAttachments() != null) {
                RpcContext.getContext().getAttachments().putAll(attachments);
            } else {
                RpcContext.getContext().setAttachments(attachments);
            }
        }
        if (invocation instanceof RpcInvocation) {
            ((RpcInvocation) invocation).setInvoker(invoker);
        }
        try {
            return invoker.invoke(invocation);
        } finally {
            RpcContext.removeContext();
        }
    }
}
