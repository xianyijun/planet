package cn.xianyijun.planet.utils;

import cn.xianyijun.planet.common.URL;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * The type Net utils.
 */
@Slf4j
public class NetUtils {
    private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");

    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    /**
     * The constant LOCALHOST.
     */
    public static final String LOCALHOST = "127.0.0.1";
    /**
     * The constant ANY_HOST.
     */
    public static final String ANY_HOST = "0.0.0.0";

    private static final int RND_PORT_START = 30000;
    private static final int RND_PORT_RANGE = 10000;

    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    private static volatile InetAddress LOCAL_ADDRESS = null;

    /**
     * Filter local host string.
     *
     * @param host the host
     * @return the string
     */
    public static String filterLocalHost(String host) {
        if (host == null || host.length() == 0) {
            return host;
        }
        if (host.contains("://")) {
            URL url = URL.valueOf(host);
            if (NetUtils.isInvalidLocalHost(url.getHost())) {
                return url.setHost(NetUtils.getLocalHost()).toFullString();
            }
        } else if (host.contains(":")) {
            int i = host.lastIndexOf(':');
            if (NetUtils.isInvalidLocalHost(host.substring(0, i))) {
                return NetUtils.getLocalHost() + host.substring(i);
            }
        } else {
            if (NetUtils.isInvalidLocalHost(host)) {
                return NetUtils.getLocalHost();
            }
        }
        return host;
    }

    /**
     * Is invalid local host boolean.
     *
     * @param host the host
     * @return the boolean
     */
    public static boolean isInvalidLocalHost(String host) {
        return host == null
                || host.length() == 0
                || "localhost".equalsIgnoreCase(host)
                || "0.0.0.0".equals(host)
                || (LOCAL_IP_PATTERN.matcher(host).matches());
    }

    /**
     * Gets local host.
     *
     * @return the local host
     */
    public static String getLocalHost() {
        InetAddress address = getLocalAddress();
        return address == null ? LOCALHOST : address.getHostAddress();
    }

    /**
     * Gets local address.
     *
     * @return the local address
     */
    public static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS != null) {
            return LOCAL_ADDRESS;
        }
        InetAddress localAddress = getLocalAddress0();
        LOCAL_ADDRESS = localAddress;
        return localAddress;
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable e) {
            log.warn("Failed to retriving ip address, " + e.getMessage(), e);
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable e) {
                                    log.warn("Failed to retriving ip address, " + e.getMessage(), e);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        log.warn("Failed to retriving ip address, " + e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            log.warn("Failed to retriving ip address, " + e.getMessage(), e);
        }
        log.error("Could not get local host ip address, will use 127.0.0.1 instead.");
        return localAddress;
    }


    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }
        String name = address.getHostAddress();
        return (name != null
                && !ANY_HOST.equals(name)
                && !LOCALHOST.equals(name)
                && IP_PATTERN.matcher(name).matches());
    }

    /**
     * To address string string.
     *
     * @param address the address
     * @return the string
     */
    public static String toAddressString(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }

    /**
     * Gets available port.
     *
     * @param port the port
     * @return the available port
     */
    public static int getAvailablePort(int port) {
        if (port <= 0) {
            return getAvailablePort();
        }
        for (int i = port; i < MAX_PORT; i++) {
            ServerSocket ss = null;
            try {
                ss = new ServerSocket(i);
                return i;
            } catch (IOException e) {
            } finally {
                if (ss != null) {
                    try {
                        ss.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return port;
    }

    /**
     * Gets available port.
     *
     * @return the available port
     */
    public static int getAvailablePort() {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.bind(null);
            return ss.getLocalPort();
        } catch (IOException e) {
            return getRandomPort();
        } finally {
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static String getIpByHost(String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException e) {
            return hostName;
        }
    }

    /**
     * Gets random port.
     *
     * @return the random port
     */
    public static int getRandomPort() {
        return RND_PORT_START + RANDOM.nextInt(RND_PORT_RANGE);
    }

    /**
     * Is invalid port boolean.
     *
     * @param port the port
     * @return the boolean
     */
    public static boolean isInvalidPort(int port) {
        return port <= MIN_PORT || port > MAX_PORT;
    }
}
