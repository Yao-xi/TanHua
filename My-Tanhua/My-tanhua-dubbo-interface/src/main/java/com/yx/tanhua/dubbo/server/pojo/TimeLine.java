package com.yx.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 时间线表 用于存储发布(或推荐)的数据
 * <p>
 * 每一个用户一张表进行存储
 * <p>
 * 好友发布动态时 同步更新时间线表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_time_line")
public class TimeLine implements java.io.Serializable {
    private static final long serialVersionUID = 9096178416317502524L;
    /**
     * 主键id
     */
    private ObjectId id;
    
    /**
     * 好友id
     */
    private Long userId;
    /**
     * 发布id
     */
    private ObjectId publishId;
    /**
     * 发布的时间
     */
    private Long date;
    
}