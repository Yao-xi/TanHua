package com.yx.tanhua.dubbo.server.api;

import com.yx.tanhua.dubbo.server.pojo.Comment;
import com.yx.tanhua.dubbo.server.pojo.Publish;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@RunWith(SpringRunner.class)
public class QuanZiApiImplTest {
    
    @Autowired
    private QuanZiApi quanZiApi;
    
    /**
     * 测试发布动态
     */
    @Test
    public void testSavePublish() {
        Publish publish = new Publish();
        publish.setUserId(1L);
        publish.setLocationName("上海市");
        publish.setSeeType(1);
        publish.setText("今天天气不错~");
        publish.setMedias(
            Collections.singletonList("https://itcast-tanhua.oss-cn-shanghai.aliyuncs.com/images/quanzi/1.jpg"));
        String result = this.quanZiApi.savePublish(publish);
        System.out.println(result);
    }
    
    @Test
    public void testQueryCommentCount() {
        String publishId = "5fae54037e52992e78a3fd5d";
        PageInfo<Comment> commentPageInfo = quanZiApi.queryCommentList(publishId, 1, 100);
        System.out.println(commentPageInfo);
        Long count = quanZiApi.queryCommentCount(publishId, 1);
        System.out.println(count);
    }
}