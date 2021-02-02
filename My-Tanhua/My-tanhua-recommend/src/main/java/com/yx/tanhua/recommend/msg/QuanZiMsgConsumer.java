package com.yx.tanhua.recommend.msg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yx.tanhua.dubbo.server.pojo.Publish;
import com.yx.tanhua.recommend.pojo.RecommendQuanZi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * 圈子消息 消费者
 * <p>
 * 接收MQ消息
 */
@Component
@RocketMQMessageListener(topic = "tanhua-quanzi",
                         consumerGroup = "tanhua-quanzi-consumer")
@Slf4j
public class QuanZiMsgConsumer implements RocketMQListener<String> {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    /**
     * 收到消息会自动调用这个方法
     *
     * @param msg
     *     消息内容
     */
    @Override
    public void onMessage(String msg) {
        try {
            // 处理Json数据
            JsonNode jsonNode = MAPPER.readTree(msg);
            int type = jsonNode.get("type").asInt();
            String publishId = jsonNode.get("publishId").asText();
            Long date = jsonNode.get("date").asLong();
            Long userId = jsonNode.get("userId").asLong();
            Long pid = jsonNode.get("pid").asLong();
            // 构造pojo类
            RecommendQuanZi recommendQuanZi = new RecommendQuanZi();
            recommendQuanZi.setPublishId(pid);
            recommendQuanZi.setDate(date);
            recommendQuanZi.setId(ObjectId.get());
            recommendQuanZi.setUserId(userId);
            
            /*
             * case: 1-发动态
             *       2-浏览动态
             *       3-点赞
             *       4-喜欢
             *       5-评论
             *       6-取消点赞
             *       7-取消喜欢
             *
             * 动态计分规则:
             *   - 浏览 +1
             *   - 点赞 +5
             *   - 喜欢 +8
             *   - 评论 +10
             *   - 文字长度 50以内1分 50~100之间2分 100以上3分
             *   - 图片个数 每个图片1分
             * */
            switch (type) {
                // 1-发动态
                case 1: {
                    int score = 0;
                    Publish publish = this.mongoTemplate.findById(new ObjectId(publishId), Publish.class);
                    if (publish != null) {
                        // 文字长度 50以内1分 50~100之间2分 100以上3分
                        int length = StringUtils.length(publish.getText());
                        if (length > 0 && length <= 50) {
                            score = 1;
                        } else if (length > 50 && length <= 100) {
                            score = 2;
                        } else if (length > 100) {
                            score = 3;
                        }
                        // 图片个数 每个图片1分
                        if (!CollectionUtils.isEmpty(publish.getMedias())) {
                            score += publish.getMedias().size();
                        }
                    }
                    recommendQuanZi.setScore((double) score);
                    break;
                }
                // 2-浏览动态
                case 2: {
                    recommendQuanZi.setScore(1d);
                    break;
                }
                // 3-点赞
                case 3: {
                    recommendQuanZi.setScore(5d);
                    break;
                }
                // 4-喜欢
                case 4: {
                    recommendQuanZi.setScore(8d);
                    break;
                }
                // 5-评论
                case 5: {
                    recommendQuanZi.setScore(10d);
                    break;
                }
                // 6-取消点赞
                case 6: {
                    recommendQuanZi.setScore(-5d);
                    break;
                }
                // 7-取消喜欢
                case 7: {
                    recommendQuanZi.setScore(-8d);
                    break;
                }
                default: {
                    recommendQuanZi.setScore(0d);
                    break;
                }
                
            }
            // 将数据写入到MongoDB
            String collectName = "recommend_quanzi_" + new DateTime().toString("yyyyMMdd");
            this.mongoTemplate.save(recommendQuanZi, collectName);
            
        } catch (Exception e) {
            log.error("消息处理失败! msg = " + msg);
        }
        
    }
}