package com.yx.tanhua.sso.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yx.tanhua.sso.config.HuanXinConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 获取环信的token
 */
@Service
@Slf4j
public class HuanXinTokenService {
    
    public static final String REDIS_KEY = "HX_TOKEN";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    @Autowired
    private HuanXinConfig huanXinConfig;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 获取token
     *
     * @return {@link String} token
     */
    public String getToken() {
        String token = this.redisTemplate.opsForValue().get(REDIS_KEY);
        if (StringUtils.isBlank(token)) {
            // 如果缓存不存在 刷新token
            return this.refreshToken();
        }
        return token;
    }
    
    /**
     * 刷新token
     * @return {@link String} token
     */
    private String refreshToken() {
        // 拼接url 固定写法
        String targetUrl =
            this.huanXinConfig.getUrl() + this.huanXinConfig.getOrgName() + "/" + this.huanXinConfig.getAppName() +
            "/token";
        // 构造请求参数 固定写法
        Map<String, String> param = new HashMap<>();
        param.put("grant_type", "client_credentials");
        param.put("client_id", this.huanXinConfig.getClientId());
        param.put("client_secret", this.huanXinConfig.getClientSecret());
        
        // 请求环信接口 固定写法
        ResponseEntity<String> responseEntity =
            this.restTemplate.postForEntity(targetUrl, param, String.class);
        
        if (responseEntity.getStatusCodeValue() != 200) {
            // 响应错误
            return null;
        }
        // 获取响应体
        String body = responseEntity.getBody();
        try {
            // 解析响应体
            JsonNode jsonNode = MAPPER.readTree(body);
            String accessToken = jsonNode.get("access_token").asText();
            if (StringUtils.isNotBlank(accessToken)) {
                // 将token保存到redis 有效期为5天 环信接口返回的有效期为6天
                this.redisTemplate.opsForValue().set(REDIS_KEY, accessToken, Duration.ofDays(5));
                return accessToken;
            }
        } catch (Exception e) {
            log.error("获取环信token出错 ~ huanXinConfig="+huanXinConfig,e);
        }
        
        return null;
    }
}