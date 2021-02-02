package com.yx.tanhua.server.controller;

import com.yx.tanhua.server.service.CommentsService;
import com.yx.tanhua.server.service.MovementsService;
import com.yx.tanhua.server.service.VideoMQService;
import com.yx.tanhua.server.service.VideoService;
import com.yx.tanhua.server.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/smallVideos")
@Slf4j
public class VideoController {
    @Autowired
    private VideoService videoService;
    @Autowired
    private MovementsService movementsService;
    @Autowired
    private CommentsService commentsService;
    
    @Autowired
    private VideoMQService videoMQService;
    /**
     * 上传小视频
     *
     * @param picFile
     *     封面图片文件
     * @param videoFile
     *     小视频文件
     *
     * @return {@link ResponseEntity<Void>}
     */
    @PostMapping
    public ResponseEntity<Void> saveVideo(
        @RequestParam(value = "videoThumbnail", required = false) MultipartFile picFile,
        @RequestParam(value = "videoFile", required = false) MultipartFile videoFile) {
        try {
            // 调用service
            String id = this.videoService.saveVideo(picFile, videoFile);
            if (StringUtils.isNotEmpty(id)) {
                //发送消息
                this.videoMQService.videoMsg(id);
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            log.error("上传小视频出错 ~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    
    /**
     * 查询小视频列表
     *
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link ResponseEntity<PageResult>}
     */
    @GetMapping
    public ResponseEntity<PageResult> queryVideoList(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize) {
        try {
            // 防止查询出错
            if (page <= 0) {
                page = 1;
            }
            // 调用service
            PageResult pageResult = this.videoService.queryVideoList(page, pageSize);
            if (null != pageResult) {
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            log.error("查询小视频列表出错 ~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 视频用户关注
     *
     * @param userId
     *     关注的用户id
     *
     * @return {@link ResponseEntity<Void>}
     */
    @PostMapping("/{id}/userFocus")
    public ResponseEntity<Void> saveUserFocusComments(@PathVariable("id") Long userId) {
        try {
            // 调用service
            Boolean success = this.videoService.followUser(userId);
            if (success) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            log.error("视频用户关注出错 ~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 取消视频用户关注
     *
     * @param userId
     *     关注的用户id
     *
     * @return {@link ResponseEntity<Void>}
     */
    @PostMapping("/{id}/userUnFocus")
    public ResponseEntity<Void> saveUserUnFocusComments(@PathVariable("id") Long userId) {
        try {
            // 调用service
            Boolean success = this.videoService.disFollowUser(userId);
            if (success) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            log.error("取消视频用户关注出错 ~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 视频点赞
     *
     * @param videoId
     *     视频id
     *
     * @return {@link ResponseEntity<Long>}
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Long> likeComment(@PathVariable("id") String videoId) {
        log.debug("视频点赞~");
        try {
            // 调用service 为视频进行点赞
            Long likeCount = movementsService.likeComment(videoId);
            if (likeCount != null) {
                videoMQService.likeVideoMsg(videoId);
                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            log.error("视频点赞出错 ~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 取消点赞
     *
     * @param videoId
     *     视频id
     *
     * @return {@link ResponseEntity<Long>}
     */
    @PostMapping("/{id}/dislike")
    public ResponseEntity<Long> disLikeComment(@PathVariable("id") String videoId) {
        try {
            // 调用service
            Long likeCount = this.movementsService.cancelLikeComment(videoId);
            if (null != likeCount) {
                videoMQService.disLikeVideoMsg(videoId);
                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            log.error("取消点赞出错 ~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    
    /**
     * 视频-评论列表
     *
     * @param videoId
     *     视频id
     * @param page
     *     当前页码
     * @param pagesize
     *     每页条数
     *
     * @return {@link ResponseEntity<PageResult>}
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<PageResult> queryCommentsList(@PathVariable("id") String videoId,
                                                        @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                        @RequestParam(value = "pagesize", defaultValue = "10") Integer pagesize) {
        try {
            // 调用service
            PageResult pageResult = commentsService.queryCommentsList(videoId, page, pagesize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            log.error("视频-评论列表获取出错 ~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 视频-提交评论
     *
     * @param param
     *     {@code content} 评论内容
     * @param videoId
     *     视频id
     *
     * @return {@link ResponseEntity<Void>}
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<Void> saveComments(@RequestBody Map<String, String> param,
                                             @PathVariable("id") String videoId) {
        try {
            //评论内容
            String content = param.get("comment");
            Boolean success = commentsService.saveComments(videoId, content);
            if (success) {
                videoMQService.commentVideoMsg(videoId);
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            log.error("视频-提交评论出错 ~", e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    
    /**
     * 视频-评论-点赞
     *
     * @param videoCommentId
     *     视频评论ID
     *
     * @return {@link ResponseEntity<Long>}
     */
    @PostMapping("/comments/{id}/like")
    public ResponseEntity<Long> commentsLikeComment(@PathVariable("id") String videoCommentId) {
        return this.likeComment(videoCommentId);
        // try {
        //     // 调用service 为视频评论进行点赞
        //     Long likeCount = movementsService.likeComment(videoCommentId);
        //     if (likeCount != null) {
        //         return ResponseEntity.ok(likeCount);
        //     }
        // } catch (Exception e) {
        //     log.error("视频-评论-点赞出错 ~", e);
        // }
        // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 视频-评论-取消点赞
     *
     * @param videoCommentId
     *     视频评论ID
     *
     * @return {@link ResponseEntity<Long>}
     */
    @PostMapping("/comments/{id}/dislike")
    public ResponseEntity<Long> disCommentsLikeComment(@PathVariable("id") String videoCommentId) {
        return this.disLikeComment(videoCommentId);
        // try {
        //     // 调用service
        //     Long likeCount = this.movementsService.cancelLikeComment(videoCommentId);
        //     if (null != likeCount) {
        //         return ResponseEntity.ok(likeCount);
        //     }
        // } catch (Exception e) {
        //     log.error("视频-评论-取消点赞出错 ~", e);
        // }
        // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
