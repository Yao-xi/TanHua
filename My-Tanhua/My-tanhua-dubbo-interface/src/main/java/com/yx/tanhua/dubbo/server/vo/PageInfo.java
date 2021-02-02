package com.yx.tanhua.dubbo.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页信息
 * <p>
 * {@link Serializable} 需要序列化 用于网络传输
 * <p>
 * 因为前端要求不能为Null 所以需要有初始化值
 *
 * @author Yaoxi
 * @date 2021/01/21 18:40:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageInfo<T> implements Serializable {
    
    private static final long serialVersionUID = -2105385689859184204L;
    
    /**
     * 总条数
     */
    private Integer total = 0;
    
    /**
     * 当前页
     */
    private Integer pageNum = 0;
    
    /**
     * 每页条数
     */
    private Integer pageSize = 0;
    
    /**
     * 数据列表
     */
    private List<T> records = Collections.emptyList();
}
