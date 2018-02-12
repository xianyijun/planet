package cn.xianyijun.planet.common;

import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

/**
 * The type Constants.
 */
public class Constants {
    /**
     * The constant REGISTRY_SPLIT_PATTERN.
     */
    public static final Pattern REGISTRY_SPLIT_PATTERN = Pattern
            .compile("\\s*[|;]+\\s*");

    /**
     * The constant PROVIDER.
     */
    public static final String PROVIDER = "provider";

    /**
     * The constant PATH_SEPARATOR.
     */
    public final static String PATH_SEPARATOR = "/";

    /**
     * The constant RPC_KEY.
     */
    public static final String RPC_KEY = "rpc";

    /**
     * The constant DISPATCHER_KEY.
     */
    public static final String DISPATCHER_KEY = "dispatcher";
    /**
     * The constant CONSUMER.
     */
    public static final String CONSUMER = "consumer";

    /**
     * The constant DYNAMIC_KEY.
     */
    public static final String DYNAMIC_KEY = "dynamic";

    /**
     * The constant ENABLED_KEY.
     */
    public static final String ENABLED_KEY = "enabled";

    /**
     * The constant CLUSTER_KEY.
     */
    public static final String CLUSTER_KEY = "cluster";


    public static final String INJVM_KEY = "injvm";
    /**
     * The constant RPC_PROPERTIES_KEY.
     */
    public static final String RPC_PROPERTIES_KEY = "rpc.properties.file";

    public static final String CALLBACK_SERVICE_PROXY_KEY = "callback.service.proxy";

    public static final String CHANNEL_CALLBACK_KEY = "channel.callback.invokers.key";

    /**
     * The constant IDLE_TIMEOUT_KEY.
     */
    public static final String IDLE_TIMEOUT_KEY = "idle.timeout";

    /**
     * The constant ACCEPTS_KEY.
     */
    public static final String ACCEPTS_KEY = "accepts";

    public static final String CHARSET_KEY = "charset";

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final String DECODE_IN_IO_THREAD_KEY = "decode.in.io";

    public static final boolean DEFAULT_DECODE_IN_IO_THREAD = true;

    /**
     * The constant ON_CONNECT_KEY.
     */
    public static final String ON_CONNECT_KEY = "onConnect";

    /**
     * The constant ON_DISCONNECT_KEY.
     */
    public static final String ON_DISCONNECT_KEY = "onDisconnect";

    /**
     * The constant ON_INVOKE_METHOD_KEY.
     */
    public static final String ON_INVOKE_METHOD_KEY = "onInvoke.method";

    /**
     * The constant ON_RETURN_METHOD_KEY.
     */
    public static final String ON_RETURN_METHOD_KEY = "onReturn.method";

    /**
     * The constant ON_THROW_METHOD_KEY.
     */
    public static final String ON_THROW_METHOD_KEY = "onThrow.method";

    /**
     * The constant ON_INVOKE_INSTANCE_KEY.
     */
    public static final String ON_INVOKE_INSTANCE_KEY = "onInvoke.instance";

    /**
     * The constant ON_RETURN_INSTANCE_KEY.
     */
    public static final String ON_RETURN_INSTANCE_KEY = "onReturn.instance";

    /**
     * The constant ON_THROW_INSTANCE_KEY.
     */
    public static final String ON_THROW_INSTANCE_KEY = "onThrow.instance";

    /**
     * The constant VERSION_KEY.
     */
    public static final String VERSION_KEY = "version";

    /**
     * The constant WEIGHT_KEY.
     */
    public static final String WEIGHT_KEY = "weight";

    /**
     * The constant DEFAULT_WEIGHT.
     */
    public static final int DEFAULT_WEIGHT = 100;

    /**
     * The constant GROUP_KEY.
     */
    public static final String GROUP_KEY = "group";

    /**
     * The constant CODEC_KEY.
     */
    public static final String CODEC_KEY = "codec";

    /**
     * The constant SIDE_KEY.
     */
    public static final String SIDE_KEY = "side";

    /**
     * The constant REMOTE_KEY.
     */
    public static final String REMOTE_KEY = "remote";

    /**
     * The constant CONSUMER_SIDE.
     */
    public static final String CONSUMER_SIDE = "consumer";

    /**
     * The constant DEFAULT_KEY_PREFIX.
     */
    public static final String DEFAULT_KEY_PREFIX = "default.";

    /**
     * The constant STUB_EVENT_KEY.
     */
    public static final String STUB_EVENT_KEY = "rpc.stub.event";

    /**
     * The constant DEFAULT_STUB_EVENT.
     */
    public static final boolean DEFAULT_STUB_EVENT = false;

    /**
     * The constant STUB_EVENT_METHODS_KEY.
     */
    public static final String STUB_EVENT_METHODS_KEY = "rpc.stub.event.methods";

    /**
     * The constant IS_CALLBACK_SERVICE.
     */
    public static final String IS_CALLBACK_SERVICE = "is_callback_service";

    /**
     * The constant IS_SERVER_KEY.
     */
    public static final String IS_SERVER_KEY = "isServer";

    /**
     * The constant INTERFACE_KEY.
     */
    public static final String INTERFACE_KEY = "interface";

    /**
     * The constant COMMA_SPLIT_PATTERN.
     */
    public static final Pattern COMMA_SPLIT_PATTERN = Pattern
            .compile("\\s*[,]+\\s*");


    /**
     * The constant CHANNEL_ATTRIBUTE_READONLY_KEY.
     */
    public static final String CHANNEL_ATTRIBUTE_READONLY_KEY = "channel.readonly";

    /**
     * The constant CHANNEL_READONLY_EVENT_SENT_KEY.
     */
    public static final String CHANNEL_READONLY_EVENT_SENT_KEY = "channel.readonly.sent";

    /**
     * The constant CHANNEL_SEND_READONLYEVENT_KEY.
     */
    public static final String CHANNEL_SEND_READONLYEVENT_KEY = "channel.readonly.send";

    /**
     * The constant HEARTBEAT_KEY.
     */
    public static final String HEARTBEAT_KEY = "heartbeat";

    /**
     * The constant HEARTBEAT_TIMEOUT_KEY.
     */
    public static final String HEARTBEAT_TIMEOUT_KEY = "heartbeat.timeout";

    /**
     * The constant WARM_UP_KEY.
     */
    public static final String WARM_UP_KEY = "warm_up";

    /**
     * The constant DEFAULT_HEARTBEAT.
     */
    public static final int DEFAULT_HEARTBEAT = 60 * 1000;

    /**
     * The constant CONSUMERS_CATEGORY.
     */
    public static final String CONSUMERS_CATEGORY = "consumers";

    /**
     * The constant SERVER_KEY.
     */
    public static final String SERVER_KEY = "server";

    /**
     * The constant DEFAULT_REMOTING_SERVER.
     */
    public static final String DEFAULT_REMOTING_SERVER = "netty";

    /**
     * The constant CLIENT_KEY.
     */
    public static final String CLIENT_KEY = "client";

    /**
     * The constant EXCHANGER_KEY.
     */
    public static final String EXCHANGER_KEY = "exchanger";

    /**
     * The constant DEFAULT_EXCHANGER.
     */
    public static final String DEFAULT_EXCHANGER = "header";

    /**
     * The constant TIMEOUT_KEY.
     */
    public static final String TIMEOUT_KEY = "timeout";

    /**
     * The constant DEFAULT_TIMEOUT.
     */
    public static final int DEFAULT_TIMEOUT = 10000;

    /**
     * The constant CONNECT_TIMEOUT_KEY.
     */
    public static final String CONNECT_TIMEOUT_KEY = "connect.timeout";

    /**
     * The constant DEFAULT_CONNECT_TIMEOUT.
     */
    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;

    /**
     * The constant SENT_KEY.
     */
    public static final String SENT_KEY = "sent";

    /**
     * The constant DEFAULT_PAYLOAD.
     */
    public static final int DEFAULT_PAYLOAD = 8 * 1024 * 1024;

    /**
     * The constant PAYLOAD_KEY.
     */
    public static final String PAYLOAD_KEY = "payload";

    /**
     * The constant COMMA_SEPARATOR.
     */
    public static final String COMMA_SEPARATOR = ",";

    /**
     * The constant EXECUTOR_SERVICE_COMPONENT_KEY.
     */
    public static final String EXECUTOR_SERVICE_COMPONENT_KEY = ExecutorService.class.getName();

    /**
     * The constant THREAD_NAME_KEY.
     */
    public static final String THREAD_NAME_KEY = "threadname";

    /**
     * The constant THREADS_KEY.
     */
    public static final String THREADS_KEY = "threads";

    /**
     * The constant RPC_VERSION_KEY.
     */
    public static final String RPC_VERSION_KEY = "rpc";

    /**
     * The constant CORE_THREADS_KEY.
     */
    public static final String CORE_THREADS_KEY = "corethreads";

    /**
     * The constant DEFAULT_THREAD_NAME.
     */
    public static final String DEFAULT_THREAD_NAME = "rpc";

    /**
     * The constant QUEUES_KEY.
     */
    public static final String QUEUES_KEY = "queues";

    /**
     * The constant DEFAULT_QUEUES.
     */
    public static final int DEFAULT_QUEUES = 0;

    /**
     * The constant DEFAULT_CORE_THREADS.
     */
    public static final int DEFAULT_CORE_THREADS = 0;

    /**
     * The constant DEFAULT_THREADS.
     */
    public static final int DEFAULT_THREADS = 200;

    /**
     * The constant DUMP_DIRECTORY.
     */
    public static final String DUMP_DIRECTORY = "dump.directory";

    /**
     * The constant IO_THREADS_KEY.
     */
    public static final String IO_THREADS_KEY = "iothreads";

    /**
     * The constant DEFAULT_IO_THREADS.
     */
    public static final int DEFAULT_IO_THREADS = Math.min(Runtime.getRuntime().availableProcessors() + 1, 32);

    /**
     * The constant THREAD_POOL_KEY.
     */
    public static final String THREAD_POOL_KEY = "threadpool";

    /**
     * The constant DEFAULT_CLIENT_THREAD_POOL.
     */
    public static final String DEFAULT_CLIENT_THREAD_POOL = "cached";

    /**
     * The constant SEND_RECONNECT_KEY.
     */
    public static final String SEND_RECONNECT_KEY = "send.reconnect";

    /**
     * The constant SHUTDOWN_TIMEOUT_KEY.
     */
    public static final String SHUTDOWN_TIMEOUT_KEY = "shutdown.timeout";

    /**
     * The constant SHUTDOWN_WAIT_KEY.
     */
    public static final String SHUTDOWN_WAIT_KEY = "rpc.service.shutdown.wait";

    /**
     * The constant DEFAULT_SHUTDOWN_TIMEOUT.
     */
    public static final int DEFAULT_SHUTDOWN_TIMEOUT = 1000 * 60 * 15;

    /**
     * The constant DEFAULT_RECONNECT_PERIOD.
     */
    public static final int DEFAULT_RECONNECT_PERIOD = 2000;

    /**
     * The constant DEFAULT_SERVER_SHUTDOWN_TIMEOUT.
     */
    public static final int DEFAULT_SERVER_SHUTDOWN_TIMEOUT = 10000;

    /**
     * The constant RECONNECT_KEY.
     */
    public static final String RECONNECT_KEY = "reconnect";

    /**
     * The constant CHECK_KEY.
     */
    public static final String CHECK_KEY = "check";

    /**
     * The constant TOKEN_KEY.
     */
    public static final String TOKEN_KEY = "token";

    /**
     * The constant APPLICATION_KEY.
     */
    public static final String APPLICATION_KEY = "application";

    /**
     * The constant PATH_KEY.
     */
    public static final String PATH_KEY = "path";

    /**
     * The constant ASYNC_KEY.
     */
    public static final String ASYNC_KEY = "async";

    /**
     * The constant $INVOKE.
     */
    public static final String $INVOKE = "$invoke";

    /**
     * The constant REGISTER_KEY.
     */
    public static final String REGISTER_KEY = "register";

    /**
     * The constant AUTO_ATTACH_INVOCATION_ID_KEY.
     */
    public static final String AUTO_ATTACH_INVOCATION_ID_KEY = "invocationid.autoattach";

    /**
     * The constant ID_KEY.
     */
    public static final String ID_KEY = "id";

    /**
     * The constant RETURN_KEY.
     */
    public static final String RETURN_KEY = "return";

    /**
     * The constant DEFAULT_RPC_PROPERTIES.
     */
    public static final String DEFAULT_RPC_PROPERTIES = "rpc.properties";

    /**
     * The constant CONNECTIONS_KEY.
     */
    public static final String CONNECTIONS_KEY = "connections";

    /**
     * The constant DEFAULT_REMOTING_CLIENT.
     */
    public static final String DEFAULT_REMOTING_CLIENT = "netty";

    /**
     * The constant LAZY_CONNECT_KEY.
     */
    public static final String LAZY_CONNECT_KEY = "lazy";

    /**
     * The constant TIMESTAMP_KEY.
     */
    public static final String TIMESTAMP_KEY = "timestamp";

    /**
     * The constant LAZY_CONNECT_INITIAL_STATE_KEY.
     */
    public static final String LAZY_CONNECT_INITIAL_STATE_KEY = "connect.lazy.initial.state";

    /**
     * The constant CALLBACK_SERVICE_KEY.
     */
    public static final String CALLBACK_SERVICE_KEY = "callback.service.instId";

    /**
     * The constant DEFAULT_LAZY_CONNECT_INITIAL_STATE.
     */
    public static final boolean DEFAULT_LAZY_CONNECT_INITIAL_STATE = true;

    /**
     * The constant RETURN_PREFIX.
     */
    public static final String RETURN_PREFIX = "return ";

    /**
     * The constant ANY_HOST_VALUE.
     */
    public static final String ANY_HOST_VALUE = "0.0.0.0";

    /**
     * The constant REGISTRY_PROTOCOL.
     */
    public static final String REGISTRY_PROTOCOL = "registry";

    /**
     * The constant REGISTRY_KEY.
     */
    public static final String REGISTRY_KEY = "registry";

    /**
     * The constant PID_KEY.
     */
    public static final String PID_KEY = "pid";

    /**
     * The constant SUBSCRIBE_KEY.
     */
    public static final String SUBSCRIBE_KEY = "subscribe";

    /**
     * The constant BACKUP_KEY.
     */
    public static final String BACKUP_KEY = "backup";
    /**
     * The constant CLUSTER_STICKY_KEY.
     */
    public static final String CLUSTER_STICKY_KEY = "sticky";
    /**
     * The constant DEFAULT_REGISTRY.
     */
    public static final String DEFAULT_REGISTRY = "rpc";

    /**
     * The constant ANY_HOST_KEY.
     */
    public static String ANY_HOST_KEY = "anyhost";

    /**
     * The constant PROVIDER_SIDE.
     */
    public static final String PROVIDER_SIDE = "provider";

    /**
     * The constant DEFAULT_KEY.
     */
    public static final String DEFAULT_KEY = "default";

    /**
     * The constant ANY_VALUE.
     */
    public static final String ANY_VALUE = "*";

    /**
     * The constant SCOPE_KEY.
     */
    public static final String SCOPE_KEY ="scope";

    /**
     * The constant CATEGORY_KEY.
     */
    public static final String CATEGORY_KEY = "category";

    /**
     * The constant PRIORITY_KEY.
     */
    public static final String PRIORITY_KEY = "priority";

    /**
     * The constant SCOPE_NONE.
     */
    public static final String SCOPE_NONE = "none";

    /**
     * The constant SCOPE_LOCAL.
     */
    public static final String SCOPE_LOCAL = "local";

    /**
     * The constant SCOPE_REMOTE.
     */
    public static final String SCOPE_REMOTE = "remote";

    /**
     * The constant MONITOR_KEY.
     */
    public static final String MONITOR_KEY = "monitor";

    /**
     * The constant LOCAL_PROTOCOL.
     */
    public static final String LOCAL_PROTOCOL = "injvm";

    /**
     * The constant PROTOCOL_KEY.
     */
    public static final String PROTOCOL_KEY = "protocol";

    /**
     * The constant EXPORT_KEY.
     */
    public static final String EXPORT_KEY = "export";

    /**
     * The constant REFER_KEY.
     */
    public static final String REFER_KEY = "refer";

    /**
     * The constant INPUT_KEY.
     */
    public static final String INPUT_KEY = "input";

    /**
     * The constant OUTPUT_KEY.
     */
    public static final String OUTPUT_KEY = "output";

    /**
     * The constant BIND_IP_KEY.
     */
    public static final String BIND_IP_KEY = "bind.ip";

    /**
     * The constant BIND_PORT_KEY.
     */
    public static final String BIND_PORT_KEY = "bind.port";

    /**
     * The constant DEFAULT_ACCEPTS.
     */
    public static final int DEFAULT_ACCEPTS = 0;

    /**
     * The constant DEFAULT_IDLE_TIMEOUT.
     */
    public static final int DEFAULT_IDLE_TIMEOUT = 600 * 1000;

    /**
     * The constant RPC_IP_TO_BIND.
     */
    public static final String RPC_IP_TO_BIND = "RPC_IP_TO_BIND";

    /**
     * The constant RPC_IP_TO_REGISTRY.
     */
    public static final String RPC_IP_TO_REGISTRY = "RPC_IP_TO_REGISTRY";

    /**
     * The constant RPC_PORT_TO_BIND.
     */
    public static final String RPC_PORT_TO_BIND = "RPC_PORT_TO_BIND";

    /**
     * The constant RPC_PORT_TO_REGISTRY.
     */
    public static final String RPC_PORT_TO_REGISTRY ="RPC_PORT_TO_REGISTRY";

    /**
     * The constant METHODS_KEY.
     */
    public static final String METHODS_KEY = "methods";

    /**
     * The constant ROUTER_KEY.
     */
    public static final String ROUTER_KEY = "router";

    /**
     * The constant FORCE_KEY.
     */
    public static final String FORCE_KEY = "force";

    /**
     * The constant RULE_KEY.
     */
    public static final String RULE_KEY = "rule";

    /**
     * The constant METHOD_KEY.
     */
    public static final String METHOD_KEY = "method";

    /**
     * The constant FILE_KEY.
     */
    public static final String FILE_KEY = "file";

    /**
     * The constant REGISTRY_FILESAVE_SYNC_KEY.
     */
    public static final String REGISTRY_FILESAVE_SYNC_KEY = "save.file";

    /**
     * The constant CLASSIFIER_KEY.
     */
    public static final String CLASSIFIER_KEY = "classifier";

    /**
     * The constant PROVIDERS_CATEGORY.
     */
    public static final String PROVIDERS_CATEGORY = "providers";

    /**
     * The constant DEFAULT_CATEGORY.
     */
    public static final String DEFAULT_CATEGORY  = PROVIDERS_CATEGORY;

    /**
     * The constant REMOVE_VALUE_PREFIX.
     */
    public static final String REMOVE_VALUE_PREFIX = "-";

    /**
     * The constant EMPTY_PROTOCOL.
     */
    public static final String EMPTY_PROTOCOL = "empty";

    /**
     * The constant REGISTRY_RETRY_PERIOD_KEY.
     */
    public static final String REGISTRY_RETRY_PERIOD_KEY = "retry.period";

    /**
     * The constant DEFAULT_REGISTRY_RETRY_PERIOD.
     */
    public static final int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;

    /**
     * The constant REGISTRY_RECONNECT_PERIOD_KEY.
     */
    public static final String REGISTRY_RECONNECT_PERIOD_KEY = "reconnect.period";

    /**
     * The constant CONSUMER_PROTOCOL.
     */
    public static final String CONSUMER_PROTOCOL = "consumer";

    /**
     * The constant ROUTERS_CATEGORY.
     */
    public static final String ROUTERS_CATEGORY = "routers";

    /**
     * The constant ROUTE_PROTOCOL.
     */
    public static final String ROUTE_PROTOCOL = "route";

    /**
     * The constant OVERRIDE_PROTOCOL.
     */
    public static final String OVERRIDE_PROTOCOL = "override";

    /**
     * The constant CONFIGURATORS_CATEGORY.
     */
    public static final String CONFIGURATORS_CATEGORY = "configurators";

    /**
     * The constant DISABLED_KEY.
     */
    public static final String DISABLED_KEY = "disabled";

    /**
     * The constant RUNTIME_KEY.
     */
    public static final String RUNTIME_KEY = "runtime";

    /**
     * The constant ALIVE_KEY.
     */
    public static final String ALIVE_KEY = "alive";

    /**
     * The constant TRANSPORTER_KEY.
     */
    public static final String TRANSPORTER_KEY = "transporter";

    /**
     * The constant REMOTE_TIMESTAMP_KEY.
     */
    public static final String REMOTE_TIMESTAMP_KEY = "remote.timestamp";

    /**
     * The constant REFERENCE_FILTER_KEY.
     */
    public static final String REFERENCE_FILTER_KEY = "reference.filter";

    /**
     * The constant INVOKER_LISTENER_KEY.
     */
    public static final String INVOKER_LISTENER_KEY = "invoker.listener";

    /**
     * The constant DEPRECATED_KEY.
     */
    public static final String DEPRECATED_KEY = "deprecated";

    /**
     * The constant EXPORTER_LISTENER_KEY.
     */
    public static final String EXPORTER_LISTENER_KEY = "exporter.listener";

    /**
     * The constant SERVICE_FILTER_KEY.
     */
    public static final String SERVICE_FILTER_KEY = "service.filter";

    /**
     * The constant DEFAULT_DIRECTORY.
     */
    public static final String DEFAULT_DIRECTORY = "rpc";
    /**
     * The constant HIDE_KEY_PREFIX.
     */
    public static final String HIDE_KEY_PREFIX = ".";
    /**
     * The constant PROVIDER_PROTOCOL.
     */
    public static final String PROVIDER_PROTOCOL = "provider";

    /**
     * The constant CLUSTER_AVAILABLE_CHECK_KEY.
     */
    public static final String CLUSTER_AVAILABLE_CHECK_KEY = "cluster.available.check";

    /**
     * The constant DEFAULT_CLUSTER_AVAILABLE_CHECK.
     */
    public static final boolean DEFAULT_CLUSTER_AVAILABLE_CHECK = true;

    /**
     * The constant DEFAULT_CLUSTER_STICKY.
     */
    public static final boolean DEFAULT_CLUSTER_STICKY = false;


    /**
     * The constant DEFAULT_CLUSTER.
     */
    public static final String DEFAULT_CLUSTER = "failover";

    /**
     * The constant DEFAULT_LOADBALANCE.
     */
    public static final String DEFAULT_LOADBALANCE = "random";

    /**
     * The constant LOADBALANCE_KEY.
     */
    public static final String LOADBALANCE_KEY = "loadbalance";

    /**
     * The constant RETRIES_KEY.
     */
    public static final String RETRIES_KEY = "retries";

    /**
     * The constant DEFAULT_RETRIES.
     */
    public static final int DEFAULT_RETRIES = 2;

    /**
     * The constant PROXY_KEY.
     */
    public static final String PROXY_KEY = "proxy";

    /**
     * The constant REGISTER_IP_KEY.
     */
    public static final String REGISTER_IP_KEY = "register.ip";

    /**
     * The constant SEMICOLON_SPLIT_PATTERN.
     */
    public static final Pattern SEMICOLON_SPLIT_PATTERN = Pattern
            .compile("\\s*[;]+\\s*");

    /**
     * The constant DEFAULT_WARM_UP.
     */
    public static final int DEFAULT_WARM_UP = 10 * 60 * 1000;

    /**
     * The limit of callback service instances for one interface on every client
     */
    public static final String CALLBACK_INSTANCES_LIMIT_KEY = "callbacks";

    public static final int DEFAULT_CALLBACK_INSTANCES = 1;

}

