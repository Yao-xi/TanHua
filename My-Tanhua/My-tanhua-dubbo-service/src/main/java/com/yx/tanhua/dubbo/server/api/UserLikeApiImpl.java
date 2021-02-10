package com.yx.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.mongodb.client.result.DeleteResult;
import com.yx.tanhua.dubbo.server.pojo.UserLike;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

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
    
    /**
     * 相互喜欢的数量
     *
     * @param userId
     *     用户id
     *
     * @return {@link Long}
     */
    @Override
    public Long queryEachLikeCount(Long userId) {
        // 我喜欢的列表
        List<Long> likeUserIdList = queryLikeUserIdList(userId);
        
        // 查我喜欢的用户中喜欢我的个数
        Query query = Query.query(Criteria.where("userId").in(likeUserIdList)
                                      .and("likeUserId").is(userId));
        return this.mongoTemplate.count(query, UserLike.class);
    }
    
    /**
     * 喜欢数
     *
     * @param userId
     *     用户id
     *
     * @return {@link Long}
     */
    @Override
    public Long queryLikeCount(Long userId) {
        return this.mongoTemplate.count(Query.query(Criteria.where("userId").is(userId)), UserLike.class);
    }
    
    /**
     * 粉丝数
     *
     * @param userId
     *     用户id
     *
     * @return {@link Long}
     */
    @Override
    public Long queryFanCount(Long userId) {
        return this.mongoTemplate.count(Query.query(Criteria.where("likeUserId").is(userId)), UserLike.class);
    }
    
    /**
     * 查询相互喜欢列表
     *
     * @param userId
     *     用户id
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link PageInfo <UserLike>}
     */
    @Override
    public PageInfo<UserLike> queryEachLikeList(Long userId, Integer page, Integer pageSize) {
        // 我喜欢的列表
        List<Long> likeUserIdList = queryLikeUserIdList(userId);
        
        Query query = Query.query(Criteria.where("userId").in(likeUserIdList)
                                      .and("likeUserId").is(userId));
        return this.queryList(query, page, pageSize);
        
    }
    
    /**
     * 查询我喜欢的列表
     *
     * @param userId
     *     用户id
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link PageInfo<UserLike>}
     */
    @Override
    public PageInfo<UserLike> queryLikeList(Long userId, Integer page, Integer pageSize) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        return this.queryList(query, page, pageSize);
    }
    
    /**
     * 查询粉丝列表
     *
     * @param userId
     *     用户id
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link PageInfo<UserLike>}
     */
    @Override
    public PageInfo<UserLike> queryFanList(Long userId, Integer page, Integer pageSize) {
        Query query = Query.query(Criteria.where("likeUserId").is(userId));
        return this.queryList(query, page, pageSize);
    }
    
    private PageInfo<UserLike> queryList(Query query, Integer page, Integer pageSize) {
        // 分页查询
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("created")));
        query.with(pageRequest);
        List<UserLike> userLikeList = this.mongoTemplate.find(query, UserLike.class);
        
        PageInfo<UserLike> pageInfo = new PageInfo<>();
        pageInfo.setRecords(userLikeList);
        pageInfo.setTotal(0);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPageNum(page);
        return pageInfo;
    }
    
    /**
     * 获取到所有我喜欢的列表的用户id
     *
     * @param userId
     *     我的id
     *
     * @return {@link List<Long>}
     */
    private List<Long> queryLikeUserIdList(Long userId) {
        // 我喜欢的列表
        List<UserLike> userLikeList =
            this.mongoTemplate.find(Query.query(Criteria.where("userId").is(userId)), UserLike.class);
        
        // 获取到所有我喜欢的列表的用户id
        List<Long> likeUserIdList = new ArrayList<>();
        for (UserLike userLike : userLikeList) {
            likeUserIdList.add(userLike.getLikeUserId());
        }
        return likeUserIdList;
    }
}