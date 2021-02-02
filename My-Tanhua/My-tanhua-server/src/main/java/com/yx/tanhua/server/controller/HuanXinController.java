package com.yx.tanhua.server.controller;

import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.common.vo.HuanXinUser;
import com.yx.tanhua.server.utils.UserThreadLocal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("huanxin")
@Slf4j
public class HuanXinController {
    
    /**
     * 查询环信用户 为了从前端使用用户名和密码直接登录环信
     *
     * @return {@link ResponseEntity<HuanXinUser>}
     */
    @GetMapping("user")
    public ResponseEntity<HuanXinUser> queryHuanXinUser() {
        // 获取当前用户
        User user = UserThreadLocal.get();
        // 构造环信用户
        HuanXinUser huanXinUser = new HuanXinUser();
        huanXinUser.setUsername(String.valueOf(user.getId()));
        huanXinUser.setPassword(DigestUtils.md5Hex(user.getId() + "_tanhua"));
        log.debug("!!! huanXinUser="+huanXinUser);
        return ResponseEntity.ok(huanXinUser);
    }
}