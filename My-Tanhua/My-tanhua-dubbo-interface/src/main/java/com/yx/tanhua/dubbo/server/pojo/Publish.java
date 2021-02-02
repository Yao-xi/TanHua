package com.yx.tanhua.dubbo.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * 发布表 动态内容
 * <p>
 * {@link Document} 用来表示对应mongodb中的哪个表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quanzi_publish")
public class Publish implements java.io.Serializable {
    
    private static final long serialVersionUID = 8732308321082804771L;
    
    /**
     * 主键id
     */
    private ObjectId id;
    
    /**
     * Long类型的id 用于推荐引擎使用
     */
    private Long pid;
    /**
     * 发布者的用户id
     */
    private Long userId;
    /**
     * 文字
     */
    private String text;
    /**
     * 媒体数据
     * <p>
     * 图片或小视频 url
     */
    private List<String> medias;
    /**
     * 谁可以看
     * <ul>
     * <li>1-公开</li>
     * <li>2-私密</li>
     * <li>3-部分可见</li>
     * <li>4-不给谁看</li>
     * </ul>
     */
    private Integer seeType;
    /**
     * 部分可见的列表
     */
    private List<Long> seeList;
    /**
     * 不给谁看的列表
     */
    private List<Long> notSeeList;
    /**
     * 经度
     */
    private String longitude;
    /**
     * 纬度
     */
    private String latitude;
    /**
     * 位置名称
     */
    private String locationName;
    /**
     * 发布时间
     */
    private Long created;
    
}