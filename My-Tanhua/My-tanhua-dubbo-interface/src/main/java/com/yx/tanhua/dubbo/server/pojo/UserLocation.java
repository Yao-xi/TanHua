package com.yx.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 地理位置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_location")
// 建立复合索引 location_index
@CompoundIndex(name = "location_index", def = "{'location': '2dsphere'}")
public class UserLocation implements java.io.Serializable {
    
    private static final long serialVersionUID = 4508868382007529970L;
    
    /**
     * 主键id
     */
    @Id
    private ObjectId id;
    /**
     * 用户id 建立索引
     */
    @Indexed
    private Long userId;
    /**
     * x:经度 y:纬度
     */
    private GeoJsonPoint location;
    /**
     * 位置描述
     */
    private String address;
    /**
     * 创建时间
     */
    private Long created;
    /**
     * 更新时间
     */
    private Long updated;
    /**
     * 上次更新时间
     */
    private Long lastUpdated;
    
}