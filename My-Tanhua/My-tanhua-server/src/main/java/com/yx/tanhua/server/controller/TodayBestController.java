package com.yx.tanhua.server.controller;

import com.yx.tanhua.server.service.TodayBestService;
import com.yx.tanhua.server.utils.Cache;
import com.yx.tanhua.server.utils.NoAuthorization;
import com.yx.tanhua.server.vo.NearUserVo;
import com.yx.tanhua.server.vo.PageResult;
import com.yx.tanhua.server.vo.RecommendUserQueryParam;
import com.yx.tanhua.server.vo.TodayBest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("tanhua")
@Slf4j
public class TodayBestController {
    @Autowired
    private TodayBestService todayBestService;
    
    /**
     * 查询今日佳人 GET /tanhua/todayBest
     *
     * @param token
     *     token
     *
     * @return 正常返回今日佳人信息
     *     <p>
     *     异常返回500
     */
    @GetMapping("todayBest")
    @Cache
    @NoAuthorization // 使用原本的校验方式
    public ResponseEntity<TodayBest> queryTodayBest(@RequestHeader("Authorization") String token) {
        log.debug("请求今日佳人数据 ~");
        try {
            // 调用service
            TodayBest todayBest = this.todayBestService.queryTodayBest(token);
            if (null != todayBest) {
                // 正常返回
                return ResponseEntity.ok(todayBest);
            }
        } catch (Exception e) {
            log.error("查询今日佳人出错~ token = " + token, e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
    
    /**
     * 查询推荐用户列表 GET /tanhua/recommendation
     *
     * @param token
     *     token
     * @param queryParam
     *     查询过滤参数
     *
     * @return {@link PageResult} 分页查询结果
     */
    @GetMapping("recommendation")
    @Cache
    @NoAuthorization // 使用原本的校验方式
    public ResponseEntity<PageResult> queryRecommendation(@RequestHeader("Authorization") String token,
                                                          RecommendUserQueryParam queryParam) {
        log.debug("请求推荐用户列表 ~");
        try {
            // 调用service
            PageResult pageResult = this.todayBestService.queryRecommendation(token, queryParam);
            if (null != pageResult) {
                return ResponseEntity.ok(pageResult);
            }
        } catch (Exception e) {
            log.error("查询推荐用户列表出错~ token = " + token, e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        
    }
    
    /**
     * 查询今日佳人详情
     *
     * @param userId
     *     佳人用户id
     *
     * @return {@link ResponseEntity<TodayBest>}
     */
    @GetMapping("{id}/personalInfo")
    public ResponseEntity<TodayBest> queryTodayBest(@PathVariable("id") Long userId) {
        log.debug("请求查询今日佳人详情 ~");
        try {
            TodayBest todayBest = this.todayBestService.queryTodayBest(userId);
            return ResponseEntity.ok(todayBest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 查询陌生人问题
     *
     * @param userId
     *     用户id
     *
     * @return {@link ResponseEntity<String>}
     */
    @GetMapping("strangerQuestions")
    public ResponseEntity<String> queryQuestion(@RequestParam("userId") Long userId) {
        log.debug("请求查询陌生人问题 ~");
        try {
            String question = this.todayBestService.queryQuestion(userId);
            return ResponseEntity.ok(question);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 回复陌生人问题
     */
    @PostMapping("strangerQuestions")
    public ResponseEntity<Void> replyQuestion(@RequestBody Map<String, Object> param) {
        try {
            Long userId = Long.valueOf(param.get("userId").toString());
            String reply = param.get("reply").toString();
            Boolean result = this.todayBestService.replyQuestion(userId, reply);
            if (result) {
                return ResponseEntity.ok().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    
    /**
     * 探花-展现卡片数据
     *
     * @return {@link ResponseEntity<List<TodayBest>>}
     */
    @GetMapping("cards")
    public ResponseEntity<List<TodayBest>> queryCardsList() {
        try {
            List<TodayBest> list = this.todayBestService.queryCardsList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 探花-左滑-喜欢
     *
     * @param likeUserId
     *     对方的id
     *
     * @return {@link ResponseEntity<Void>}
     */
    @GetMapping("{id}/love")
    public ResponseEntity<Void> likeUser(@PathVariable("id") Long likeUserId) {
        try {
            log.debug("请求喜欢~ likeUserId:"+likeUserId);
            Boolean bool = this.todayBestService.likeUser(likeUserId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 探花-右滑-不喜欢
     *
     * @param likeUserId
     *     对方的id
     *
     * @return {@link ResponseEntity<Void>}
     */
    @GetMapping("{id}/unlove")
    public ResponseEntity<Void> disLikeUser(@PathVariable("id") Long likeUserId) {
        try {
            log.debug("请求不喜欢~ likeUserId:"+likeUserId);
            Boolean bool = this.todayBestService.disLikeUser(likeUserId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    
    /**
     * 搜附近
     *
     * @param gender
     *     性别
     * @param distance
     *     距离
     *
     * @return {@link ResponseEntity<List<NearUserVo>>}
     */
    @GetMapping("search")
    public ResponseEntity<List<NearUserVo>> queryNearUser(@RequestParam(value = "gender", required = false) String gender,
                                                          @RequestParam(value = "distance", defaultValue = "2000") String distance) {
        try {
            List<NearUserVo> list = this.todayBestService.queryNearUser(gender, distance);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
