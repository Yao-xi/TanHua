package com.yx.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 相册表 用于存储自己发布的数据
 * <p>
 * 每一个用户一张表进行存储
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_album")
public class Album implements java.io.Serializable {
    
    private static final long serialVersionUID = 432183095092216817L;
    
    /**
     * 主键id
     */
    private ObjectId id;
    
    /**
     * 发布id
     */
    private ObjectId publishId;
    /**
     * 发布时间
     */
    private Long created;
    
}