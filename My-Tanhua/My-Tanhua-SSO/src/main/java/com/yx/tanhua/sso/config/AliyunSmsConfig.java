package com.yx.tanhua.sso.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 阿里云短信配置
 * @author Yaoxi
 * @date 2021/01/17 11:42:13
 */
@Configuration
@PropertySource(value = "classpath:aliyun.properties",encoding = "UTF-8")
@ConfigurationProperties(prefix = "aliyun.sms")
@Data
public class AliyunSmsConfig {
    /**
     * 区域Id
     */
    private String regionId;
    /**
     * 访问密钥Id
     */
    private String accessKeyId;
    /**
     * 访问密钥Secret
     */
    private String accessKeySecret;
    /**
     * 域
     */
    private String domain;
    /**
     * 签名
     */
    private String signName;
    /**
     * 模板code
     */
    private String templateCode;
}
