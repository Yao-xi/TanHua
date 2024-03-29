package com.yx.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装发布出来的动态详细内容
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movements {
    
    /**
     * 动态id
     */
    private String id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 性别
     * <ul>
     *     <li>man</li>
     *     <li>woman</li>
     * </ul>
     */
    private String gender;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 标签
     */
    private String[] tags;
    /**
     * 文字动态
     */
    private String textContent;
    /**
     * 图片动态
     */
    private String[] imageContent;
    /**
     * 距离
     */
    private String distance;
    /**
     * 发布时间
     * <p>
     * 如: 10分钟前
     */
    private String createDate;
    /**
     * 点赞数
     */
    private Integer likeCount;
    /**
     * 评论数
     */
    private Integer commentCount;
    /**
     * 喜欢数
     */
    private Integer loveCount;
    /**
     * 是否点赞
     * <p>
     * (1-是 0-否)
     */
    private Integer hasLiked;
    /**
     * 是否喜欢
     * <p>
     * (1-是 0-否)
     */
    private Integer hasLoved;
    
}