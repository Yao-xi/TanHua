package com.yx.tanhua.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 公告表
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Announcement extends BasePojo {
    
    /**
     * 主键id
     */
    private Long id;
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String description;
    
}