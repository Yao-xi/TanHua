package com.yx.tanhua.sso.service;

import com.aliyun.oss.OSSClient;
import com.yx.tanhua.common.config.AliyunConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PicUploadServiceTest {
    @Autowired
    private OSSClient ossClient;
    @Autowired
    private AliyunConfig aliyunConfig;
    
    @Test
    public void testDelete() {
        String file = "images/2021/01/18/16109618295759085.jpg";
        ossClient.deleteObject(aliyunConfig.getBucketName(), file);
    }
    
    @Test
    public void testUpload() throws Exception {
        String filePath = "images/test/img123456.jpg";
        File uploadFile = new File("C:\\Users\\Yaoxi\\Pictures\\Saved Pictures\\图像.jpg");
        ossClient.putObject(aliyunConfig.getBucketName(), filePath, new
            FileInputStream(uploadFile));
    }
}