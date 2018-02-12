package cn.xianyijun.planet.rpc.api.protocol;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.ExporterListener;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.InvokerListener;
import cn.xianyijun.planet.rpc.api.Protocol;
import cn.xianyijun.planet.rpc.api.listener.DefaultExporterListenerAdapter;
import cn.xianyijun.planet.rpc.api.listener.DepercatedInvokerListener;
import cn.xianyijun.planet.rpc.api.listener.ListenerExporterWrapper;
import cn.xianyijun.planet.rpc.api.listener.ListenerInvokerWrapper;

import java.util.Arrays;
import java.util.Collections;

/**
 * The type Protocol listener wrapper.
 *
 * @author xianyijun
 */
public class ProtocolListenerWrapper implements Protocol {

    private final Protocol protocol;

    private final InvokerListener invokerListener = new DepercatedInvokerListener();

    private final ExporterListener exporterListener = new DefaultExporterListenerAdapter();

    /**
     * Instantiates a new Protocol listener wrapper.
     *
     * @param protocol the protocol
     */
    public ProtocolListenerWrapper(Protocol protocol) {
        if (protocol == null) {
            throw new IllegalArgumentException("protocol can not be null");
        }
        this.protocol = protocol;
    }

    @Override
    public int getDefaultPort() {
        return protocol.getDefaultPort();
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        if (Constants.REGISTRY_PROTOCOL.equals(invoker.getUrl().getProtocol())) {
            return protocol.export(invoker);
        }
        return new ListenerExporterWrapper<>(protocol.export(invoker),
                Collections.unmodifiableList(Arrays.asList(exporterListener)));
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
            return protocol.refer(type, url);
        }
        return new ListenerInvokerWrapper<>(protocol.refer(type, url),
                Collections.unmodifiableList(Collections.singletonList(invokerListener)));
    }

    @Override
    public void destroy() {
        protocol.destroy();
    }

}
