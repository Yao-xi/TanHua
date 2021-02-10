package com.yx.tanhua.server.controller;

import com.yx.tanhua.server.service.UsersService;
import com.yx.tanhua.server.vo.CountsVo;
import com.yx.tanhua.server.vo.PageResult;
import com.yx.tanhua.server.vo.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
@Slf4j
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
    public ResponseEntity<Void> updateUserInfo(@RequestBody UserInfoVo userInfoVo) {
        try {
            Boolean bool = this.usersService.updateUserInfo(userInfoVo);
            if (bool) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 互相喜欢 喜欢 粉丝 - 统计
     *
     * @return {@link ResponseEntity<CountsVo>}
     */
    @GetMapping("counts")
    public ResponseEntity<CountsVo> queryCounts() {
        try {
            CountsVo countsVo = this.usersService.queryCounts();
            if (countsVo != null) {
                return ResponseEntity.ok(countsVo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 互相关注 我关注 粉丝 谁看过我 - 翻页列表
     *
     * @param type
     *     1 互相关注 2 我的关注 3 粉丝 4 谁看过我
     */
    @GetMapping("friends/{type}")
    public ResponseEntity<PageResult> queryLikeList(@PathVariable("type") String type,
                                                    @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                    @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize,
                                                    @RequestParam(value = "nickname", required = false) String nickname) {
        try {
            page = Math.max(1, page);
            PageResult pageResult = this.usersService.queryLikeList(Integer.valueOf(type), page, pageSize, nickname);
            // --- debug ---
            String str;
            switch (Integer.parseInt(type)) {
                case 1:
                    str = "互相关注";
                    break;
                case 2:
                    str ="我的关注";
                    break;
                case 3:
                    str ="粉丝";
                    break;
                case 4:
                    str ="谁看过我";
                    break;
                default:
                    str = "";
                    break;
            }
            log.debug("type:"+str+" 列表 :" + pageResult.getItems());
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 取消喜欢
     *
     * @param userId
     *     不喜欢的用户id
     */
    @DeleteMapping("like/{uid}")
    public ResponseEntity<Void> disLike(@PathVariable("uid") Long userId) {
        try {
            this.usersService.disLike(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 关注粉丝
     *
     * @param userId
     *     粉丝id
     */
    @PostMapping("fans/{uid}")
    public ResponseEntity<Void> likeFan(@PathVariable("uid") Long userId) {
        try {
            this.usersService.likeFan(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}