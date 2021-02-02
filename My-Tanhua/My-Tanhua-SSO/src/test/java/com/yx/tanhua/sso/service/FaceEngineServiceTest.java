package com.yx.tanhua.sso.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FaceEngineServiceTest {
    
    @Autowired
    private FaceEngineService faceEngineService;
    
    @Test
    public void testCheckIsPortrait() {
        File file = new File("G:\\temp\\头像01.jpg");
        boolean checkIsPortrait = this.faceEngineService.checkIsPortrait(file);
        System.out.println(checkIsPortrait); // true|false
    }
}