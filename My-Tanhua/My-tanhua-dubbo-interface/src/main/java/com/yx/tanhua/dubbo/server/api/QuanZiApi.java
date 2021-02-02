package com.yx.tanhua.dubbo.server.api;

import com.yx.tanhua.dubbo.server.pojo.Comment;
import com.yx.tanhua.dubbo.server.pojo.Publish;
import com.yx.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

public interface QuanZiApi {
    
    /**
     * 发布动态
     *
     * @param publish
     *     封装数据的publish对象
     *
     * @return PublishId
     */
    String savePublish(Publish publish);
    
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
     * @return {@link PageInfo<Publish>} 动态数据的分页信息
     */
    PageInfo<Publish> queryPublishList(Long userId, Integer page, Integer pageSize);
    
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
    boolean saveLikeComment(Long userId, String publishId);
    
    /**
     * 取消点赞、喜欢等
     *
     * @param userId
     *     用户id
     * @param publishId
     *     发布id
     * @param commentType
     *     动作类型(点赞 喜欢)
     *
     * @return 是否取消点赞成功
     */
    boolean removeComment(Long userId, String publishId, Integer commentType);
    
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
    boolean saveLoveComment(Long userId, String publishId);
    
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
    boolean saveComment(Long userId, String publishId, Integer type, String content);
    
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
     * @return {@link Long}
     */
    Long queryCommentCount(String publishId, Integer type);
    
    /**
     * 根据id查询单条动态
     *
     * @param id
     *     发布id
     *
     * @return {@link Publish} 发布对象
     */
    Publish queryPublishById(String id);
    
    
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
    PageInfo<Comment> queryCommentList(String publishId, Integer page, Integer pageSize);
    
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
    PageInfo<Comment> queryCommentListByUser(Long userId, Integer type, Integer page, Integer pageSize);
    
    /**
     * 根据pid批量查询数据
     *
     * @param pids pid
     *
     * @return {@link List<Publish>}
     */
    List<Publish> queryPublishByPids(List<Long> pids);
}