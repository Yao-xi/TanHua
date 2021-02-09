package com.yx.tanhua.server.controller;

import com.yx.tanhua.server.service.UsersService;
import com.yx.tanhua.server.vo.UserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UsersController {

    @Autowired
    private UsersService usersService;
    
    /**
     * 用户资料 - 读取
     *
     * @param userID
     *     用户id
     * @param huanxinID
     *     环信用户id
     *
     * @return {@link ResponseEntity<UserInfoVo>}
     */
    @GetMapping
    public ResponseEntity<UserInfoVo> queryUserInfo(@RequestParam(value = "userID", required = false) String userID,
                                                    @RequestParam(value = "huanxinID", required = false) String huanxinID) {
        try {
            // 用户第一次 接收到 环信推来的信息 不带用户信息
            // 为了兼容消息模块: app会从环信收到消息, 但是收到的消息只有环信的用户id,
            //    想要展示用户头像, 必须使用环信huanxinID 查询用户消息
            // 所以这个方法有两个参数,传递一个即可
            UserInfoVo userInfoVo = this.usersService.queryUserInfo(userID, huanxinID);
            if (null != userInfoVo) {
                return ResponseEntity.ok(userInfoVo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    /**
     * 更新用户信息
     */
    @PutMapping
    public ResponseEntity<Void> updateUserInfo(@RequestBody UserInfoVo userInfoVo){
        try {
            Boolean bool = this.usersService.updateUserInfo(userInfoVo);
            if(bool){
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}