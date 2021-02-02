package com.yx.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.common.pojo.UserInfo;
import com.yx.tanhua.dubbo.server.api.QuanZiApi;
import com.yx.tanhua.dubbo.server.pojo.Comment;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import com.yx.tanhua.server.utils.UserThreadLocal;
import com.yx.tanhua.server.vo.Comments;
import com.yx.tanhua.server.vo.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentsService {
    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 查询动态的评论列表
     *
     * @param publishId
     *     动态的id
     * @param page
     *     当前页码
     * @param pagesize
     *     每页条数
     *
     * @return {@link PageResult} 分页信息
     */
    public PageResult queryCommentsList(String publishId, Integer page, Integer pagesize) {
        // 获取当前user对象
        User user = UserThreadLocal.get();
        // 构造默认分页数据
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pagesize);
        pageResult.setPages(0);
        pageResult.setCounts(0);
        
        // 远程调用 查询评论信息
        PageInfo<Comment> pageInfo = this.quanZiApi.queryCommentList(publishId, page, pagesize);
        // 获取评论数据列表
        List<Comment> records = pageInfo.getRecords();
        if (records.isEmpty()) {
            // 获取失败 返回默认分页数据
            return pageResult;
        }
        // 获取评论的userId列表
        List<Long> userIds = new ArrayList<>();
        for (Comment comment : records) {
            if (!userIds.contains(comment.getUserId())) {
                userIds.add(comment.getUserId());
            }
        }
        
        // 构造查询条件 通过userId查询用户信息
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        List<UserInfo> userInfos = this.userInfoService.queryUserInfoList(queryWrapper);
        // ------ 优化双层for遍历 O(n^2) -> O(n) Map集合操作接近O(1) ------
        // 把用户信息转换成Map集合
        Map<Long, UserInfo> userInfoMap = new HashMap<>();
        for (UserInfo userInfo : userInfos) {
            userInfoMap.put(userInfo.getUserId(), userInfo);
        }
        
        List<Comments> result = new ArrayList<>();
        for (Comment record : records) {
            // 获取该评论的用户信息
            UserInfo userInfo = userInfoMap.get(record.getUserId());
            // 构造并填充comments对象
            Comments comments = new Comments();
            comments.setContent(record.getContent());
            comments.setCreateDate(new DateTime(record.getCreated()).toString("yyyy年MM月dd日 HH:mm"));
            comments.setId(record.getId().toHexString());
            comments.setAvatar(userInfo.getLogo());
            comments.setNickname(userInfo.getNickName());
            
            // ------ 评论点赞数据 ------
            // 从缓存中获取点赞数
            String key = "QUANZI_COMMENT_LIKE_" + comments.getId();
            String value = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotEmpty(value)) {
                //noinspection ConstantConditions
                comments.setLikeCount(Integer.valueOf(value));
            } else {
                comments.setLikeCount(0);
            }
            // 从缓存中查询是否点赞
            String userKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" + comments.getId();
            //noinspection ConstantConditions
            comments.setHasLiked(this.redisTemplate.hasKey(userKey) ? 1 : 0); // 1是 0否
            
            // 添加到列表中
            result.add(comments);
        }
        
        
        /*
        List<Comments> result = new ArrayList<>();
        for (Comment record : records) {
            Comments comments = new Comments();
            comments.setContent(record.getContent());
            comments.setCreateDate(new DateTime(record.getCreated()).toString("yyyy年MM月dd日 HH:mm"));
            comments.setId(record.getId().toHexString());
            
            for (UserInfo userInfo : userInfos) {
                if (record.getUserId().longValue() == userInfo.getUserId().longValue()) {
                    comments.setAvatar(userInfo.getLogo());
                    comments.setNickname(userInfo.getNickName());
                    break;
                }
            }
            
            String key = "QUANZI_COMMENT_LIKE_" + comments.getId();
            String value = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotEmpty(value)) {
                //noinspection ConstantConditions
                comments.setLikeCount(Integer.valueOf(value)); //点赞数
            } else {
                comments.setLikeCount(0);
            }
            
            String userKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" + comments.getId();
            //noinspection ConstantConditions
            comments.setHasLiked(this.redisTemplate.hasKey(userKey) ? 1 : 0); //是否点赞（1是，0否）
            
            result.add(comments);
        }*/
        // 封装数据 并返回结果
        pageResult.setItems(result);
        return pageResult;
    }
    
    /**
     * 保存评论
     *
     * @param publishId
     *     动态的id
     * @param content
     *     评论内容
     *
     * @return {@link Boolean} 是否成功
     */
    public Boolean saveComments(String publishId, String content) {
        // 获取当前的user信息
        User user = UserThreadLocal.get();
        // 远程调用 保存评论
        boolean success = this.quanZiApi.saveComment(user.getId(), publishId, 2, content);
        if (!success) {
            // 保存失败
            return false;
        }
        
        // 评论数
        Long commentCount = 0L;
    
        // 保存评论数到redis
        String key = "QUANZI_COMMENT_" + publishId;
        if (Boolean.FALSE.equals(this.redisTemplate.hasKey(key))) {
            // 缓存中没有值的话 远程调用 查询评论数
            commentCount = this.quanZiApi.queryCommentCount(publishId, 2);
            // 写入缓存
            this.redisTemplate.opsForValue().set(key, String.valueOf(commentCount));
        }
        
        // 缓存数据自增
        this.redisTemplate.opsForValue().increment(key);
        
        return true;
    }
}
