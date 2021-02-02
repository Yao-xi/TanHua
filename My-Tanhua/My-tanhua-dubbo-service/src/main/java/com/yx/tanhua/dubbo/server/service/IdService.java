package com.yx.tanhua.dubbo.server.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 使用redis的自增长值 生成自增长的id
 */
@Service
public class IdService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 利用redis创建自增长id
     *
     * @param type
     *     用于拼Redis的key
     * @param objectId
     *     用于传递相同参数可以获取到相同的id值
     *
     * @return {@link Long} 自增长后的id
     */
    public Long createId(String type, String objectId) {
        // 转大写
        type = StringUtils.upperCase(type);
        
        String hashKey = "TANHUA_HASH_ID_" + type;
        // 查hash表中 如果ObjectId已经存在的话 就返回对应的id
        if (this.redisTemplate.opsForHash().hasKey(hashKey, objectId)) {
            //noinspection ConstantConditions
            return Long.valueOf(this.redisTemplate.opsForHash().get(hashKey, objectId).toString());
        }
        // 生成自增长id
        String key = "TANHUA_ID_" + type;
        Long id = this.redisTemplate.opsForValue().increment(key);
        
        // 将生成的id写入到hash表中 (因为是永久存储 所以数据量大的话会占用空间)
        //noinspection ConstantConditions
        this.redisTemplate.opsForHash().put(hashKey, objectId, id.toString());
        
        return id;
    }
    
}