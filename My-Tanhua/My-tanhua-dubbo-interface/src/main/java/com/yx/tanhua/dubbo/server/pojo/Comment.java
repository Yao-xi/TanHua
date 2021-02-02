package com.yx.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 评论表 quanzi_comment
 * <p>
 * 点赞 评论 喜欢 都视为对动态的评论
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_comment")
public class Comment implements java.io.Serializable {
    
    private static final long serialVersionUID = -291788258125767614L;
    
    /**
     * 主键id
     */
    private ObjectId id;
    
    /**
     * 发布id
     */
    private ObjectId publishId;
    /**
     * 评论类型
     * <ul>
     *     <li>1-点赞</li>
     *     <li>2-评论 只有评论会有额外的字段</li>
     *     <li>3-喜欢</li>
     * </ul>
     */
    private Integer commentType;
    /**
     * 评论内容
     */
    private String content;
    /**
     * 评论人id
     */
    private Long userId;
    /**
     * 发布人的用户id
     */
    private Long publishUserId;
    
    /**
     * 是否为父节点 是否是对某个评论的评论
     * <p>
     * 默认{@code false}
     */
    private Boolean isParent = false;
    /**
     * 父节点id
     */
    private ObjectId parentId;
    
    /**
     * 发表时间
     */
    private Long created;
    
}