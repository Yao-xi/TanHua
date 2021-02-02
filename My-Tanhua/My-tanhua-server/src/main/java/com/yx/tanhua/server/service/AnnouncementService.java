package com.yx.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yx.tanhua.common.pojo.Announcement;
import com.yx.tanhua.server.mapper.AnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;
    
    
    /**
     * 查询公告列表
     */
    public IPage<Announcement> queryList(Integer page, Integer pageSize) {
        // 构造查询条件
        QueryWrapper<Announcement> queryWrapper =
            new QueryWrapper<Announcement>().orderByDesc("created");
        return this.announcementMapper.selectPage(new Page<>(page, pageSize), queryWrapper);
    }
}