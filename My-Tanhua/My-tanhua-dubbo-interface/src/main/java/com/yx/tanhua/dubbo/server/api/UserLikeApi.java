package com.yx.tanhua.dubbo.server.api;

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
}
