package com.yx.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.yx.tanhua.dubbo.server.pojo.RecommendUser;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

/**
 * {@link RecommendUserApi}的实现类
 * <p>
 * {@link Service} 声明这是一个dubbo服务
 * <p>
 * {@link Service#version()} 指定版本号
 *
 * @author Yaoxi
 * @date 2021/01/21 18:44:23
 */
@Service(version = "1.0.0")
@Slf4j
public class RecommendUserApiImpl implements RecommendUserApi {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * 查询一位得分最高的推荐用户
     *
     * @param userId
     *     用户Id
     *
     * @return {@link RecommendUser} 推荐用户
     */
    @Override
    public RecommendUser queryWithMaxScore(Long userId) {
        // 查询用户 按照得分倒序排序 取第一个即为得分最高的
        Query query = Query.query(Criteria.where("toUserId").is(userId))
            .with(Sort.by(Sort.Order.desc("score"))).limit(1);
        return this.mongoTemplate.findOne(query, RecommendUser.class);
    }
    
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
    @Override
    public PageInfo<RecommendUser> queryPageInfo(Long userId, Integer pageNum, Integer pageSize) {
        // 分页并且排序参数
        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize,
                                                 Sort.by(Sort.Order.desc("score")));
        // 查询参数
        Query query = Query.query(Criteria.where("toUserId").is(userId)).with(pageRequest);
        List<RecommendUser> recommendUserList = this.mongoTemplate.find(query, RecommendUser.class);
        // 封装成PageInfo对象 暂时不提供数据总数
        return new PageInfo<>(0, pageNum, pageSize, recommendUserList);
    }
    
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
    @Override
    public double queryScore(Long userId, Long toUserId) {
        Query query = Query.query(Criteria.where("toUserId").is(toUserId)
                                      .and("userId").is(userId));
        RecommendUser recommendUser = this.mongoTemplate.findOne(query, RecommendUser.class);
        if (recommendUser == null) {
            return 0;
        }
        return recommendUser.getScore();
    }
}
