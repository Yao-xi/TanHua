package com.yx.tanhua.dubbo.server.api;

import com.yx.tanhua.dubbo.server.pojo.RecommendUser;
import com.yx.tanhua.dubbo.server.vo.PageInfo;

/**
 * 推荐用户接口
 *
 * @author Yaoxi
 * @date 2021/01/21 19:42:19
 */
public interface RecommendUserApi {
    /**
     * 查询一位得分最高的推荐用户
     *
     * @param userId
     *     用户Id
     *
     * @return {@link RecommendUser} 推荐用户
     */
    RecommendUser queryWithMaxScore(Long userId);
    
    /**
     * 查询分页信息
     * 按照得分倒序
     *
     * @param userId
     *     用户Id
     * @param pageNum
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link PageInfo<RecommendUser>} 分页的推荐用户信息
     */
    PageInfo<RecommendUser> queryPageInfo(Long userId, Integer pageNum, Integer pageSize);
    
    /**
     * 查询推荐好友的缘分值
     *
     * @param userId
     *     推荐用户id
     * @param toUserId
     *     用户id
     *
     * @return 缘分值
     */
    double queryScore(Long userId, Long toUserId);
}
