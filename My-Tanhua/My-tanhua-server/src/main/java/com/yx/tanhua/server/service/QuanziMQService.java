package com.yx.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.dubbo.server.api.QuanZiApi;
import com.yx.tanhua.dubbo.server.pojo.Publish;
import com.yx.tanhua.server.utils.UserThreadLocal;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 圈子发MQ消息业务 用于和大数据平台交互
 */
@Service
public class QuanziMQService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(QuanziMQService.class);
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;
    
    /**
     * 发布动态消息
     *
     * @param publishId 动态id
     *
     * @return 是否发消息成功
     */
    public Boolean publishMsg(String publishId) {
        return this.sendMsg(publishId, 1);
    }
    
    /**
     * 发送圈子操作相关的消息
     *
     * @param publishId 动态发布的id
     * @param type
     *     <ul>
     *        <li>1-发动态</li>
     *        <li>2-浏览动态</li>
     *        <li>3-点赞</li>
     *        <li>4-喜欢</li>
     *        <li>5-评论</li>
     *        <li>6-取消点赞</li>
     *        <li>7-取消喜欢</li>
     *     </ul>
     *
     * @return 是否发消息成功
     */
    private Boolean sendMsg(String publishId, Integer type) {
        try {
            // 获取当前用户
            User user = UserThreadLocal.get();
            // 远程调用 根据id查发布的动态信息
            Publish publish = this.quanZiApi.queryPublishById(publishId);
            
            // 构建消息
            Map<String, Object> msg = new HashMap<>();
            msg.put("userId", user.getId());
            msg.put("date", System.currentTimeMillis());
            msg.put("publishId", publishId);
            msg.put("pid", publish.getPid());
            msg.put("type", type);
            // 发MQ消息
            this.rocketMQTemplate.convertAndSend("tanhua-quanzi", msg);
        } catch (Exception e) {
            LOGGER.error("发送消息失败! publishId = " + publishId + ", type = " + type, e);
            return false;
        }
        // 发送成功
        return true;
    }
    
    /**
     * 浏览动态消息
     *
     * @param publishId 动态id
     *
     * @return 是否发消息成功
     */
    public Boolean queryPublishMsg(String publishId) {
        return this.sendMsg(publishId, 2);
    }
    
    /**
     * 点赞动态消息
     *
     * @param publishId 动态id
     *
     * @return 是否发消息成功
     */
    public Boolean likePublishMsg(String publishId) {
        return this.sendMsg(publishId, 3);
    }
    
    /**
     * 取消点赞动态消息
     *
     * @param publishId 动态id
     *
     * @return 是否发消息成功
     */
    public Boolean disLikePublishMsg(String publishId) {
        return this.sendMsg(publishId, 6);
    }
    
    /**
     * 喜欢动态消息
     *
     * @param publishId 动态id
     *
     * @return 是否发消息成功
     */
    public Boolean lovePublishMsg(String publishId) {
        return this.sendMsg(publishId, 4);
    }
    
    /**
     * 取消喜欢动态消息
     *
     * @param publishId 动态id
     *
     * @return 是否发消息成功
     */
    public Boolean disLovePublishMsg(String publishId) {
        return this.sendMsg(publishId, 7);
    }
    
    /**
     * 评论动态消息
     *
     * @param publishId 动态id
     *
     * @return 是否发消息成功
     */
    public Boolean sendCommentPublishMsg(String publishId) {
        return this.sendMsg(publishId, 5);
    }
}