package com.yx.tanhua.sso.controller;

import com.yx.tanhua.sso.service.SmsService;
import com.yx.tanhua.common.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Yaoxi
 * @date 2021/01/17 11:41:34
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class SmsController {
    @Autowired
    private SmsService smsService;
    
    /**
     * 发送短信验证码接口
     * <p>
     * ResponseEntity spring对http响应的封装 由标头和正文以及状态码组成 可以链式调用创建对象
     */
    @PostMapping("/login")
    public ResponseEntity<ErrorResult> sendCheckCode(@RequestBody Map<String, String> param) {
        log.debug("请求短信验证码 ~");
        ErrorResult errorResult = null;
        String phone = param.get("phone");
        try {
            // 调用service层发验证码
            errorResult = this.smsService.sendCheckCode(phone);
            if (null == errorResult) {
                // 正常发送
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            // 出现异常导致发送失败
            log.error("发送短信验证码失败~ phone = " + phone, e);
            errorResult = ErrorResult.builder().errCode("000002").errMessage("短信验证码发送失败！").build();
        }
        // 发送失败 响应状态码:500 响应正文:errorResult
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }
}
