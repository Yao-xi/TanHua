package com.yx.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 今日佳人
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodayBest {
    
    /**
     * 用户id
     */
    private Long id;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 性别 {@code man} {@code woman}
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
     * 缘分值
     */
    private Long fateValue;
}
