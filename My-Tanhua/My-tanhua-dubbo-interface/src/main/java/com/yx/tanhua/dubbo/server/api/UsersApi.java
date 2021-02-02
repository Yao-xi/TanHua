package com.yx.tanhua.dubbo.server.api;

import com.yx.tanhua.dubbo.server.pojo.Users;
import com.yx.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

public interface UsersApi {
    /**
     * 保存好友
     *
     * @param users
     *     用户和好友的关系对象
     *
     * @return {@link String} 保存成功记录的主键id
     */
    String saveUsers(Users users);
    
    /**
     * 根据用户id查询好友列表
     * 查询tanhua_users集合
     *
     * @param userId
     *     用户id
     *
     * @return users集合
     */
    List<Users> queryAllUsersList(Long userId);
    
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
    PageInfo<Users> queryUsersList(Long userId, Integer page, Integer pageSize);
}
