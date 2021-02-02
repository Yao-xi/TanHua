package com.yx.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.yx.tanhua.dubbo.server.pojo.Users;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service(version = "1.0.0")
public class UsersApiImpl implements UsersApi {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * 保存好友
     *
     * @param users
     *     用户和好友的关系对象
     *
     * @return {@link String} 保存成功记录的主键id
     */
    @Override
    public String saveUsers(Users users) {
        // 校验
        if (users.getUserId() == null || users.getFriendId() == null) {
            return null;
        }
        
        // 构造查询条件 查询是否已经存在好友关系
        Query query = Query.query(
            Criteria.where("userId").is(users.getUserId())
                .and("friendId").is(users.getFriendId()));
        Users oldUsers = this.mongoTemplate.findOne(query, Users.class);
        if (oldUsers != null) {
            // 该好友的关系已经存在
            return null;
        }
        
        // 补全users对象
        users.setId(ObjectId.get());
        users.setDate(System.currentTimeMillis());
        // 将数据写入到MongoDB中
        this.mongoTemplate.save(users);
        
        // 返回记录的主键id
        return users.getId().toHexString();
    }
    
    /**
     * 根据用户id查询好友列表
     * 查询tanhua_users集合
     *
     * @param userId
     *     用户id
     *
     * @return users集合
     */
    @Override
    public List<Users> queryAllUsersList(Long userId) {
        // 构造查询条件 返回查询结果
        Query query = Query.query(Criteria.where("userId").is(userId));
        return this.mongoTemplate.find(query, Users.class);
    }
    
    /**
     * 根据用户id查询好友列表(分页查询)
     * 查询tanhua_users集合
     *
     * @param userId
     *     用户id
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link PageInfo<Users>}
     */
    @Override
    public PageInfo<Users> queryUsersList(Long userId, Integer page, Integer pageSize) {
        // 构造分页查询条件
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize,
                                                 Sort.by(Sort.Order.desc("created")));
        Query query = Query.query(Criteria.where("userId").is(userId)).with(pageRequest);
        // 查询Mongodb
        List<Users> usersList = this.mongoTemplate.find(query, Users.class);
        // 封装分页数据并返回结果
        PageInfo<Users> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setRecords(usersList);
        pageInfo.setTotal(0); //不提供总数
        return pageInfo;
    }
}