package com.itheima.bigevent.utils;

/**
 * 从系统环境变量读取 OSS 凭证，避免密钥泄漏到 Git
 * 支持 Windows 环境变量和 JVM 系统属性
 */
public class OSSConfig {
    public static String getAccessKeyId() {
        String v = System.getProperty("OSS_ACCESS_KEY_ID");
        if (v != null && !v.isEmpty()) return v;
        return System.getenv("OSS_ACCESS_KEY_ID");
    }
    public static String getAccessKeySecret() {
        String v = System.getProperty("OSS_ACCESS_KEY_SECRET");
        if (v != null && !v.isEmpty()) return v;
        return System.getenv("OSS_ACCESS_KEY_SECRET");
    }
}
