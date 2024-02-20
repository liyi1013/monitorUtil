package com.liyi.monitorutil.checker.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PortChecker {

    /**
     * The logger used to log information about the HttpCheck class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PortChecker.class);

    /**
     * 检查指定主机和端口是否开放。
     * @param host 主机名或IP地址
     * @param port 端口号
     * @return 如果端口开放则返回true，否则返回false
     */
    public static boolean isPortOpen(String host, int port) {
        try (Socket socket = new Socket()) {
            // 设置超时时间，避免无限期等待连接
            socket.setSoTimeout(1000); // 设置超时时间为1秒
            socket.connect(new InetSocketAddress(host, port), 1000); // 尝试连接到指定的主机和端口
            LOGGER.info("Port {} is open", port);
            return true; // 如果连接成功，则端口是开放的
        } catch (IOException e) {
            LOGGER.error("Port {} is not open", port);
            // 连接失败则认为端口未开放或者网络不通
            return false;
        }
    }


    /**
     * 检查指定主机和端口是否开放。
     *
     * @param host 主机名或IP地址
     * @param port 端口号
     * @return 如果端口开放则返回true，否则返回false
     */
    public boolean check(String host, int port) {
        return PortChecker.isPortOpen(host, port);
    }
}
