package com.yx.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.dubbo.server.api.VideoApi;
import com.yx.tanhua.dubbo.server.pojo.Video;
import com.yx.tanhua.server.utils.UserThreadLocal;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class VideoMQService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoMQService.class);
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    @Reference(version = "1.0.0")
    private VideoApi videoApi;
    
    /**
     * 发布小视频消息
     *
     * @return
     */
    public Boolean videoMsg(String videoId) {
        return this.sendMsg(videoId, 1);
    }
    
    /**
     * 发送小视频操作相关的消息
     *
     * @param videoId
     *     videoId
     * @param type
     *     1-发动态 2-点赞 3-取消点赞 4-评论
     *
     * @return 是否成功发送消息
     */
    private Boolean sendMsg(String videoId, Integer type) {
        try {
            // 获取当前用户
            User user = UserThreadLocal.get();
            // 查video信息
            Video video = this.videoApi.queryVideoById(videoId);
            
            // 构建消息
            Map<String, Object> msg = new HashMap<>();
            msg.put("userId", user.getId());
            msg.put("date", System.currentTimeMillis());
            msg.put("videoId", videoId);
            msg.put("vid", video.getVid());
            msg.put("type", type);
            // 发消息
            this.rocketMQTemplate.convertAndSend("tanhua-video", msg);
        } catch (Exception e) {
            LOGGER.error("发送消息失败! videoId = " + videoId + ", type = " + type, e);
            return false;
        }
        
        return true;
    }
    
    /**
     * 点赞小视频
     *
     * @return
     */
    public Boolean likeVideoMsg(String videoId) {
        return this.sendMsg(videoId, 2);
    }
    
    /**
     * 取消点赞小视频
     *
     * @return
     */
    public Boolean disLikeVideoMsg(String videoId) {
        return this.sendMsg(videoId, 3);
    }
    
    /**
     * 评论小视频
     *
     * @return
     */
    public Boolean commentVideoMsg(String videoId) {
        return this.sendMsg(videoId, 4);
    }
}
