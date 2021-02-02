package com.yx.tanhua.dubbo.server.api;

import com.yx.tanhua.dubbo.server.pojo.Video;
import com.yx.tanhua.dubbo.server.vo.PageInfo;

import java.util.List;

public interface VideoApi {
    
    /**
     * 保存小视频
     *
     * @param video
     *     视频
     *
     * @return 是否保存成功
     */
    String saveVideo(Video video);
    
    /**
     * 分页查询小视频列表，按照时间倒序排序
     *
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link PageInfo<Video>}
     */
    PageInfo<Video> queryVideoList(Integer page, Integer pageSize);
    
    /**
     * 关注用户
     *
     * @param userId
     *     用户id
     * @param followUserId
     *     关注的用户id
     *
     * @return 是否关注成功
     */
    Boolean followUser(Long userId, Long followUserId);
    
    /**
     * 取消关注用户
     *
     * @param userId
     *     用户id
     * @param followUserId
     *     关注的用户id
     *
     * @return 是否取消关注成功
     */
    Boolean disFollowUser(Long userId, Long followUserId);
    
    /**
     * 根据id查询视频信息
     */
    Video queryVideoById(String vid);
    /**
     * 根据vid批量查询数据
     */
    List<Video> queryVideoListByVids(List<Long> vids);
}
