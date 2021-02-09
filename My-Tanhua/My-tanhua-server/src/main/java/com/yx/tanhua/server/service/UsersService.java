package com.yx.tanhua.server.service;

import com.yx.tanhua.common.enums.SexEnum;
import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.common.pojo.UserInfo;
import com.yx.tanhua.server.utils.UserThreadLocal;
import com.yx.tanhua.server.vo.UserInfoVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsersService {
    
    @Autowired
    private UserInfoService userInfoService;
    
    /**
     * 查询用户信息
     */
    public UserInfoVo queryUserInfo(String userID, String huanxinID) {
        // 获取当前用户
        User user = UserThreadLocal.get();
        
        // 如果有传递userId 则使用传递的值
        Long userId = user.getId();
        if (StringUtils.isNotBlank(userID)) {
            userId = Long.valueOf(userID);
        } else if (StringUtils.isNotBlank(huanxinID)) {
            userId = Long.valueOf(huanxinID);
        }
        
        // 查mysql获取用户信息
        UserInfo userInfo = this.userInfoService.queryById(userId);
        if (userInfo == null) {
            return null;
        }
        
        // 封装userInfoVo对象
        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setAge(userInfo.getAge() != null ? userInfo.getAge().toString() : null);
        userInfoVo.setAvatar(userInfo.getLogo());
        userInfoVo.setBirthday(userInfo.getBirthday());
        userInfoVo.setEducation(userInfo.getEdu());
        userInfoVo.setCity(userInfo.getCity());
        userInfoVo.setGender(userInfo.getSex().name().toLowerCase());
        userInfoVo.setId(userInfo.getUserId());
        userInfoVo.setIncome(userInfo.getIncome() + "K");
        userInfoVo.setMarriage(StringUtils.equals(userInfo.getMarriage(), "已婚") ? 1 : 0);
        userInfoVo.setNickname(userInfo.getNickName());
        userInfoVo.setProfession(userInfo.getIndustry());
        
        return userInfoVo;
    }
    
    /**
     * 更新用户信息
     */
    public Boolean updateUserInfo(UserInfoVo userInfoVo) {
        User user = UserThreadLocal.get();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setNickName(userInfoVo.getNickname());
        userInfo.setAge(Integer.valueOf(userInfoVo.getAge()));
        userInfo.setSex(StringUtils.equalsIgnoreCase(userInfoVo.getGender(), "man") ? SexEnum.MAN : SexEnum.WOMAN);
        userInfo.setBirthday(userInfoVo.getBirthday());
        userInfo.setCity(userInfoVo.getCity());
        userInfo.setEdu(userInfoVo.getEducation());
        userInfo.setIncome(StringUtils.replaceAll(userInfoVo.getIncome(), "K", ""));
        userInfo.setIndustry(userInfoVo.getProfession());
        userInfo.setMarriage(userInfoVo.getMarriage() == 1 ? "已婚" : "未婚");
        return this.userInfoService.updateUserInfoByUserId(userInfo);
    }
}