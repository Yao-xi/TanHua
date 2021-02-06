package com.yx.tanhua.dubbo.server.api;

import com.yx.tanhua.dubbo.server.pojo.Visitors;

import java.util.List;

public interface VisitorsApi {
    
    /**
     * 保存来访记录
     *
     * @param visitors
     *     来访信息
     *
     * @return {@link String} 主键id
     */
    String saveVisitor(Visitors visitors);
    
    /**
     * 按照时间倒序排序，查询最近的访客信息
     *
     * @param userId
     *     用户id
     * @param num
     *     个数
     *
     * @return {@link List<Visitors>}
     */
    List<Visitors> topVisitor(Long userId, Integer num);
    
    /**
     * 按照时间倒序排序，查询最近的访客信息
     *
     * @param userId
     *     用户id
     * @param date
     *     查询大于该时间的记录
     *
     * @return {@link List<Visitors>}
     */
    List<Visitors> topVisitor(Long userId, Long date);
}