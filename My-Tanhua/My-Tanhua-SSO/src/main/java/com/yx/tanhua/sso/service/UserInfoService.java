package com.yx.tanhua.sso.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yx.tanhua.common.enums.SexEnum;
import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.common.pojo.UserInfo;
import com.yx.tanhua.common.service.PicUploadService;
import com.yx.tanhua.sso.mapper.UserInfoMapper;
import com.yx.tanhua.common.vo.PicUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@Slf4j
public class UserInfoService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserInfoMapper userInfoMapper;
    
    /**
     * 人脸识别Service
     */
    @Autowired
    private FaceEngineService faceEngineService;
    
    /**
     * 图片上传Service
     */
    @Autowired
    private PicUploadService picUploadService;
    
    /**
     * 保存用户信息
     *
     * @param param
     *     封装用户信息的json字符串
     * @param token
     *     token
     *
     * @return {@link Boolean} 是否操作成功
     */
    public Boolean saveUserInfo(Map<String, String> param, String token) {
        // 通过token获取用户 以此校验token是否合法
        User user = this.userService.queryUserByToken(token);
        if (null == user) {
            // 未能获取到用户
            return false;
        }
        // 封装用户信息对象
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setSex(StringUtils.equalsIgnoreCase(param.get("gender"), "man")
                            ? SexEnum.MAN : SexEnum.WOMAN);
        userInfo.setNickName(param.get("nickname"));
        userInfo.setBirthday(param.get("birthday"));
        userInfo.setCity(param.get("city"));
        
        // 调用dao插入数据
        return this.userInfoMapper.insert(userInfo) == 1;
    }
    
    /**
     * 保存用户头像
     *
     * @param file
     *     头像文件
     * @param token
     *     token
     *
     * @return {@link Boolean} 是否操作成功
     */
    public Boolean saveUserLogo(MultipartFile file, String token) {
        // 通过token查询用户
        User user = this.userService.queryUserByToken(token);
        if (null == user) {
            return false;
        }
        
        try {
            //校验图片是否是人像
            boolean b = this.faceEngineService.checkIsPortrait(file.getBytes());
            log.debug("校验图片是否是人像: " + b);
            if (!b) {
                // 不是人像 校验不通过
                // todo 启用人脸校验
                // return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 图片上传到阿里云OSS
        PicUploadResult result = this.picUploadService.upload(file);
        if (StringUtils.isEmpty(result.getName())) {
            // 上传失败
            return false;
        }
        
        // 把头像保存到用户信息中
        UserInfo userInfo = new UserInfo();
        userInfo.setLogo(result.getName());
        // user_id作为条件更新数据
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId());
        // 调用dao
        return this.userInfoMapper.update(userInfo, queryWrapper) == 1;
    }
}
