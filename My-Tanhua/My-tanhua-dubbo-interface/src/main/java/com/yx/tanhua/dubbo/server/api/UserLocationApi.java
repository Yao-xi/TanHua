package com.yx.tanhua.dubbo.server.api;

public interface UserLocationApi {
    
    /**
     * 更新用户地理位置
     *
     * @param userId 用户id
     * @param longitude 经度
     * @param latitude 纬度
     * @param address 地址
     *
     * @return {@link String} 用户地理位置记录的主键id
     */
    String updateUserLocation(Long userId, Double longitude, Double latitude, String address);
    
}