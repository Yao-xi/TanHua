package com.yx.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RecommendUserApiTest {
    
    // @Autowired
    @Reference(version = "1.0.0")
    private RecommendUserApi recommendUserApi;
    
    @Test
    public void testQueryWithMaxScore() {
        System.out.println(this.recommendUserApi.queryWithMaxScore(1L));
        System.out.println(this.recommendUserApi.queryWithMaxScore(8L));
        System.out.println(this.recommendUserApi.queryWithMaxScore(26L));
    }
    
    @Test
    public void testQueryPageInfo() {
        System.out.println(this.recommendUserApi.queryPageInfo(1L, 1, 5));
        System.out.println(this.recommendUserApi.queryPageInfo(1L, 2, 5));
        System.out.println(this.recommendUserApi.queryPageInfo(1L, 3, 5));
    }
    
}