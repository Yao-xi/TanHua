package com.yx.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yx.tanhua.dubbo.server.api.RecommendUserApi;
import com.yx.tanhua.dubbo.server.pojo.RecommendUser;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import com.yx.tanhua.server.vo.TodayBest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 与dubbo服务进行交互
 *
 * @author Yaoxi
 * @date 2021/01/21 20:04:53
 */
@Service
@Slf4j
public class RecommendUserService {
    
    /**
     * 通过dubbo远程调用 {@link RecommendUserApi}
     */
    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;
    
    /**
     * 查询今日佳人信息 仅携带推荐的用户id
     *
     * @param userId
     *     被推荐的用户id
     *
     * @return {@link TodayBest} 仅携带推荐的用户id
     */
    public TodayBest queryTodayBest(Long userId) {
        
        RecommendUser recommendUser = null;
        try {
            // 远程调用 查询得分最高的推荐用户
            recommendUser = this.recommendUserApi.queryWithMaxScore(userId);
        } catch (Exception e) {
            log.error("远程调用失败 ~ " + recommendUserApi.getClass().getName(), e);
        }
        
        if (null == recommendUser) {
            // 未查到返回null
            return null;
        }
        // 查到后 封装对象
        TodayBest todayBest = new TodayBest();
        todayBest.setId(recommendUser.getUserId());
        
        // 计算缘分值 (去尾取整)
        double score = Math.floor(recommendUser.getScore());
        todayBest.setFateValue(Double.valueOf(score).longValue());
        
        return todayBest;
    }
    
    /**
     * 查询推荐用户id列表
     *
     * @param id
     *     被推荐的用户的id
     * @param page
     *     当前页
     * @param pagesize
     *     每页条数
     *
     * @return {@link PageInfo<RecommendUser>} 分页信息的封装
     */
    public PageInfo<RecommendUser> queryRecommendUserList(Long id, Integer page, Integer pagesize) {
        try {
            return this.recommendUserApi.queryPageInfo(id, page, pagesize);
        } catch (Exception e) {
            log.error("远程调用失败 ~ ", e);
        }
        return null;
    }
    
    /**
     * 查询两个用户的缘分值
     */
    public double queryScore(Long userId, Long id) {
        try {
            return this.recommendUserApi.queryScore(userId, id);
        } catch (Exception e) {
            log.error("远程调用失败 ~ ", e);
        }
        return 0;
    }
}
