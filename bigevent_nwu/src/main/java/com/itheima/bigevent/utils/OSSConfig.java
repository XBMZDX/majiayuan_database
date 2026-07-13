package com.itheima.bigevent.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class OSSConfig {
    @Value("${oss.access-key-id}")
    private String accessKeyId;

    @Value("${oss.access-key-secret}")
    private String accessKeySecret;

    private static String ak;
    private static String sk;

    @PostConstruct
    public void init() { ak = accessKeyId; sk = accessKeySecret; }

    public static String getAccessKeyId() { return ak; }
    public static String getAccessKeySecret() { return sk; }
}
