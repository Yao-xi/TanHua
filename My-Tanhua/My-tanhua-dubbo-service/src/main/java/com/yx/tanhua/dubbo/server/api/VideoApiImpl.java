package com.yx.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.mongodb.client.result.DeleteResult;
import com.yx.tanhua.dubbo.server.pojo.FollowUser;
import com.yx.tanhua.dubbo.server.pojo.Video;
import com.yx.tanhua.dubbo.server.service.IdService;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class VideoApiImpl implements VideoApi {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IdService idService;
    
    /**
     * 保存小视频
     *
     * @param video
     *     小视频信息
     *
     * @return 是否保存成功
     */
    @Override
    public String saveVideo(Video video) {
        // 校验是否包含userId
        if (video.getUserId() == null) {
            return null;
        }
        // 填充主键id和创建时间
        video.setId(ObjectId.get());
        video.setCreated(System.currentTimeMillis());
        
        // 生成自增长vid
        video.setVid(this.idService.createId("video", video.getId().toHexString()));
        
        // 存入mongodb
        this.mongoTemplate.save(video);
        return video.getId().toHexString();
    }
    
    /**
     * 分页查询小视频列表，按照时间倒序排序
     *
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link PageInfo<Video>}
     */
    @Override
    public PageInfo<Video> queryVideoList(Integer page, Integer pageSize) {
        // 构造分页查询条件 按创建时间倒序
        Pageable pageable = PageRequest.of(page - 1, pageSize,
                                           Sort.by(Sort.Order.desc("created")));
        Query query = new Query().with(pageable);
        // 查询Mongodb 获取小视频列表
        List<Video> videos = this.mongoTemplate.find(query, Video.class);
        // 封装分页数据
        PageInfo<Video> pageInfo = new PageInfo<>();
        pageInfo.setRecords(videos);
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(0); //不提供总数
        return pageInfo;
    }
    
    /**
     * 关注用户
     *
     * @param userId
     *     用户id
     * @param followUserId
     *     关注的用户id
     *
     * @return 是否关注成功
     */
    @Override
    public Boolean followUser(Long userId, Long followUserId) {
        try {
            // 封装followUser对象
            FollowUser followUser = new FollowUser();
            followUser.setId(ObjectId.get());
            followUser.setUserId(userId);
            followUser.setFollowUserId(followUserId);
            followUser.setCreated(System.currentTimeMillis());
            // 存入Mongodb
            this.mongoTemplate.save(followUser);
            return true;
        } catch (Exception e) {
            log.error("关注用户失败 ~ userId=" + userId + " followUserId=" + followUserId, e);
        }
        return false;
    }
    
    /**
     * 取消关注用户
     *
     * @param userId
     *     用户id
     * @param followUserId
     *     关注的用户id
     *
     * @return 是否取消关注成功
     */
    @Override
    public Boolean disFollowUser(Long userId, Long followUserId) {
        // 构造查询条件
        Query query = Query.query(Criteria.where("userId").is(userId)
                                      .and("followUserId").is(followUserId));
        // 从Mongodb中删除数据
        DeleteResult deleteResult = this.mongoTemplate.remove(query, FollowUser.class);
        return deleteResult.getDeletedCount() > 0;
    }
    
    /**
     * 根据id查询视频信息
     */
    @Override
    public Video queryVideoById(String vid) {
        return mongoTemplate.findById(new ObjectId(vid), Video.class);
    }
    
    /**
     * 根据vid批量查询数据
     */
    @Override
    public List<Video> queryVideoListByVids(List<Long> vids) {
        Query query = Query.query(Criteria.where("vid").in(vids));
        return this.mongoTemplate.find(query, Video.class);
    }
}
