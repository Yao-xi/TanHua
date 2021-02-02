package com.yx.tanhua.server.controller;

import com.yx.tanhua.server.service.MovementsService;
import com.yx.tanhua.server.service.QuanziMQService;
import com.yx.tanhua.server.vo.Movements;
import com.yx.tanhua.server.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("movements")
@Slf4j
public class MovementsController {
    
    @Autowired
    private MovementsService movementsService;
    
    @Autowired
    private QuanziMQService quanziMQService;
    
    /**
     * 发送动态 POST /movements
     *
     * @param textContent
     *     文字动态
     * @param location
     *     位置
     * @param multipartFile
     *     图片动态
     * @param latitude
     *     纬度
     * @param longitude
     *     经度
     *
     * @return {@link ResponseEntity<Void>}
     */
    @PostMapping()
    public ResponseEntity<Void> savePublish(@RequestParam(value = "textContent", required = false) String textContent,
                                            @RequestParam(value = "location", required = false) String location,
                                            @RequestParam(value = "latitude", required = false) String latitude,
                                            @RequestParam(value = "longitude", required = false) String longitude,
                                            @RequestParam(value = "imageContent", required = false) MultipartFile[] multipartFile) {
        log.debug("请求: 发布动态 ~");
        try {
            // 调用service
            String publishId = this.movementsService.savePublish(
                textContent, location, latitude, longitude, multipartFile);
            if (StringUtils.isEmpty(publishId)) {
                // 发送消息
                this.quanziMQService.publishMsg(publishId);
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 查询好友动态
     *
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link PageResult}
     */
    @GetMapping
    public ResponseEntity<PageResult> queryPublishList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                       @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize) {
        try {
            // 调用service
            PageResult pageResult = movementsService.queryUserPublishList(page, pageSize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 查询推荐动态
     *
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link PageResult}
     */
    @GetMapping("recommend")
    public ResponseEntity<PageResult> queryRecommendPublishList(
        @RequestParam(value = "page", defaultValue = "1") Integer page,
        @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize) {
        try {
            PageResult pageResult = movementsService.queryRecommendPublishList(page, pageSize);
            return ResponseEntity.ok(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 点赞
     *
     * @param publishId
     *     发布id
     *
     * @return {@link ResponseEntity<Long>} 携带点赞数
     */
    @GetMapping("/{id}/like")
    public ResponseEntity<Long> likeComment(@PathVariable("id") String publishId) {
        try {
            // 调用service
            Long likeCount = this.movementsService.likeComment(publishId);
            if (likeCount != null) {
                // 发送点赞消息
                this.quanziMQService.likePublishMsg(publishId);
                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 取消点赞
     *
     * @param publishId
     *     发布id
     *
     * @return {@link ResponseEntity<Long>} 携带点赞数
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity<Long> disLikeComment(@PathVariable("id") String publishId) {
        try {
            // 调用service
            Long likeCount = this.movementsService.cancelLikeComment(publishId);
            if (null != likeCount) {
                // 发送取消点赞消息
                this.quanziMQService.disLikePublishMsg(publishId);
                return ResponseEntity.ok(likeCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 喜欢
     *
     * @param publishId
     *     发布id
     *
     * @return {@link ResponseEntity<Long>} 携带喜欢数
     */
    @GetMapping("/{id}/love")
    public ResponseEntity<Long> loveComment(@PathVariable("id") String publishId) {
        try {
            Long loveCount = this.movementsService.loveComment(publishId);
            if (null != loveCount) {
                // 发送喜欢消息
                this.quanziMQService.lovePublishMsg(publishId);
                return ResponseEntity.ok(loveCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 取消喜欢
     *
     * @param publishId
     *     发布id
     *
     * @return {@link ResponseEntity<Long>} 携带喜欢数
     */
    @GetMapping("/{id}/unlove")
    public ResponseEntity<Long> disLoveComment(@PathVariable("id") String publishId) {
        try {
            Long loveCount = this.movementsService.cancelLoveComment(publishId);
            if (null != loveCount) {
                // 发送取消喜欢消息
                this.quanziMQService.disLovePublishMsg(publishId);
                return ResponseEntity.ok(loveCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 查询单条动态信息
     *
     * @param publishId
     *     发布id
     *
     * @return {@link ResponseEntity<Movements>} 携带动态详细内容的封装
     */
    @GetMapping("/{id}")
    public ResponseEntity<Movements> queryById(@PathVariable("id") String publishId) {
        try {
            log.debug("查询单条动态信息 publishId=" + publishId);
            // 调用service
            Movements movements = this.movementsService.queryById(publishId);
            if (null != movements) {
                // 发送浏览动态消息
                this.quanziMQService.queryPublishMsg(publishId);
                return ResponseEntity.ok(movements);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * todo 谁来看我-暂不完成.
     * 该方法在时是为了不让上边方法报错
     */
    @GetMapping("/visitors")
    public ResponseEntity<Movements> visitors() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 首页点击动态后 查看用户所有动态
     * <p>
     * todo 暂不完成防止上面的方法出错
     */
    @GetMapping("all")
    public ResponseEntity<PageResult> queryAlbumList(@RequestParam(value = "page", defaultValue = "1") Integer page,
                                                     @RequestParam(value = "pagesize", defaultValue = "10") Integer pageSize,
                                                     @RequestParam(value = "userId") Long userId) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}