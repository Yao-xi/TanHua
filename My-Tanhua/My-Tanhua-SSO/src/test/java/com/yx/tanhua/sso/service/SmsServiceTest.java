package com.yx.tanhua.sso.service;

import com.yx.tanhua.common.vo.ErrorResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsServiceTest {
    
    @Autowired
    SmsService smsService;
    
    @Test
    public void sendCheckCode() {
        ErrorResult errorResult = smsService.sendCheckCode("18338228591");
        System.out.println(errorResult);
    }
}