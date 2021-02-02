package com.yx.tanhua.sso;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {
    @Autowired
    private RedisTemplate<String,String> template;
    
    @Test
    public void test01(){
        template.opsForValue().set("TEST_KEY_123456", "123456",Duration.ofMinutes(20));
        // redisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(20));
    }
}
