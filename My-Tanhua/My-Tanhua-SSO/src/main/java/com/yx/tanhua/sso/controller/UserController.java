package com.yx.tanhua.sso.controller;

import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.sso.service.UserService;
import com.yx.tanhua.common.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yaoxi
 * @date 2021/01/17 11:41:46
 */
@RestController
@RequestMapping("user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    
    /**
     * 用户登录 POST /user/loginVerification
     *
     * @param param
     *     phone 手机号, verificationCode 验证码
     *
     * @return {@link ResponseEntity<Object>}
     *     返回 {@code {"token": "...", "isNew": true} }
     */
    @PostMapping("loginVerification")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> param) {
        log.debug("请求用户登录 ~");
        try {
            log.debug("param = " + param);
            String phone = param.get("phone");
            String code = param.get("verificationCode");
            // 调用service
            String data = this.userService.login(phone, code);
            if (StringUtils.isNotEmpty(data)) {
                //登录成功
                Map<String, Object> result = new HashMap<>(2);
                String[] ss = StringUtils.split(data, '|');
                result.put("token", ss[0]);
                result.put("isNew", Boolean.valueOf(ss[1]));
                
                log.debug(result.toString());
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 登陆失败 返回500+错误消息
        ErrorResult errorResult = ErrorResult.builder().errCode("000002").errMessage("登录失败！").build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }
    
    /**
     * 校验token，根据token查询用户数据 GET /user/{token}
     * <p>
     * 预留给其他模块使用
     *
     * @param token
     *     token
     * @param requestEntity
     *     debug用
     *
     * @return {@link User}
     *     返回user对象
     */
    @GetMapping("{token}")
    public User queryUserByToken(@PathVariable("token") String token, RequestEntity<String> requestEntity) {
        log.debug("直接访问校验token... url: " + requestEntity.getUrl());
        // 调用service
        return this.userService.queryUserByToken(token);
    }
}