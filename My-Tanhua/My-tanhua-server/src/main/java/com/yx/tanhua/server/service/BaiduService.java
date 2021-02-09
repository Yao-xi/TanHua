package com.yx.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.dubbo.server.api.UserLocationApi;
import com.yx.tanhua.server.utils.UserThreadLocal;
import org.springframework.stereotype.Service;

@Service
public class BaiduService {
    
    @Reference(version = "1.0.0")
    private UserLocationApi userLocationApi;
    
    /**
     * 更新位置
     *
     * @return {@link Boolean}
     */
    public Boolean updateLocation(Double longitude, Double latitude, String address) {
        try {
            // 获取当前用户信息
            User user = UserThreadLocal.get();
            // 远程调用
            this.userLocationApi.updateUserLocation(user.getId(), longitude, latitude, address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
}