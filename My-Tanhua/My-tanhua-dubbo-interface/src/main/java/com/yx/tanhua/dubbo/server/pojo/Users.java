package com.yx.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 用户和好友的关系表
 * <p>
 * 如果一个用户是另一个用户的好友 则会添加一条记录在这个表中
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tanhua_users")
public class Users implements java.io.Serializable {
    
    private static final long serialVersionUID = 6003135946820874230L;
    /**
     * 主键id
     */
    private ObjectId id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 好友id
     */
    private Long friendId;
    /**
     * 创建时间
     */
    private Long date;
}