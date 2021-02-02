package com.yx.tanhua.common.service;

import com.aliyun.oss.OSSClient;
import com.yx.tanhua.common.config.AliyunConfig;
import com.yx.tanhua.common.vo.PicUploadResult;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

/**
 * 图片上传 Service
 *
 * @author Yaoxi
 * @date 2021/01/17 11:39:31
 */
@Service
public class PicUploadService {
    
    /**
     * 允许上传的格式
     */
    private static final String[] IMAGE_TYPE = new String[]{".bmp", ".jpg",
                                                            ".jpeg", ".gif", ".png"};
    @Autowired
    private OSSClient ossClient;
    
    @Autowired
    private AliyunConfig aliyunConfig;
    
    /**
     * 上传
     *
     * @param uploadFile
     *     multipart请求中收到的上传文件
     *
     * @return {@link PicUploadResult} 图片上传结果
     */
    public PicUploadResult upload(MultipartFile uploadFile) {
        
        PicUploadResult fileUploadResult = new PicUploadResult();
        
        // 图片格式校验
        boolean isLegal = false;
        for (String type : IMAGE_TYPE) {
            // StringUtils.endsWithIgnoreCase: 忽略大小写，检查字符串是否以指定的后缀结尾
            if (StringUtils.endsWithIgnoreCase(uploadFile.getOriginalFilename(), type)) {
                isLegal = true;
                break;
            }
        }
        if (!isLegal) {
            // 文件类型非法 返回error
            fileUploadResult.setStatus("error");
            return fileUploadResult;
        }
        
        // 获取原始文件名
        String fileName = uploadFile.getOriginalFilename();
        // 文件路径: images/2018/12/29/xxxx.jpg
        String filePath = getFilePath(fileName);
        
        try {
            // 上传到阿里云
            ossClient.putObject(aliyunConfig.getBucketName(), filePath,
                                new ByteArrayInputStream(uploadFile.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            //上传失败
            fileUploadResult.setStatus("error");
            return fileUploadResult;
        }
        
        // 上传成功
        fileUploadResult.setStatus("done");
        // Url前缀 + 文件路径
        fileUploadResult.setName(this.aliyunConfig.getUrlPrefix() + filePath);
        fileUploadResult.setUid(String.valueOf(System.currentTimeMillis()));
        
        return fileUploadResult;
    }
    
    /**
     * 获取上传后的文件路径
     */
    private String getFilePath(String sourceFileName) {
        DateTime dateTime = new DateTime();
        // 文件路径: "images/yyyy/MM/dd/"+"系统当前时间毫秒值"+"100-9999随机数"+".文件后缀名"
        return "images/" + dateTime.toString("yyyy")
               + "/" + dateTime.toString("MM") + "/"
               + dateTime.toString("dd") + "/" + System.currentTimeMillis() +
               RandomUtils.nextInt(100, 9999) + "." +
               StringUtils.substringAfterLast(sourceFileName, ".");
    }
}
