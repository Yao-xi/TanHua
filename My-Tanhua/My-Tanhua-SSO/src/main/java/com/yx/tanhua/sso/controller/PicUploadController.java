package com.yx.tanhua.sso.controller;

import com.yx.tanhua.common.service.PicUploadService;
import com.yx.tanhua.common.vo.PicUploadResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("pic/upload")
@RestController
public class PicUploadController {
    @Autowired
    private PicUploadService picUploadService;
    
    /**
     * 测试图片上传用
     */
    @PostMapping
    @ResponseBody
    public PicUploadResult upload(@RequestParam("file") MultipartFile multipartFile) {
        return this.picUploadService.upload(multipartFile);
    }
}
