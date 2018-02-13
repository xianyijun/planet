package cn.xianyijun.planet.rpc.rpc;

import cn.xianyijun.planet.common.Constants;
import cn.xianyijun.planet.common.URL;
import cn.xianyijun.planet.common.extension.ExtensionLoader;
import cn.xianyijun.planet.exception.RemotingException;
import cn.xianyijun.planet.exception.RpcException;
import cn.xianyijun.planet.remoting.api.Channel;
import cn.xianyijun.planet.remoting.api.Transporter;
import cn.xianyijun.planet.remoting.api.Transporters;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeChannel;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeClient;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeHandler;
import cn.xianyijun.planet.remoting.api.exchange.ExchangeServer;
import cn.xianyijun.planet.remoting.api.exchange.Exchangers;
import cn.xianyijun.planet.remoting.api.exchange.support.ExchangeHandlerAdapter;
import cn.xianyijun.planet.rpc.api.Exporter;
import cn.xianyijun.planet.rpc.api.Invocation;
import cn.xianyijun.planet.rpc.api.Invoker;
import cn.xianyijun.planet.rpc.api.Protocol;
import cn.xianyijun.planet.rpc.api.RpcContext;
import cn.xianyijun.planet.rpc.api.RpcInvocation;
import cn.xianyijun.planet.rpc.api.protocol.AbstractProtocol;
import cn.xianyijun.planet.utils.NetUtils;
import cn.xianyijun.planet.utils.StringUtils;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The type Rpc protocol.
 * @author xianyijun
 */
public class RpcProtocol extends AbstractProtocol {
    /**
     * The constant NAME.
     */
    public static final String NAME = "rpc";

    private static final String IS_CALLBACK_SERVICE_INVOKE = "_isCallBackServiceInvoke";

    private static RpcProtocol INSTANCE;

    private final ConcurrentMap<String, String> stubServiceMethodsMap = new ConcurrentHashMap<>();

    private final Map<String, ExchangeServer> serverMap = new ConcurrentHashMap<>();

    private final Map<String, ReferenceCountExchangeClient> referenceClientMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, LazyConnectExchangeClient> ghostClientMap = new ConcurrentHashMap<>();

    public RpcProtocol() {
        INSTANCE = this;
    }

    private ExchangeHandler requestHandler = new ExchangeHandlerAdapter(){
        @Override
        public Object reply(ExchangeChannel channel, Object message) throws RemotingException {
            logger.info("[RpcProtocol] requestHandler.reply , channel: {} ,message: {}", channel , message);
            if (message instanceof Invocation) {
                Invocation inv = (Invocation) message;
                Invoker<?> invoker = getInvoker(channel, inv);

                if (Boolean.TRUE.toString().equals(inv.getAttachments().get(IS_CALLBACK_SERVICE_INVOKE))) {
                    String methodsStr = invoker.getUrl().getParameters().get("methods");
                    boolean hasMethod = false;
                    if (methodsStr == null || !methodsStr.contains(",")) {
                        hasMethod = inv.getMethodName().equals(methodsStr);
                    } else {
                        String[] methods = methodsStr.split(",");
                        for (String method : methods) {
                            if (inv.getMethodName().equals(method)) {
                                hasMethod = true;
                                break;
                            }
                        }
                    }
                    if (!hasMethod) {
                        logger.warn(new IllegalStateException("The methodName " + inv.getMethodName() + " not found in callback service interface ,invoke will be ignored. please update the api interface. url is:" + invoker.getUrl()) + " ,invocation is :" + inv);
                        return null;
                    }
                }
                RpcContext.getContext().setRemoteAddress(channel.getRemoteAddress());
                return invoker.invoke(inv);
            }
            throw new RemotingException(channel, message.getClass().getName() + ": " + message + ", channel: consumer: " + channel.getRemoteAddress() + " --> provider: " + channel.getLocalAddress());
        }

        @Override
        public void received(Channel channel, Object message) throws RemotingException {
            logger.info("[RpcProtocol] requestHandler.received , channel: {} ,message :{}", channel ,message);
            if (message instanceof Invocation) {
                reply((ExchangeChannel) channel, message);
            } else {
                super.received(channel, message);
            }
        }

        @Override
        public void connected(Channel channel) throws RemotingException {
            logger.info("[RpcProtocol] requestHandler.connect , channel:{}", channel);
            invoke(channel, Constants.ON_CONNECT_KEY);
        }

        @Override
        public void disConnected(Channel channel) throws RemotingException {
            logger.info("[RpcProtocol] requestHandler.disConnected , channel:{}", channel);
            if (logger.isInfoEnabled()) {
                logger.info("disConnected from " + channel.getRemoteAddress() + ",url:" + channel.getUrl());
            }
            invoke(channel, Constants.ON_DISCONNECT_KEY);
        }

        private void invoke(Channel channel, String methodKey) {
            logger.info("[RpcProtocol] requestHandler.invoke , channel :{} ,methodKey: {}", channel, methodKey);
            Invocation invocation = createInvocation(channel, channel.getUrl(), methodKey);
            if (invocation != null) {
                try {
                    received(channel, invocation);
                } catch (Throwable t) {
                    logger.warn("Failed to invoke event method " + invocation.getMethodName() + "(), cause: " + t.getMessage(), t);
                }
            }
        }

        private Invocation createInvocation(Channel channel, URL url, String methodKey) {
            logger.info("[RpcProtocol] createInvocation , channel :{} url :{} methodKey :{}", channel ,url.toFullString() ,methodKey);
            String method = url.getParameter(methodKey);
            if (method == null || method.length() == 0) {
                return null;
            }
            RpcInvocation invocation = new RpcInvocation(method, new Class<?>[0], new Object[0]);
            invocation.setAttachment(Constants.PATH_KEY, url.getPath());
            invocation.setAttachment(Constants.GROUP_KEY, url.getParameter(Constants.GROUP_KEY));
            invocation.setAttachment(Constants.INTERFACE_KEY, url.getParameter(Constants.INTERFACE_KEY));
            invocation.setAttachment(Constants.VERSION_KEY, url.getParameter(Constants.VERSION_KEY));
            if (url.getParameter(Constants.STUB_EVENT_KEY, false)) {
                invocation.setAttachment(Constants.STUB_EVENT_KEY, Boolean.TRUE.toString());
            }
            return invocation;
        }
    };


    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        logger.info("[export] rpc protocol start export");
        URL url = invoker.getUrl();
        String serviceKey = serviceKey(url);
        RpcExporter<T> exporter = new RpcExporter<>(invoker, serviceKey, exporterMap);
        exporterMap.put(serviceKey, exporter);

        Boolean isStubSupportEvent = url.getParameter(Constants.STUB_EVENT_KEY, Constants.DEFAULT_STUB_EVENT);
        Boolean isCallbackService = url.getParameter(Constants.IS_CALLBACK_SERVICE, false);

        if (isStubSupportEvent && !isCallbackService) {
            String stubServiceMethods = url.getParameter(Constants.STUB_EVENT_METHODS_KEY);
            logger.info(String.format("[export] stubServiceMethod, serviceKey : %s ,method : %s", serviceKey, stubServiceMethods));
            if (!StringUtils.isBlank(stubServiceMethods)){
                stubServiceMethodsMap.put(url.getServiceKey(), stubServiceMethods);
            }
        }
        openServer(url);

        return exporter;
    }

    private void openServer(URL url) {
        logger.info(String.format("[openServer] start open server,url : %s", url));
        String key = url.getAddress();

        boolean isServer = url.getParameter(Constants.IS_SERVER_KEY, true);

        if (isServer) {
            ExchangeServer server = serverMap.get(key);
            if (server == null) {
                serverMap.put(key, createServer(url));
            } else {
                server.reset(url);
            }
        }
    }

    @Override
    public <T> Invoker<T> refer(Class<T> serviceType, URL url) throws RpcException {
        RpcInvoker invoker = new RpcInvoker<>(serviceType, url, getClients(url), invokers);
        invokers.add(invoker);
        return invoker;
    }

    private ExchangeServer createServer(URL url) {
        logger.info(String.format("[createServer] start create server, url : %s", url));
        url = url.addParameterIfAbsent(Constants.CHANNEL_READONLY_EVENT_SENT_KEY, Boolean.TRUE.toString());
        url = url.addParameterIfAbsent(Constants.HEARTBEAT_KEY, String.valueOf(Constants.DEFAULT_HEARTBEAT));
        String str = url.getParameter(Constants.SERVER_KEY, Constants.DEFAULT_REMOTING_SERVER);

        Set<String> supportedTypes = Transporters.getSupportedTypes();
        logger.info(String.format("[createServer] supportedType : %s", supportedTypes));

        if (str != null && !supportedTypes.contains(str)){
            throw new RpcException("Unsupported server type: " + str + ", url: " + url);
        }

        url = url.addParameter(Constants.CODEC_KEY, RpcCodec.NAME);
        ExchangeServer server;
        try{
            server = Exchangers.bind(url, requestHandler);
        }catch (RemotingException e){
            logger.error(String.format("[createServer] failure to create server, url : %s", url));
            throw new RpcException("Fail to start server(url: " + url + ") " + e.getMessage(), e);
        }
        logger.info(String.format("[createServer] server : %s", server));
        str = url.getParameter(Constants.CLIENT_KEY);
        if (str != null && str.length() > 0) {
            if (!supportedTypes.contains(str)) {
                throw new RpcException("Unsupported client type: " + str);
            }
        }
        return server;
    }

    private ExchangeClient[] getClients(URL url) {
        boolean serviceShareConnect = false;
        int connections = url.getParameter(Constants.CONNECTIONS_KEY, 0);
        if (connections == 0) {
            serviceShareConnect = true;
            connections = 1;
        }

        ExchangeClient[] clients = new ExchangeClient[connections];
        for (int i = 0; i < clients.length; i++) {
            if (serviceShareConnect) {
                clients[i] = getSharedClient(url);
            } else {
                clients[i] = initClient(url);
            }
        }
        return clients;
    }

    private ExchangeClient getSharedClient(URL url) {
        String key = url.getAddress();
        ReferenceCountExchangeClient client = referenceClientMap.get(key);
        if (client != null) {
            if (!client.isClosed()) {
                client.incrementAndGetCount();
                return client;
            } else {
                referenceClientMap.remove(key);
            }
        }
        synchronized (key.intern()) {
            ExchangeClient exchangeClient = initClient(url);
            client = new ReferenceCountExchangeClient(exchangeClient, ghostClientMap);
            referenceClientMap.put(key, client);
            ghostClientMap.remove(key);
            return client;
        }
    }

    /**
     * 创建新连接.
     */
    private ExchangeClient initClient(URL url) {
        String str = url.getParameter(Constants.CLIENT_KEY, url.getParameter(Constants.SERVER_KEY, Constants.DEFAULT_REMOTING_CLIENT));

        url = url.addParameter(Constants.CODEC_KEY, RpcCodec.NAME);
        url = url.addParameterIfAbsent(Constants.HEARTBEAT_KEY, String.valueOf(Constants.DEFAULT_HEARTBEAT));

        if (str != null && str.length() > 0 && !ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(str)) {
            throw new RpcException("Unsupported client type: " + str + "," +
                    " supported client type is " + StringUtils.join(ExtensionLoader.getExtensionLoader(Transporter.class).getSupportedExtensions(), " "));
        }

        ExchangeClient client;
        try {
            // connection should be lazy
            if (url.getParameter(Constants.LAZY_CONNECT_KEY, false)) {
                client = new LazyConnectExchangeClient(url, requestHandler);
            } else {
                client = Exchangers.connect(url, requestHandler);
            }
        } catch (RemotingException e) {
            throw new RpcException("Fail to create remoting client for service(" + url + "): " + e.getMessage(), e);
        }
        return client;
    }

    /**
     * Gets invoker.
     *
     * @param channel the channel
     * @param inv     the inv
     * @return the invoker
     * @throws RemotingException the remoting exception
     */
    public Invoker<?> getInvoker(Channel channel, Invocation inv) throws RemotingException {
        int port = channel.getLocalAddress().getPort();
        String path = inv.getAttachments().get(Constants.PATH_KEY);
        boolean isStubServiceInvoke = Boolean.TRUE.toString().equals(inv.getAttachments().get(Constants.STUB_EVENT_KEY));
        if (isStubServiceInvoke) {
            port = channel.getRemoteAddress().getPort();
        }
        boolean isCallBackServiceInvoke = isClientSide(channel) && !isStubServiceInvoke;
        if (isCallBackServiceInvoke) {
            path = inv.getAttachments().get(Constants.PATH_KEY) + "." + inv.getAttachments().get(Constants.CALLBACK_SERVICE_KEY);
            inv.getAttachments().put(IS_CALLBACK_SERVICE_INVOKE, Boolean.TRUE.toString());
        }
        String serviceKey = serviceKey(port, path, inv.getAttachments().get(Constants.VERSION_KEY), inv.getAttachments().get(Constants.GROUP_KEY));

        RpcExporter<?> exporter = (RpcExporter<?>) exporterMap.get(serviceKey);

        if (exporter == null) {
            throw new RemotingException(channel, "Not found exported service: " + serviceKey + " in " + exporterMap.keySet() + ", may be version or group mismatch " + ", channel: consumer: " + channel.getRemoteAddress() + " --> provider: " + channel.getLocalAddress() + ", message:" + inv);
        }

        return exporter.getInvoker();
    }

    private boolean isClientSide(Channel channel) {
        InetSocketAddress address = channel.getRemoteAddress();
        URL url = channel.getUrl();
        return url.getPort() == address.getPort() &&
                NetUtils.filterLocalHost(channel.getUrl().getIp())
                        .equals(NetUtils.filterLocalHost(address.getAddress().getHostAddress()));
    }

    public static RpcProtocol getRpcProtocol() {
        if (INSTANCE == null) {
            ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(RpcProtocol.NAME);
        }
        return INSTANCE;
    }
}
