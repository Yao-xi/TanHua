package com.yx.tanhua.server.controller;

import com.yx.tanhua.server.service.CommentsService;
import com.yx.tanhua.server.service.MovementsService;
import com.yx.tanhua.server.service.QuanziMQService;
import com.yx.tanhua.server.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("comments")
public class CommentsController {
    
    @Autowired
    private CommentsService commentsService;
    
    @Autowired
    private MovementsService movementsService;
    
    @Autowired
    private QuanziMQService quanziMQService;
    
    /**
     * 查询评论列表
     *
     * @param publishId
     *     查询评论的动态的id
     * @param page
     *     当前页码
     * @param pagesize
     *     每页条数
     *
     * @return {@link ResponseEntity<PageResult>}
     */
    @GetMapping
    public ResponseEntity<PageResult> queryCommentsList(@RequestParam("movementId") String publishId,
                                                        @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                        @RequestParam(value = "pagesize", defaultValue = "10") Integer pagesize) {
        try {
            // 调用service
            PageResult pageResult = this.commentsService.queryCommentsList(publishId, page, pagesize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 保存评论
     *
     * @param param
     *     {@code movementId} 动态的id
     *     <p>
     *     {@code comment} 评论内容
     *
     * @return {@link ResponseEntity<Void>}
     */
    @PostMapping
    public ResponseEntity<Void> saveComments(@RequestBody Map<String, String> param) {
        try {
            // 从请求体中获取参数
            // 动态的id
            String publishId = param.get("movementId");
            // 评论内容
            String content = param.get("comment");
            // 调用service
            Boolean result = this.commentsService.saveComments(publishId, content);
            if (result) {
                // 发送消息
                this.quanziMQService.sendCommentPublishMsg(publishId);
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 点赞评论
     *
     * @param publishId
     *     评论的id
     *
     * @return {@link ResponseEntity<Long>}
     */
    @GetMapping("/{id}/like")
    public ResponseEntity<Long> likeComment(@PathVariable("id") String publishId) {
        try {
            // 评论视为一种发布的动态 点赞可以调用movementsService.likeComment方法
            // 调用service
            Long likeCount = this.movementsService.likeComment(publishId);
            if (likeCount != null) {
                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 取消点赞评论
     *
     * @param publishId
     *     评论的id
     *
     * @return {@link ResponseEntity<Long>}
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity<Long> disLikeComment(@PathVariable("id") String publishId) {
        try {
            // 调用service
            Long likeCount = this.movementsService.cancelLikeComment(publishId);
            if (null != likeCount) {
                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
