package com.yx.tanhua.server.service;

import com.yx.tanhua.server.vo.TodayBest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RecommendUserServiceTest {
    
    @Autowired
    RecommendUserService recommendUserService;
    
    @Test
    public void queryTodayBest() {
        TodayBest todayBest = recommendUserService.queryTodayBest(1L);
        System.out.println(todayBest);
    }
}