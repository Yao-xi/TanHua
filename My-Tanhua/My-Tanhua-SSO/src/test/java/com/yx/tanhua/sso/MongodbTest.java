package com.yx.tanhua.sso;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MongodbTest {
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Test
    public void test01() {
    
    }
}
