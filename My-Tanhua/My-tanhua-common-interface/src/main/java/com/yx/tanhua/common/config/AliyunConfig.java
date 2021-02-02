package com.yx.tanhua.common.config;

import com.aliyun.oss.OSSClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 阿里云OSS配置
 *
 * @author Yaoxi
 * @date 2021/01/17 11:42:26
 */
@Configuration
@PropertySource(value = "classpath:aliyun.properties",encoding = "UTF-8")
@ConfigurationProperties(prefix = "aliyun")
@Data
public class AliyunConfig {
    
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String urlPrefix;
    
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    @Bean
    public OSSClient oSSClient() {
        return new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }
}
