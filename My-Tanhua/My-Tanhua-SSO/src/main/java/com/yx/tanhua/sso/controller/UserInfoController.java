package com.yx.tanhua.sso.controller;

import com.yx.tanhua.sso.service.UserInfoService;
import com.yx.tanhua.common.vo.ErrorResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("user")
@Slf4j
public class UserInfoController {
    
    @Autowired
    private UserInfoService userInfoService;
    
    /**
     * 完善个人信息-基本信息 POST /user/loginReginfo
     *
     * @param param
     *     封装用户信息的json字符串
     * @param token
     *     token 通过请求头中的"Authorization"获取
     *
     * @return {@link ResponseEntity<Object>} 正常响应 200
     */
    @PostMapping("loginReginfo")
    public ResponseEntity<Object> saveUserInfo(@RequestBody Map<String, String> param,
                                               @RequestHeader("Authorization") String token) {
        log.debug("请求完善个人基本信息 ~");
        try {
            // 调用service
            Boolean bool = this.userInfoService.saveUserInfo(param, token);
            if (bool) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ErrorResult errorResult = ErrorResult.builder().errCode("000001").errMessage("保存用户信息失败！").build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }
    
    /**
     * 完善个人信息-用户头像 POST /user/loginReginfo/head
     *
     * @param file
     *     头像文件
     * @param token
     *     token 通过请求头中的"Authorization"获取
     *
     * @return {@link ResponseEntity<Object>} 正常响应 200
     */
    @PostMapping("loginReginfo/head")
    public ResponseEntity<Object> saveUserLogo(@RequestParam("headPhoto") MultipartFile file,
                                               @RequestHeader("Authorization") String token) {
        log.debug("请求完善用户头像 ~");
        try {
            // 调用service
            Boolean bool = this.userInfoService.saveUserLogo(file, token);
            if (bool) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ErrorResult errorResult = ErrorResult.builder().errCode("000001").errMessage("保存用户logo失败！").build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
    }
    
}