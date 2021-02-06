package com.yx.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.yx.tanhua.dubbo.server.pojo.RecommendUser;
import com.yx.tanhua.dubbo.server.pojo.Visitors;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service(version = "1.0.0")
public class VisitorsApiImpl implements VisitorsApi {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * 保存来访记录
     *
     * @param visitors
     *     来访信息
     *
     * @return {@link String} 主键id
     */
    @Override
    public String saveVisitor(Visitors visitors) {
        visitors.setId(ObjectId.get());
        visitors.setDate(System.currentTimeMillis());
        // 保存到mongodb
        this.mongoTemplate.save(visitors);
        
        return visitors.getId().toHexString();
    }
    
    /**
     * 按照时间倒序排序，查询最近的访客信息
     *
     * @param userId
     *     用户id
     * @param num
     *     个数
     *
     * @return {@link List<Visitors>}
     */
    @Override
    public List<Visitors> topVisitor(Long userId, Integer num) {
        Pageable pageable = PageRequest.of(0, num, Sort.by(Sort.Order.desc("date")));
        Query query = Query.query(Criteria.where("userId").is(userId)).with(pageable);
        return this.queryVisitorList(query);
    }
    
    /**
     * 按照时间倒序排序，查询最近的访客信息
     *
     * @param userId
     *     用户id
     * @param date
     *     大于该时间的
     *
     * @return {@link List<Visitors>}
     */
    @Override
    public List<Visitors> topVisitor(Long userId, Long date) {
        Query query = Query.query(Criteria
                                      .where("userId").is(userId)
                                      .and("date").gt(date));
        return this.queryVisitorList(query);
    }
    
    private List<Visitors> queryVisitorList(Query query) {
        // 查mongodb
        List<Visitors> visitors = this.mongoTemplate.find(query, Visitors.class);
        
        // 查询得分
        for (Visitors visitor : visitors) {
            Query queryRecommend =
                Query.query(Criteria.where("toUserId").is(visitor.getUserId())
                                .and("userId").is(visitor.getVisitorUserId()));
            RecommendUser recommendUser =
                mongoTemplate.findOne(queryRecommend, RecommendUser.class);
            if (recommendUser != null) {
                visitor.setScore(recommendUser.getScore());
            } else {
                // 设置默认得分
                visitor.setScore(30d);
            }
        }
        
        return visitors;
    }
}