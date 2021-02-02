package com.yx.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * 推荐用户
 * <p>
 * 仅携带被推荐用户id和推荐用户id及推荐得分 需要额外去Mysql查UserInfo表补全信息
 * <p>
 * 实现{@link Serializable}接口 可以被序列化 因为要通过网络传输
 * <p>
 * {@link Document}注解 标识这个类和Mongodb的表对应
 *
 * @author Yaoxi
 * @date 2021/01/21 18:34:03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "recommend_user") // Mongodb对应的表
public class RecommendUser implements Serializable {
    
    private static final long serialVersionUID = -4296017160071130962L;
    
    /**
     * 主键id
     * <p>
     * {@link Id} 标识为主键id
     */
    @Id
    private ObjectId id;
    /**
     * 推荐的用户id
     * <p>
     * {@link Indexed} 建立索引
     */
    @Indexed
    private Long userId;
    /**
     * 用户id
     */
    private Long toUserId;
    /**
     * 推荐得分
     * <p>
     * {@link Indexed} 建立索引
     */
    @Indexed
    private Double score;
    /**
     * 日期
     * <p>
     * 注意这里是用{@link String}类型 存入Mongodb的
     */
    private String date;
}
