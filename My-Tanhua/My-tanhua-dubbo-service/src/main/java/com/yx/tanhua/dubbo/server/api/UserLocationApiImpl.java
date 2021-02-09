package com.yx.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Service;
import com.mongodb.client.result.UpdateResult;
import com.yx.tanhua.dubbo.server.pojo.UserLocation;
import com.yx.tanhua.dubbo.server.vo.UserLocationVo;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@Service(version = "1.0.0")
@Slf4j
public class UserLocationApiImpl implements UserLocationApi {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
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
     *
     * @throws com.mongodb.MongoWriteException
     *     Can't extract geo keys 检查传入的数据的经纬度信息是否正确
     */
    @Override
    public String updateUserLocation(Long userId, Double longitude, Double latitude, String address) {
        // 构造用户地理位置pojo对象
        UserLocation userLocation = new UserLocation();
        userLocation.setAddress(address);
        userLocation.setLocation(new GeoJsonPoint(longitude, latitude));
        userLocation.setUserId(userId);
        
        // 在mongodb中查询用户地理位置pojo对象
        Query query = Query.query(Criteria.where("userId").is(userLocation.getUserId()));
        UserLocation ul = this.mongoTemplate.findOne(query, UserLocation.class);
        if (ul == null) {
            // 未查到则新增
            userLocation.setId(ObjectId.get());
            userLocation.setCreated(System.currentTimeMillis());
            userLocation.setUpdated(userLocation.getCreated());
            userLocation.setLastUpdated(userLocation.getCreated());
            this.mongoTemplate.save(userLocation);
            
            return userLocation.getId().toHexString();
            
        } else {
            // 查到则更新
            Update update = Update
                .update("location", userLocation.getLocation())
                .set("address", userLocation.getAddress())
                .set("updated", System.currentTimeMillis())
                .set("lastUpdated", ul.getUpdated());
            UpdateResult updateResult = this.mongoTemplate.updateFirst(query, update, UserLocation.class);
            log.debug("更新了 " + updateResult.getModifiedCount()+" 条~");
        }
        
        return ul.getId().toHexString();
    }
    
    /**
     * 查询用户地理位置
     */
    @Override
    public UserLocationVo queryByUserId(Long userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));
        UserLocation userLocation = this.mongoTemplate.findOne(query, UserLocation.class);
        if (userLocation != null) {
            return UserLocationVo.format(userLocation);
        } else {
            return null;
        }
    }
    
    /**
     * 根据地理位置查询用户
     *
     * @param range
     *     范围 单位 米
     */
    @Override
    public List<UserLocationVo> queryUserFromLocation(Double longitude, Double latitude, Integer range) {
        
        // 中心点
        GeoJsonPoint geoJsonPoint = new GeoJsonPoint(longitude, latitude);
        
        // 转换为2d-sphere的距离
        Distance distance = new Distance(range / 1000.0, Metrics.KILOMETERS);
        
        // 画一个圆
        Circle circle = new Circle(geoJsonPoint, distance);
        // 查询范围内的数据
        Query query = Query.query(Criteria.where("location").withinSphere(circle));
        return UserLocationVo.formatToList(this.mongoTemplate.find(query, UserLocation.class));
    }
    
}