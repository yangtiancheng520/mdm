package com.vueadmin.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IP工具类
 */
public class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    private IpUtils() {}

    /**
     * 获取客户端IP地址
     */
    public static String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return UNKNOWN;
        }
        return getClientIp(attributes.getRequest());
    }

    /**
     * 获取客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (isEmptyIp(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isEmptyIp(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isEmptyIp(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isEmptyIp(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (isEmptyIp(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (isEmptyIp(ip)) {
            ip = request.getRemoteAddr();
            if (LOCALHOST_IP.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
                // 根据网卡取本机配置的IP
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ip = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    // ignore
                }
            }
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 判断是否为空IP
     */
    private static boolean isEmptyIp(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }
}
