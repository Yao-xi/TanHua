package com.yx.tanhua.common.vo;

import lombok.Data;

/**
 * 图片上传返回对象
 *
 * @author Yaoxi
 * @date 2021/01/17 11:46:48
 */
@Data
public class PicUploadResult {
    
    /**
     * 文件唯一标识
     */
    private String uid;
    
    /**
     * 文件名
     */
    private String name;
    
    /**
     * 状态: uploading done error removed
     */
    private String status;
    
    /**
     * 服务端响应内容
     * <p>
     * 如:'{"status": "success"}'
     */
    private String response;
}
