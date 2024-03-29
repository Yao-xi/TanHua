package com.yx.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 来访者信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "visitors")
public class Visitors implements java.io.Serializable {
    
    private static final long serialVersionUID = 2811682148052386573L;
    
    /**
     * 主键id
     */
    private ObjectId id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 来访用户id
     */
    private Long visitorUserId;
    /**
     * 访问来源 如首页,圈子等
     */
    private String from;
    /**
     * 来访时间
     */
    private Long date;
    
    /**
     * 得分
     */
    private Double score;
    
}
