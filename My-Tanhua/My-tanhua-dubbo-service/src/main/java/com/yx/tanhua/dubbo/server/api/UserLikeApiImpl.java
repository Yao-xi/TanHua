package com.yx.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.mongodb.client.result.DeleteResult;
import com.yx.tanhua.dubbo.server.pojo.UserLike;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Service(version = "1.0.0")
public class UserLikeApiImpl implements UserLikeApi {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * 保存喜欢
     *
     * @param userId
     *     自己的id
     * @param likeUserId
     *     对方的id
     *
     * @return 记录的id
     */
    @Override
    public String saveUserLike(Long userId, Long likeUserId) {
        // 查询mongodb中是否已存在喜欢记录
        Query query = Query.query(Criteria.where("userId").is(userId)
                                      .and("likeUserId").is(likeUserId));
        if (this.mongoTemplate.count(query, UserLike.class) > 0) {
            return null;
        }
        // 封装UserLike对象
        UserLike userLike = new UserLike();
        userLike.setId(ObjectId.get());
        userLike.setCreated(System.currentTimeMillis());
        userLike.setUserId(userId);
        userLike.setLikeUserId(likeUserId);
        // 保存到mongodb
        this.mongoTemplate.save(userLike);
        return userLike.getId().toHexString();
    }
    
    /**
     * 查询是否相互喜欢
     *
     * @param userId
     *     自己的id
     * @param likeUserId
     *     对方的id
     *
     * @return {@link Boolean} 是否相互喜欢
     */
    @Override
    public Boolean isMutualLike(Long userId, Long likeUserId) {
        Criteria criteria1 = Criteria.where("userId").is(userId)
            .and("likeUserId").is(likeUserId);
        Criteria criteria2 = Criteria.where("userId").is(likeUserId)
            .and("likeUserId").is(userId);
        Criteria criteria = new Criteria().orOperator(criteria1, criteria2);
        return this.mongoTemplate.count(Query.query(criteria), UserLike.class) == 2;
    }
    
    /**
     * 删除用户喜欢
     *
     * @param userId
     *     自己的id
     * @param likeUserId
     *     对方的id
     *
     * @return {@link Boolean}
     */
    @Override
    public Boolean deleteUserLike(Long userId, Long likeUserId) {
        Query query = Query.query(Criteria.where("userId").is(userId)
                                      .and("likeUserId").is(likeUserId));
        DeleteResult deleteResult = this.mongoTemplate.remove(query, UserLike.class);
        return deleteResult.getDeletedCount() == 1;
    }
}