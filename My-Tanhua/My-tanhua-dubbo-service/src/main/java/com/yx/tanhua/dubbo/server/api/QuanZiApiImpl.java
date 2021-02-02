package com.yx.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.mongodb.client.result.DeleteResult;
import com.yx.tanhua.dubbo.server.pojo.*;
import com.yx.tanhua.dubbo.server.service.IdService;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class QuanZiApiImpl implements QuanZiApi {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdService idService;
    
    /**
     * 发布动态
     *
     * @param publish
     *     封装数据的publish对象
     *
     * @return PublishId
     */
    @Override
    public String savePublish(Publish publish) {
        
        // 校验参数
        if (publish.getUserId() == null) {
            return null;
        }
        
        try {
            // 设置创建时间
            publish.setCreated(System.currentTimeMillis());
            // 设置主键id
            publish.setId(ObjectId.get());
            // 设置pid
            publish.setPid(this.idService.createId("publish", publish.getId().toHexString()));
            
            // 保存发布 插入数据
            this.mongoTemplate.save(publish);
            log.debug("保存发布成功 publish=" + publish);
            
            // 构造相册对象
            Album album = new Album();
            album.setPublishId(publish.getId());
            album.setCreated(System.currentTimeMillis());
            album.setId(ObjectId.get());
            
            // 插入数据 指定表名: "quanzi_album_" + publish.getUserId()
            this.mongoTemplate.save(album, "quanzi_album_" + publish.getUserId());
            log.debug("自己的动态表插入数据成功 album=" + album);
            
            // 查找好友信息
            Criteria criteria = Criteria.where("userId").is(publish.getUserId());
            List<Users> users = this.mongoTemplate.find(Query.query(criteria), Users.class);
            // 遍历所有的好友
            for (Users user : users) {
                // 构造时间线对象
                TimeLine timeLine = new TimeLine();
                timeLine.setId(ObjectId.get());
                timeLine.setPublishId(publish.getId());
                timeLine.setUserId(user.getUserId());
                timeLine.setDate(System.currentTimeMillis());
                // 写入到好友的时间线表中
                this.mongoTemplate.save(
                    timeLine, "quanzi_time_line_" + user.getFriendId());
                log.debug("写入到好友的时间线表中成功 timeLine=" + timeLine);
            }
            
            return publish.getId().toHexString();
        } catch (Exception e) {
            e.printStackTrace();
            // todo 出错的事务回滚
            //  MongoDB非集群不支持事务 暂不进行实现
            //  MongoDB事务可以通过AOP实现 类似Mysql事务
        }
        
        return null;
    }
    
    /**
     * 查询好友动态
     *
     * @param userId
     *     用户id 未指定查推荐数据
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link PageInfo <Publish>} 动态数据的分页信息
     */
    @Override
    public PageInfo<Publish> queryPublishList(Long userId, Integer page, Integer pageSize) {
        // 构造分页查询条件 根据创建时间倒序排序
        Query queryTimeLine = new Query().with(
            PageRequest.of(page - 1, pageSize, Sort.by(Sort.Order.desc("date"))));
        
        String collectionName = "quanzi_time_line_" + userId;
        // 如果没有传递用户id 则为查询推荐用户信息
        if (userId == null) {
            collectionName = "quanzi_time_line_recommend";
        }
        
        // 查询时间线表 该用户可以看到的好友动态会在时间线表中存储一份publishId
        List<TimeLine> timeLineList = this.mongoTemplate.find(
            queryTimeLine, TimeLine.class, collectionName);
        
        // 获取已发布的动态的publishId列表
        List<ObjectId> publishIds = new ArrayList<>();
        for (TimeLine timeLine : timeLineList) {
            publishIds.add(timeLine.getPublishId());
        }
        
        // 查询发布信息 根据publishId在Publish表中查询完整信息 并以创建日期为倒序排序
        Query queryPublish = Query.query(Criteria.where("id").in(publishIds))
            .with(Sort.by(Sort.Order.desc("created")));
        List<Publish> publishList = this.mongoTemplate.find(queryPublish, Publish.class);
        
        // 封装分页数据
        PageInfo<Publish> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setRecords(publishList);
        pageInfo.setTotal(0); //不提供总数
        
        return pageInfo;
    }
    
    /**
     * 点赞
     *
     * @param userId
     *     用户id
     * @param publishId
     *     发布id
     *
     * @return 是否点赞成功
     */
    @Override
    public boolean saveLikeComment(Long userId, String publishId) {
        // 构造查询条件 点赞-commentType=1
        Query query = Query.query(
            Criteria.where("publishId").is(new ObjectId(publishId))
                .and("userId").is(userId)
                .and("commentType").is(1));
        // 查询是否已经点赞
        long count = this.mongoTemplate.count(query, Comment.class);
        if (count > 0) {
            // 已经点过赞 则本次点赞失败
            return false;
        }
        // 保存点赞记录
        return this.saveComment(userId, publishId, 1, null);
    }
    
    /**
     * 取消点赞、喜欢等
     *
     * @param userId
     *     用户id
     * @param publishId
     *     发布id
     * @param commentType
     *     评论类型
     *     <p>
     *     1-点赞
     *     2-评论
     *     3-喜欢
     *
     * @return 是否取消成功
     */
    @Override
    public boolean removeComment(Long userId, String publishId, Integer commentType) {
        // 构造查询条件
        Query query = Query.query(
            Criteria.where("publishId").is(new ObjectId(publishId))
                .and("userId").is(userId)
                .and("commentType").is(commentType));
        // 从Mongodb中移除数据
        DeleteResult remove = this.mongoTemplate.remove(query, Comment.class);
        // 删除记录数 > 0 即删除成功
        return remove.getDeletedCount() > 0;
    }
    
    /**
     * 喜欢
     *
     * @param userId
     *     用户id
     * @param publishId
     *     发布id
     *
     * @return boolean 是否喜欢成功
     */
    @Override
    public boolean saveLoveComment(Long userId, String publishId) {
        // 构造查询条件
        Query query = Query.query(
            Criteria.where("publishId").is(new ObjectId(publishId))
                .and("userId").is(userId)
                .and("commentType").is(3));
        // 判断是否已经点过喜欢
        long count = this.mongoTemplate.count(query, Comment.class);
        if (count > 0) {
            return false;
        }
        // 保存喜欢记录
        return this.saveComment(userId, publishId, 3, null);
    }
    
    /**
     * 保存评论
     *
     * @param userId
     *     用户id
     * @param publishId
     *     发布id
     * @param type
     *     评论类型
     *     <p>
     *     1-点赞
     *     2-评论
     *     3-喜欢
     * @param content
     *     评论内容 只有类型为评论才会有内容
     *
     * @return boolean 是否保存成功
     */
    @Override
    public boolean saveComment(Long userId, String publishId, Integer type, String content) {
        try {
            // 构造comment对象
            Comment comment = new Comment();
            comment.setId(ObjectId.get());
            comment.setUserId(userId);
            comment.setContent(content);
            comment.setPublishId(new ObjectId(publishId));
            comment.setCommentType(type);
            comment.setCreated(System.currentTimeMillis());
    
            // 设置发布人的id
            Publish publish = this.mongoTemplate.findById(comment.getPublishId(), Publish.class);
            if (publish != null) {
                comment.setPublishUserId(publish.getUserId());
            } else {
                Video video = this.mongoTemplate.findById(comment.getPublishId(), Video.class);
                if (video != null) {
                    comment.setPublishUserId(video.getUserId());
                }
            }
            
            // 保存到Mongodb
            this.mongoTemplate.save(comment);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 出现异常 保存失败
        return false;
    }
    
    /**
     * 查询评论数
     *
     * @param publishId
     *     发布id
     * @param type
     *     评论类型
     *     <p>
     *     1-点赞
     *     2-评论
     *     3-喜欢
     *
     * @return {@link Long} 评论数
     */
    @Override
    public Long queryCommentCount(String publishId, Integer type) {
        // 构造查询条件
        // ObjectId的值 如果是string则需要包装成对象
        Query query = Query.query(
            Criteria.where("publishId").is(new ObjectId(publishId))
                .and("commentType").is(type));
        // 返回查询结果
        long count = this.mongoTemplate.count(query, Comment.class);
        log.debug("查询评论数 type=" + type + " publishId=" + publishId + " count=" + count);
        return count;
    }
    
    /**
     * 根据id查询单条动态
     *
     * @param id
     *     发布id
     *
     * @return {@link Publish} 发布对象
     */
    @Override
    public Publish queryPublishById(String id) {
        // 查询Mongodb
        return this.mongoTemplate.findById(new ObjectId(id), Publish.class);
    }
    
    /**
     * 查询评论
     *
     * @param publishId
     *     被评论的动态id
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link PageInfo<Comment>} 评论的分页信息
     */
    @Override
    public PageInfo<Comment> queryCommentList(String publishId, Integer page, Integer pageSize) {
        // 构造分页查询条件 按创建时间倒序排序
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize,
                                                 Sort.by(Sort.Order.desc("created")));
        Query query = new Query(Criteria.where("publishId").is(new ObjectId(publishId))
                                    .and("commentType").is(2)).with(pageRequest);
        
        return getCommentPageInfo(page, pageSize, query);
    }
    
    /**
     * 重复代码抽取
     * <p>
     * 连接Mongodb查询分页信息
     *
     * @return {@link PageInfo<Comment>}
     */
    private PageInfo<Comment> getCommentPageInfo(Integer page, Integer pageSize, Query query) {
        // 查询时间线表 获取当前评论的
        List<Comment> timeLineList = this.mongoTemplate.find(query, Comment.class);
        // 封装分页数据
        PageInfo<Comment> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setRecords(timeLineList);
        pageInfo.setTotal(0); //不提供总数
        return pageInfo;
    }
    
    /**
     * 查询用户的评论数据
     *
     * @param type
     *     评论类型
     *     <p>
     *     1-点赞
     *     2-评论
     *     3-喜欢
     *
     * @return {@link PageInfo<Comment>}
     */
    @Override
    public PageInfo<Comment> queryCommentListByUser(Long userId, Integer type, Integer page, Integer pageSize) {
        // 构造查询条件
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize,
                                                 Sort.by(Sort.Order.desc("created")));
        Query query = new Query(
            Criteria.where("publishUserId").is(userId)
                .and("commentType").is(type)).with(pageRequest);
        
        return getCommentPageInfo(page, pageSize, query);
    }
    
    /**
     * 根据pid批量查询数据
     *
     * @param pids
     *     pid
     *
     * @return {@link List<Publish>}
     */
    @Override
    public List<Publish> queryPublishByPids(List<Long> pids) {
        Query query = Query.query(Criteria.where("pid").in(pids));
        return this.mongoTemplate.find(query, Publish.class);
    }
    
    
}