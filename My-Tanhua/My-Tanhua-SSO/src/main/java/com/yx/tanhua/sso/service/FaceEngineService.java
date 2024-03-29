package com.yx.tanhua.sso.service;

import com.arcsoft.face.EngineConfiguration;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FunctionConfiguration;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectOrient;
import com.arcsoft.face.enums.ErrorInfo;
import com.arcsoft.face.enums.ImageFormat;
import com.arcsoft.face.toolkit.ImageFactory;
import com.arcsoft.face.toolkit.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaoxi
 * @date 2021/01/17 16:03:12
 */
@Service
@Slf4j
public class FaceEngineService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FaceEngineService.class);
    
    @Value("${arcsoft.appid}")
    private String appid;
    
    @Value("${arcsoft.sdkKey}")
    private String sdkKey;
    
    @Value("${arcsoft.libPath}")
    private String libPath;
    
    private FaceEngine faceEngine;
    
    /**
     * 初始化引擎
     * <p>
     * {@code @PostConstruct} 在该类使用之前必须调用此方法
     */
    @PostConstruct
    public void init() {
        // 激活并且初始化引擎
        FaceEngine faceEngine = new FaceEngine(libPath);
        int activeCode = faceEngine.activeOnline(appid, sdkKey);
        if (activeCode != ErrorInfo.MOK.getValue() && activeCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
            LOGGER.error("引擎激活失败");
            throw new RuntimeException("引擎激活失败");
        }
        
        //引擎配置
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        //IMAGE检测模式，用于处理单张的图像数据
        engineConfiguration.setDetectMode(DetectMode.ASF_DETECT_MODE_IMAGE);
        //人脸检测角度，全角度
        engineConfiguration.setDetectFaceOrientPriority(DetectOrient.ASF_OP_ALL_OUT);
        
        //功能配置
        FunctionConfiguration functionConfiguration = new FunctionConfiguration();
        functionConfiguration.setSupportAge(true);
        functionConfiguration.setSupportFace3dAngle(true);
        functionConfiguration.setSupportFaceDetect(true);
        functionConfiguration.setSupportFaceRecognition(true);
        functionConfiguration.setSupportGender(true);
        functionConfiguration.setSupportLiveness(true);
        functionConfiguration.setSupportIRLiveness(true);
        engineConfiguration.setFunctionConfiguration(functionConfiguration);
        
        //初始化引擎
        int initCode = faceEngine.init(engineConfiguration);
        
        if (initCode != ErrorInfo.MOK.getValue()) {
            LOGGER.error("初始化引擎出错!");
            throw new RuntimeException("初始化引擎出错!");
        }
        
        this.faceEngine = faceEngine;
    }
    
    /**
     * 检测图片是否为人像
     *
     * @param imageData
     *     {@code byte[]} 图像数据对象
     *
     * @return {@code true}:人像 {@code false}:非人像
     *
     * @see #checkIsPortrait(ImageInfo)
     */
    public boolean checkIsPortrait(byte[] imageData) {
        return this.checkIsPortrait(ImageFactory.getRGBData(imageData));
    }
    
    /**
     * 检测图片是否为人像
     *
     * @param imageInfo
     *     图像信息对象
     *
     * @return {@code true}:人像 {@code false}:非人像
     */
    public boolean checkIsPortrait(ImageInfo imageInfo) {
        // 定义人脸列表
        List<FaceInfo> faceInfoList = new ArrayList<>();
        // 检查人脸 检查到的数据存入人脸列表中
        faceEngine.detectFaces(
            imageInfo.getImageData(),   // 图片数据
            imageInfo.getWidth(),       // 宽度
            imageInfo.getHeight(),      // 高度
            ImageFormat.CP_PAF_BGR24,   // 图像格式
            faceInfoList                // 人脸列表
        );
        log.debug("faceInfoList: " +faceInfoList);
        return !faceInfoList.isEmpty();
    }
    
    
    /**
     * 检测图片是否为人像
     *
     * @param file
     *     图片文件
     *
     * @return {@code true}:人像 {@code false}:非人像
     *
     * @see #checkIsPortrait(ImageInfo)
     */
    public boolean checkIsPortrait(File file) {
        return this.checkIsPortrait(ImageFactory.getRGBData(file));
    }
    
}