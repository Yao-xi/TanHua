package com.yx.tanhua.sso.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * tx短信配置
 * @author Yaoxi
 * @date 2021/01/17 11:41:52
 */
@Configuration
@PropertySource(value = "classpath:txyun.properties",encoding = "UTF-8")
@ConfigurationProperties(prefix = "txyun.sms")
@Data
public class TxyunSmsConfig {
    /**
     * 区域Id
     */
    private String regionId;
    /**
     * 访问密钥Id
     */
    private String secretId;
    /**
     * 访问密钥Secret
     */
    private String secretKey;
    /**
     * 域
     */
    private String appId;
    /**
     * 签名
     */
    private String sign;
    /**
     * 模板Id
     */
    private String templateId;
}
