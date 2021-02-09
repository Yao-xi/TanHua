package com.yx.tanhua.dubbo.server.api;

import com.yx.tanhua.dubbo.server.vo.UserLocationVo;

import java.util.List;

public interface UserLocationApi {
    
    /**
     * 更新用户地理位置
     *
     * @param userId
     *     用户id
     * @param longitude
     *     经度
     * @param latitude
     *     纬度
     * @param address
     *     地址
     *
     * @return {@link String} 用户地理位置记录的主键id
     */
    String updateUserLocation(Long userId, Double longitude, Double latitude, String address);
    
    /**
     * 查询用户地理位置
     */
    UserLocationVo queryByUserId(Long userId);
    
    /**
     * 根据地理位置查询用户
     *
     * @param range
     *     范围 单位 米
     */
    List<UserLocationVo> queryUserFromLocation(Double longitude, Double latitude, Integer range);
}