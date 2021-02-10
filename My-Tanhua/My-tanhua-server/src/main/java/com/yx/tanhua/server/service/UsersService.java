package com.yx.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yx.tanhua.common.enums.SexEnum;
import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.common.pojo.UserInfo;
import com.yx.tanhua.dubbo.server.api.UserLikeApi;
import com.yx.tanhua.dubbo.server.api.VisitorsApi;
import com.yx.tanhua.dubbo.server.pojo.UserLike;
import com.yx.tanhua.dubbo.server.pojo.Visitors;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import com.yx.tanhua.server.utils.UserThreadLocal;
import com.yx.tanhua.server.vo.CountsVo;
import com.yx.tanhua.server.vo.PageResult;
import com.yx.tanhua.server.vo.UserInfoVo;
import com.yx.tanhua.server.vo.UserLikeListVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsersService {
    
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RecommendUserService recommendUserService;
    
    @Reference(version = "1.0.0")
    private UserLikeApi userLikeApi;
    @Reference(version = "1.0.0")
    private VisitorsApi visitorsApi;
    
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
    
    /**
     * 互相喜欢 喜欢 粉丝 - 统计
     *
     * @return {@link CountsVo}
     */
    public CountsVo queryCounts() {
        // 获取当前用户
        User user = UserThreadLocal.get();
        
        Long eachLoveCount = this.userLikeApi.queryEachLikeCount(user.getId());
        Long fanCount = this.userLikeApi.queryFanCount(user.getId());
        Long loveCount = this.userLikeApi.queryLikeCount(user.getId());
        
        CountsVo countsVo = new CountsVo();
        countsVo.setEachLoveCount(eachLoveCount);
        countsVo.setFanCount(fanCount);
        countsVo.setLoveCount(loveCount);
        
        return countsVo;
    }
    
    /**
     * 互相关注 我关注 粉丝 谁看过我 - 翻页列表
     *
     * @param type
     *     1 互相关注 2 我关注 3 粉丝 4 谁看过我
     */
    public PageResult queryLikeList(Integer type, Integer page, Integer pageSize, String nickname) {
        // 获取当前用户
        User user = UserThreadLocal.get();
        // 初始化分页结果
        PageResult pageResult = new PageResult();
        pageResult.setPagesize(pageSize);
        pageResult.setPage(page);
        pageResult.setPages(0);
        pageResult.setCounts(0);
        
        // type: 1 互相关注 2 我关注 3 粉丝 4 谁看过我
        List<Long> userIds = getUserIds(type, page, pageSize, user);
        
        if (CollectionUtils.isEmpty(userIds)) {
            // 未查到返回默认结果
            return pageResult;
        }
        
        QueryWrapper<UserInfo> queryWrapper =
            new QueryWrapper<UserInfo>().in("user_id", userIds);
        // 附加查询条件
        if (StringUtils.isNotBlank(nickname)) {
            queryWrapper.like("nick_name", nickname);
        }
        // 查询UserInfo
        List<UserInfo> userInfoList = this.userInfoService.queryList(queryWrapper);
        
        List<UserLikeListVo> userLikeListVos = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            UserLikeListVo userLikeListVo = getLikeListVo(userInfo);
            
            double score = this.recommendUserService.queryScore(user.getId(), userInfo.getUserId());
            if (score == 0) {
                score = RandomUtils.nextDouble(30, 90);
            }
            userLikeListVo.setMatchRate((int) score);
            
            userLikeListVos.add(userLikeListVo);
        }
        
        pageResult.setItems(userLikeListVos);
        return pageResult;
    }
    
    /**
     * 代码抽取
     *
     * @param type
     *     1 互相关注 2 我关注 3 粉丝 4 谁看过我
     */
    private List<Long> getUserIds(Integer type, Integer page, Integer pageSize, User user) {
        List<Long> userIds = new ArrayList<>();
        switch (type) {
            case 1: {
                PageInfo<UserLike> pageInfo = this.userLikeApi.queryEachLikeList(user.getId(), page, pageSize);
                for (UserLike record : pageInfo.getRecords()) {
                    userIds.add(record.getUserId());
                }
                break;
            }
            case 2: {
                PageInfo<UserLike> pageInfo = this.userLikeApi.queryLikeList(user.getId(), page, pageSize);
                for (UserLike record : pageInfo.getRecords()) {
                    userIds.add(record.getLikeUserId());
                }
                break;
            }
            case 3: {
                PageInfo<UserLike> pageInfo = this.userLikeApi.queryFanList(user.getId(), page, pageSize);
                for (UserLike record : pageInfo.getRecords()) {
                    userIds.add(record.getUserId());
                }
                break;
            }
            case 4: {
                List<Visitors> visitors = this.visitorsApi.topVisitor(user.getId(), page, pageSize);
                for (Visitors visitor : visitors) {
                    userIds.add(visitor.getVisitorUserId());
                }
                break;
            }
            default:
                break;
        }
        return userIds;
    }
    
    /**
     * 代码抽取
     */
    private UserLikeListVo getLikeListVo(UserInfo userInfo) {
        UserLikeListVo userLikeListVo = new UserLikeListVo();
        userLikeListVo.setAge(userInfo.getAge());
        userLikeListVo.setAvatar(userInfo.getLogo());
        userLikeListVo.setCity(userInfo.getCity());
        userLikeListVo.setEducation(userInfo.getEdu());
        userLikeListVo.setGender(userInfo.getSex().name().toLowerCase());
        userLikeListVo.setId(userInfo.getUserId());
        userLikeListVo.setMarriage(StringUtils.equals(userInfo.getMarriage(), "已婚") ? 1 : 0);
        userLikeListVo.setNickname(userInfo.getNickName());
        return userLikeListVo;
    }
    
    /**
     * 取消喜欢
     *
     * @param userId 不喜欢的用户id
     */
    public void disLike(Long userId) {
        User user = UserThreadLocal.get();
        this.userLikeApi.deleteUserLike(user.getId(), userId);
    }
    
    /**
     * 关注粉丝
     *
     * @param userId 粉丝id
     */
    public void likeFan(Long userId) {
        User user = UserThreadLocal.get();
        this.userLikeApi.saveUserLike(user.getId(), userId);
    }
}