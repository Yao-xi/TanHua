package com.yx.tanhua.dubbo.server.api;

import com.yx.tanhua.dubbo.server.pojo.UserLike;
import com.yx.tanhua.dubbo.server.vo.PageInfo;

public interface UserLikeApi {
    
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
    String saveUserLike(Long userId, Long likeUserId);
    
    
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
    Boolean isMutualLike(Long userId, Long likeUserId);
    
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
    Boolean deleteUserLike(Long userId, Long likeUserId);
    
    
    /**
     * 相互喜欢的数量
     *
     * @param userId
     *     用户id
     *
     * @return {@link Long}
     */
    Long queryEachLikeCount(Long userId);
    
    /**
     * 喜欢数
     *
     * @param userId
     *     用户id
     *
     * @return {@link Long}
     */
    Long queryLikeCount(Long userId);
    
    /**
     * 粉丝数
     *
     * @param userId
     *     用户id
     *
     * @return {@link Long}
     */
    Long queryFanCount(Long userId);
    
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
     * @return {@link PageInfo<UserLike>}
     */
    PageInfo<UserLike> queryEachLikeList(Long userId, Integer page, Integer pageSize);
    
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
    PageInfo<UserLike> queryLikeList(Long userId, Integer page, Integer pageSize);
    
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
    PageInfo<UserLike> queryFanList(Long userId, Integer page, Integer pageSize);
}
