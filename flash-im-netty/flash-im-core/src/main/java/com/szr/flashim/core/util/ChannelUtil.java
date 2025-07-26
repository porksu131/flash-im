package com.szr.flashim.core.util;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ChannelUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(ChannelUtil.class);

    public static String parseChannelRemoteAddr(final Channel channel) {
        if (channel == null) {
            return "";
        }
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        if (socketAddress != null) {
            return socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort();
        }
        return "";
    }

    public static String parseChannelLoaclAddr(final Channel channel) {
        if (channel == null) {
            return "";
        }
        InetSocketAddress socketAddress = (InetSocketAddress) channel.localAddress();
        if (socketAddress != null) {
            return socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort();
        }
        return "";
    }

    public static int parseChannelRemoteAddrPort(final Channel channel) {
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        if (socketAddress != null) {
            return socketAddress.getPort();
        }
        return 0;
    }

    public static void closeChannel(final Channel channel) {
        channel.close().addListener(future -> {
            if (future.isSuccess()) {
                LOGGER.info("close the connection:" + parseChannelRemoteAddr(channel));
            }
        });
    }

    public static String parseSocketAddressAddr(SocketAddress socketAddress) {
        if (socketAddress != null) {
            // Default toString of InetSocketAddress is "hostName/IP:port"
            final String addr = socketAddress.toString();
            int index = addr.lastIndexOf("/");
            return (index != -1) ? addr.substring(index + 1) : addr;
        }
        return "";
    }

    public static Integer parseSocketAddressPort(SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress) {
            return ((InetSocketAddress) socketAddress).getPort();
        }
        return -1;
    }

    public static String normalizeHostAddress(final InetAddress localHost) {
        if (localHost instanceof Inet6Address) {
            return "[" + localHost.getHostAddress() + "]";
        } else {
            return localHost.getHostAddress();
        }
    }

    public static boolean isInternalIP(byte[] ip) {
        if (ip.length != 4) {
            throw new RuntimeException("illegal ipv4 bytes");
        }

        //10.0.0.0~10.255.255.255
        //172.16.0.0~172.31.255.255
        //192.168.0.0~192.168.255.255
        //127.0.0.0~127.255.255.255
        if (ip[0] == (byte) 10) {
            return true;
        } else if (ip[0] == (byte) 127) {
            return true;
        } else if (ip[0] == (byte) 172) {
            return ip[1] >= (byte) 16 && ip[1] <= (byte) 31;
        } else if (ip[0] == (byte) 192) {
            return ip[1] == (byte) 168;
        }
        return false;
    }

    public static boolean isInternalV6IP(InetAddress inetAddr) {
        return inetAddr.isAnyLocalAddress() // Wild card ipv6
                || inetAddr.isLinkLocalAddress() // Single broadcast ipv6 address: fe80:xx:xx...
                || inetAddr.isLoopbackAddress() //Loopback ipv6 address
                || inetAddr.isSiteLocalAddress();// Site local ipv6 address: fec0:xx:xx...
    }

}
