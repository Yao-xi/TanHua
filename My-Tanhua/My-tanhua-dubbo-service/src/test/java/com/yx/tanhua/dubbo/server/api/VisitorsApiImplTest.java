package com.yx.tanhua.dubbo.server.api;

import com.alibaba.dubbo.config.annotation.Reference;
import com.yx.tanhua.dubbo.server.pojo.Visitors;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class VisitorsApiImplTest {
    
    @Reference(version = "1.0.0")
    private VisitorsApi visitorsApi;
    
    @Test
    public void testSave() {
        for (int i = 0; i < 100; i++) {
            Visitors visitors = new Visitors();
            
            visitors.setFrom("首页");
            visitors.setUserId(RandomUtils.nextLong(1, 10));
            visitors.setVisitorUserId(RandomUtils.nextLong(11, 50));
            
            this.visitorsApi.saveVisitor(visitors);
        }
        
        System.out.println("ok");
        
    }
}