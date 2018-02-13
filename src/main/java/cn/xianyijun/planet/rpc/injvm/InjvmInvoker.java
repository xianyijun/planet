package cn.xianyijun.planet.rpc.injvm;

import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Result;
import cn.xianyijun.planet.rpc.api.RpcContext;
import cn.xianyijun.planet.rpc.api.protocol.AbstractInvoker;
import cn.xianyijun.planet.utils.NetUtils;

import java.util.Map;

/**
 *
 * @author xianyijun
 * @date 2018/1/21
 *
 * @param <T> the type parameter
 */
public class InjvmInvoker<T> extends AbstractInvoker {

    private final String key;

    private final Map<String, Exporter<?>> exporterMap;

    /**
     * Instantiates a new Injvm invoker.
     *
     * @param type        the type
     * @param url         the url
     * @param key         the key
     * @param exporterMap the exporter map
     */
    public InjvmInvoker(Class<T> type, URL url, String key, Map<String, Exporter<?>> exporterMap) {
        super(type, url);
        this.key = key;
        this.exporterMap = exporterMap;
    }

    @Override
    public boolean isAvailable() {
        InjvmExporter<?> exporter = (InjvmExporter<?>) exporterMap.get(key);
        return exporter != null && super.isAvailable();
    }

    @Override
    public Result doInvoke(Invocation invocation) throws Throwable {
        Exporter<?> exporter = InJVMProtocol.getExporter(exporterMap, getUrl());
        if (exporter == null) {
            throw new RpcException("Service [" + key + "] not found.");
        }
        RpcContext.getContext().setRemoteAddress(NetUtils.LOCALHOST, 0);
        return exporter.getInvoker().invoke(invocation);
    }
}
