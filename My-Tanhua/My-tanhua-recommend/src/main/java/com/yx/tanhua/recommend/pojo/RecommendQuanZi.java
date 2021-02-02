package com.yx.tanhua.recommend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

/**
 * 圈子推荐
 * <p>
 * 存储到MongoDB的中的实体结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendQuanZi {
    
    /**
     * 主键id
     */
    private ObjectId id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 动态id 需要转化为Long类型
     */
    private Long publishId;
    /**
     * 得分
     */
    private Double score;
    /**
     * 时间戳
     */
    private Long date;
}