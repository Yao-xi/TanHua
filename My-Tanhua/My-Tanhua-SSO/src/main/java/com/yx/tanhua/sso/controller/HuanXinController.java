package com.yx.tanhua.sso.controller;

import com.yx.tanhua.sso.service.HuanXinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}