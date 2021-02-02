package com.yx.tanhua.server.controller;

import com.yx.tanhua.server.service.TodayBestService;
import com.yx.tanhua.server.utils.Cache;
import com.yx.tanhua.server.utils.NoAuthorization;
import com.yx.tanhua.server.vo.PageResult;
import com.yx.tanhua.server.vo.RecommendUserQueryParam;
import com.yx.tanhua.server.vo.TodayBest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    
    
}
