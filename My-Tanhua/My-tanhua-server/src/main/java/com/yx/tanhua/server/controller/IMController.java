package com.yx.tanhua.server.controller;

import com.yx.tanhua.server.service.IMService;
import com.yx.tanhua.server.utils.NoAuthorization;
import com.yx.tanhua.server.vo.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("messages")
public class IMController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(IMController.class);
    
    @Autowired
    private IMService imService;
    
    /**
     * 添加好友
     *
     * @param param
     *     userId 好友id
     *
     * @return {@link ResponseEntity<Void>}
     */
    @PostMapping("contacts")
    public ResponseEntity<Void> contactUser(@RequestBody Map<String, Object> param) {
        try {
            Long userId = Long.valueOf(param.get("userId").toString());
            boolean result = this.imService.contactUser(userId);
            if (result) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            LOGGER.error("添加联系人失败! param=" + param, e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 查询联系人列表
     *
     * @param keyword
     *     关键词
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link ResponseEntity<PageResult>}
     */
    @GetMapping("contacts")
    public ResponseEntity<PageResult> queryContactsList(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize,
        @RequestParam(value = "keyword", required = false) String keyword) {
        
        PageResult pageResult = this.imService.queryContactsList(page, pageSize, keyword);
        return ResponseEntity.ok(pageResult);
    }
    
    /**
     * 查询点赞列表
     */
    @GetMapping("likes")
    public ResponseEntity<PageResult> queryMessageLikeList(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize) {
        
        PageResult pageResult = this.imService.queryMessageLikeList(page, pageSize);
        return ResponseEntity.ok(pageResult);
    }
    
    /**
     * 查询评论列表
     */
    @GetMapping("comments")
    public ResponseEntity<PageResult> queryMessageCommentList(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize) {
        
        PageResult pageResult = this.imService.queryMessageCommentList(page, pageSize);
        return ResponseEntity.ok(pageResult);
    }
    
    /**
     * 查询喜欢列表
     */
    @GetMapping("loves")
    public ResponseEntity<PageResult> queryMessageLoveList(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize) {
        
        // 调用service
        PageResult pageResult = this.imService.queryMessageLoveList(page, pageSize);
        return ResponseEntity.ok(pageResult);
    }
    
    /**
     * 查询公告列表
     */
    @GetMapping("announcements")
    @NoAuthorization  // 公告无需进行token校验
    public ResponseEntity<PageResult> queryMessageAnnouncementList(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize) {
        
        // 调用service
        PageResult pageResult = this.imService.queryMessageAnnouncementList(page, pageSize);
        return ResponseEntity.ok(pageResult);
    }
}