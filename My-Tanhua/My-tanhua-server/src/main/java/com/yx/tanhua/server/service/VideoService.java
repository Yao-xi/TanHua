package com.yx.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.common.pojo.UserInfo;
import com.yx.tanhua.common.service.PicUploadService;
import com.yx.tanhua.common.vo.PicUploadResult;
import com.yx.tanhua.dubbo.server.api.QuanZiApi;
import com.yx.tanhua.dubbo.server.api.VideoApi;
import com.yx.tanhua.dubbo.server.pojo.Video;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import com.yx.tanhua.server.utils.UserThreadLocal;
import com.yx.tanhua.server.vo.PageResult;
import com.yx.tanhua.server.vo.VideoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@Slf4j
public class VideoService {
    
    @Autowired
    protected FastFileStorageClient storageClient;
    @Autowired
    private PicUploadService picUploadService;
    @Autowired
    private FdfsWebServer fdfsWebServer;
    
    @Reference(version = "1.0.0")
    private VideoApi videoApi;
    
    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private UserInfoService userInfoService;
    
    /**
     * 保存小视频
     *
     * @param picFile
     *     封面图片文件
     * @param videoFile
     *     视频文件
     *
     * @return {@link Boolean} 是否保存成功
     */
    public String saveVideo(MultipartFile picFile, MultipartFile videoFile) {
        // 获取当前用户信息
        User user = UserThreadLocal.get();
        // 构造video对象
        Video video = new Video();
        video.setUserId(user.getId());
        video.setSeeType(1);
        try {
            // 上传封面图片到OSS
            PicUploadResult picUploadResult = this.picUploadService.upload(picFile);
            // 填充图片路径
            video.setPicUrl(picUploadResult.getName());
            
            // 上传视频到fastDFS
            // storageClient.uploadFile(IO流, 文件大小, 文件扩展名, 元数据)
            StorePath storePath = storageClient.uploadFile(
                videoFile.getInputStream(),
                videoFile.getSize(),
                StringUtils.substringAfter(videoFile.getOriginalFilename(), "."),
                null);
            // 填充视频路径
            video.setVideoUrl(fdfsWebServer.getWebServerUrl() + "/" + storePath.getFullPath());
            // 写入到Mongodb
            return this.videoApi.saveVideo(video);
        } catch (Exception e) {
            log.error("视频上传失败 user=" + user, e);
        }
        // 失败返回false
        return null;
    }
    
    
    /**
     * 查询视频列表
     *
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link PageResult} 分页结果
     */
    @SuppressWarnings("DuplicatedCode")
    public PageResult queryVideoList(Integer page, Integer pageSize) {
        // 获取当前用户信息
        User user = UserThreadLocal.get();
        // 封装分页结果
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);
        pageResult.setPages(60);
        pageResult.setCounts(600);
        
        // ------ 小视频推荐 ------
        PageInfo<Video> pageInfo = null;
        // 先从Redis查询
        String redisValue = this.redisTemplate.opsForValue().get("QUANZI_VIDEO_RECOMMEND_" + user.getId());
        if (StringUtils.isNotEmpty(redisValue)) {
            // 缓存命中则返回推荐列表
            String[] vids = StringUtils.split(redisValue, ',');
            // 计算分页开始索引
            int startIndex = (page - 1) * pageSize;
            if (startIndex < vids.length && startIndex >= 0) {
                // 计算分页结束索引
                int endIndex = startIndex + pageSize - 1;
                if (endIndex >= vids.length) {
                    endIndex = vids.length - 1;
                }
                // 获取满足的vid列表
                List<Long> vidList = new ArrayList<>();
                for (int i = startIndex; i <= endIndex; i++) {
                    vidList.add(Long.valueOf(vids[i]));
                }
                // 通过vid查询video
                List<Video> videoList = this.videoApi.queryVideoListByVids(vidList);
                // 封装pageInfo
                pageInfo = new PageInfo<>();
                pageInfo.setRecords(videoList);
            }
        }
        
        if (null == pageInfo) {
            // 缓存未命中查询默认列表
            // 远程调用 获取分页信息
            pageInfo = this.videoApi.queryVideoList(page, pageSize);
        }
        
        
        this.videoApi.queryVideoList(page, pageSize);
        // 获取视频列表
        List<Video> records = pageInfo.getRecords();
        // 构造返回给前端的数据
        List<VideoVo> videoVoList = new ArrayList<>();
        // 构造用户id列表
        // List<Long> userIds = new ArrayList<>();
        // 用户id列表需要去重 顺序不影响 所以考虑改用Set
        Set<Long> userIds = new HashSet<>();
        
        for (Video record : records) {
            // 构造videoVo对象
            VideoVo videoVo = new VideoVo();
            // 填充videoVo对象
            videoVo.setUserId(record.getUserId());
            videoVo.setCover(record.getPicUrl());
            videoVo.setVideoUrl(record.getVideoUrl());
            videoVo.setId(record.getId().toHexString());
            videoVo.setSignature("我就是我~");
            
            // 获取评论数
            // todo 可以改用redis缓存解决
            Long commentCount = this.quanZiApi.queryCommentCount(videoVo.getId(), 2);
            videoVo.setCommentCount(commentCount == null ? 0 : commentCount.intValue());
            
            // 是否已经关注
            String followUserKey = "VIDEO_FOLLOW_USER_" + user.getId() + "_" + videoVo.getUserId();
            //noinspection ConstantConditions
            videoVo.setHasFocus(this.redisTemplate.hasKey(followUserKey) ? 1 : 0);
            // 给videoVo封装点赞相关信息
            setLikeInfo(user, videoVo);
            
            // 向用户id列表中添加用户id
            // if (!userIds.contains(record.getUserId())) {
            //     userIds.add(record.getUserId());
            // }
            userIds.add(record.getUserId());
            
            videoVoList.add(videoVo);
        }
        // 构造查询条件
        QueryWrapper<UserInfo> queryWrapper =
            new QueryWrapper<UserInfo>().in("user_id", userIds);
        // 查询Mysql 获取用户完整信息
        List<UserInfo> userInfos = this.userInfoService.queryUserInfoList(queryWrapper);
        Map<Long, UserInfo> userInfoMap = new HashMap<>();
        for (UserInfo userInfo : userInfos) {
            userInfoMap.put(userInfo.getUserId(), userInfo);
        }
        for (VideoVo videoVo : videoVoList) {
            UserInfo userInfo = userInfoMap.get(videoVo.getUserId());
            // 给videoVo添加用户详细信息
            videoVo.setNickname(userInfo.getNickName());
            videoVo.setAvatar(userInfo.getLogo());
        }
        
        pageResult.setItems(videoVoList);
        return pageResult;
    }
    
    /**
     * 代码块抽取
     * <p>
     * 封装点赞相关信息
     */
    private void setLikeInfo(User user, VideoVo videoVo) {
        // 是否已经点赞（1是，0否）
        String userKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" + videoVo.getId();
        //noinspection ConstantConditions
        videoVo.setHasLiked(this.redisTemplate.hasKey(userKey) ? 1 : 0);
        // 获取点赞数
        String key = "QUANZI_COMMENT_LIKE_" + videoVo.getId();
        String value = this.redisTemplate.opsForValue().get(key);
        //noinspection ConstantConditions
        if (StringUtils.isNotEmpty(value) && value.matches("^[0-9]*$")) {
            videoVo.setLikeCount(Integer.valueOf(value));
        } else {
            videoVo.setLikeCount(0);
        }
    }
    
    /**
     * 关注用户
     *
     * @param userId
     *     关注用户的id
     *
     * @return 是否关注成功
     */
    public Boolean followUser(Long userId) {
        // 获取当前用户信息
        User user = UserThreadLocal.get();
        // 远程调用 添加关注
        Boolean success = this.videoApi.followUser(user.getId(), userId);
        if (success) {
            //记录已关注
            String followUserKey = "VIDEO_FOLLOW_USER_" + user.getId() + "_" + userId;
            this.redisTemplate.opsForValue().set(followUserKey, "1");
        }
        return success;
    }
    
    /**
     * 取消关注
     *
     * @param userId
     *     关注用户的id
     *
     * @return 是否取消关注成功
     */
    public Boolean disFollowUser(Long userId) {
        // 获取当前用户信息
        User user = UserThreadLocal.get();
        // 远程调用 取消关注
        Boolean success = this.videoApi.disFollowUser(user.getId(), userId);
        if (success) {
            // 移除已关注
            String followUserKey = "VIDEO_FOLLOW_USER_" + user.getId() + "_" + userId;
            this.redisTemplate.delete(followUserKey);
        }
        return success;
    }
}
