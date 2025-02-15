package com.yupi.mianshiya.constant;

import com.yupi.mianshiya.utils.InetAddressUtils;

import java.net.InetAddress;

public interface Constants {
    /**
     * 本机IP地址
     */
    InetAddress LOCALHOST_IP = InetAddressUtils.getLocalHostExactAddress();
}
