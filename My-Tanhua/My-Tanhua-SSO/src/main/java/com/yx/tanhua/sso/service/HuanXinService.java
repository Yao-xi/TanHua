package com.yx.tanhua.sso.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yx.tanhua.common.vo.HuanXinUser;
import com.yx.tanhua.sso.config.HuanXinConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class HuanXinService {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    @Autowired
    private HuanXinTokenService huanXinTokenService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private HuanXinConfig huanXinConfig;
    
    /**
     * 注册环信用户
     *
     * @param userId
     *
     * @return boolean
     */
    public boolean register(Long userId) {
        // 拼接url 固定写法
        String targetUrl = this.huanXinConfig.getUrl()
                           + this.huanXinConfig.getOrgName() + "/"
                           + this.huanXinConfig.getAppName() + "/users";
        log.debug("rest url="+targetUrl);
        String token = this.huanXinTokenService.getToken();
        
        try {
            // 请求体 固定写法
            // TODO 密码生成策略建议改用随机的
            HuanXinUser huanXinUser =
                new HuanXinUser(String.valueOf(userId), DigestUtils.md5Hex(userId + "_tanhua"));
            String body = MAPPER.writeValueAsString(Collections.singletonList(huanXinUser));
            
            // 请求头 固定写法
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("Authorization", "Bearer " + token);
            
            // 发请求 固定写法
            HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> responseEntity =
                this.restTemplate.postForEntity(targetUrl, httpEntity, String.class);
            
            return responseEntity.getStatusCodeValue() == 200;
        } catch (Exception e) {
            log.error("注册环信用户失败 ~ userId=" + userId, e);
        }
        
        // 注册失败
        return false;
        
    }
    
    /**
     * 添加好友
     *
     * @param userId
     *     用户id
     * @param friendId
     *     好友id
     *
     * @return boolean 是否添加成功
     */
    public boolean contactUsers(Long userId, Long friendId) {
        // 构造访问url
        String targetUrl = this.huanXinConfig.getUrl()
                           + this.huanXinConfig.getOrgName() + "/"
                           + this.huanXinConfig.getAppName() + "/users/" +
                           userId + "/contacts/users/" + friendId;
        log.debug("rest url="+targetUrl);
        try {
            // 获取token
            String token = this.huanXinTokenService.getToken();
            // 请求头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);
            // 发请求获取响应
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity =
                this.restTemplate.postForEntity(targetUrl, httpEntity, String.class);
            // 响应成功 即添加成功 返回true
            return responseEntity.getStatusCodeValue() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 添加失败
        return false;
    }
    
    /**
     * 环信发消息
     * @return boolean
     */
    public boolean sendMsg(String target, String type, String msg) {
        // 构造url
        String targetUrl = this.huanXinConfig.getUrl()
                           + this.huanXinConfig.getOrgName() + "/"
                           + this.huanXinConfig.getAppName() + "/messages";
        log.debug("rest url="+targetUrl);
        try {
            // 获取环信token
            String token = this.huanXinTokenService.getToken();
            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + token);
            // 构造请求参数
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("target_type", "users");
            paramMap.put("target", Collections.singletonList(target));
            
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("type", type);
            msgMap.put("msg", msg);
            
            paramMap.put("msg", msgMap);
            
            // 表示消息发送者
            // 无此字段Server会默认设置为"from":"admin"
            // 有from字段但值为空串("")时请求失败
            // msgMap.put("from", type);
            
            // 发请求
            HttpEntity<String> httpEntity = new HttpEntity<>(MAPPER.writeValueAsString(paramMap), headers);
            ResponseEntity<String> responseEntity =
                this.restTemplate.postForEntity(targetUrl, httpEntity, String.class);
            
            return responseEntity.getStatusCodeValue() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}