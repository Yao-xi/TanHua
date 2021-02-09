package com.yx.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yx.tanhua.common.pojo.UserInfo;
import com.yx.tanhua.server.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoService {
    @Autowired
    private UserInfoMapper userInfoMapper;
    
    /**
     * 根据用户id查询用户信息
     * <p>
     * 为了简单处理 直接查询数据库了
     * <p>
     * 建议编写dubbo服务 进行调用
     *
     * @param userId
     *     用户id
     *
     * @return {@link UserInfo} 用户的完整信息
     */
    public UserInfo queryUserInfoByUserId(Long userId) {
        // 调用dao
        return this.userInfoMapper.selectOne(
            new QueryWrapper<UserInfo>().eq("user_id", userId));
    }
    
    /**
     * 查询用户信息列表
     *
     * @param queryWrapper
     *     查询条件
     *
     * @return 用户信息列表
     */
    public List<UserInfo> queryUserInfoList(QueryWrapper<UserInfo> queryWrapper) {
        return this.userInfoMapper.selectList(queryWrapper);
    }
    
    /**
     * 分页查询用户信息列表
     *
     * @return {@link IPage<UserInfo>}
     */
    public IPage<UserInfo> queryUserInfoList(Integer page, Integer pageSize, QueryWrapper<UserInfo> queryWrapper) {
        IPage<UserInfo> pager = new Page<>(page, pageSize);
        return this.userInfoMapper.selectPage(pager, queryWrapper);
        
    }
    
    /**
     * 根据用户ID查询用户详细信息
     *
     * @param userId
     *     用户ID
     *
     * @return 用户详细信息
     */
    public UserInfo queryById(Long userId) {
        return userInfoMapper.selectById(userId);
    }
    
    /**
     * 根据查询条件,查询用户列表
     *
     * @param queryWrapper
     *     查询条件
     *
     * @return 用户列表
     */
    public List<UserInfo> queryList(QueryWrapper<UserInfo> queryWrapper) {
        return userInfoMapper.selectList(queryWrapper);
    }
    
    /**
     * 更新用户信息
     */
    public Boolean updateUserInfoByUserId(UserInfo userInfo) {
        QueryWrapper<UserInfo> queryWrapper =
            new QueryWrapper<UserInfo>().eq("user_id", userInfo.getUserId());
        return this.userInfoMapper.update(userInfo, queryWrapper) > 0;
    }
}
