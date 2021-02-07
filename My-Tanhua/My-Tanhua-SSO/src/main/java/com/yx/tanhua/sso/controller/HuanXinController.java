package com.yx.tanhua.sso.controller;

import com.yx.tanhua.sso.service.HuanXinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/huanxin")
public class HuanXinController {

    @Autowired
    private HuanXinService huanXinService;
    
    /**
     * 添加联系人
     *
     * @param userId
     *     用户id
     * @param friendId
     *     好友id
     *
     * @return {@link ResponseEntity<Void>}
     */
    @PostMapping("contacts/{owner_username}/{friend_username}")
    public ResponseEntity<Void> contactUsers(@PathVariable("owner_username") Long userId,
                                             @PathVariable("friend_username") Long friendId) {
        try {
            boolean success = this.huanXinService.contactUsers(userId, friendId);
            if (success) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    
    /**
     * 发送环信的系统消息
     *
     * (被tanhua-server模块调用)
     *
     * @param target
     *     目标
     * @param msg
     *     消息
     * @param type
     *     类型
     *
     * @return {@link ResponseEntity<Void>}
     */
    @PostMapping("messages")
    public ResponseEntity<Void> sendMsg(@RequestParam("target") String target,
                                        @RequestParam("msg") String msg,
                                        @RequestParam(value = "type", defaultValue = "txt") String type) {
        try {
            boolean result = this.huanXinService.sendMsg(target, type, msg);
            if (result) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}