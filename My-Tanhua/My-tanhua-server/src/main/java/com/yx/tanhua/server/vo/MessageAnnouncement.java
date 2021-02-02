package com.yx.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公告消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageAnnouncement {
    
    /**
     * 主键id
     */
    private String id;
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String description;
    /**
     * 创建日期
     */
    private String createDate;
    
}