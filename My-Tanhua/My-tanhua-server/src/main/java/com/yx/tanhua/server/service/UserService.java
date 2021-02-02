package com.yx.tanhua.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yx.tanhua.common.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class UserService {
    
    /**
     * JSON反序列化用
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    /**
     * 发http请求用
     */
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * sso系统服务地址
     */
    @Value("${tanhua.sso.url}")
    private String ssoUrl;
    
    /**
     * 验证用户令牌
     * <p>
     * 通过sso的rest接口查询
     * <p>
     * 访问url可以通过nginx转发到sso服务
     *
     * @param token
     *     token
     *
     * @return {@link User} 用户对象
     */
    public User queryUserByToken(String token) {
        // 访问url
        String url = ssoUrl + "/user/" + token;
        try {
            // 用restTemplate发http请求 获取响应数据
            String data = this.restTemplate.getForObject(url, String.class);
            log.debug("data = " + data);
            if (StringUtils.isEmpty(data)) {
                return null;
            }
            // JSON反序列化
            return MAPPER.readValue(data, User.class);
        } catch (Exception e) {
            log.error("校验token出错，token = " + token, e);
        }
        return null;
    }
}
