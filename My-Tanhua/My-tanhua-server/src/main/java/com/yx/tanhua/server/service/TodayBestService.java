package com.yx.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.common.pojo.UserInfo;
import com.yx.tanhua.dubbo.server.pojo.RecommendUser;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import com.yx.tanhua.server.utils.UserThreadLocal;
import com.yx.tanhua.server.vo.PageResult;
import com.yx.tanhua.server.vo.RecommendUserQueryParam;
import com.yx.tanhua.server.vo.TodayBest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TodayBestService {
    @Autowired
    private UserService userService;
    
    @Autowired
    private RecommendUserService recommendUserService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    /**
     * 默认推荐的用户id
     * <p>
     * 在配置文件中手动配置
     */
    @Value("${tanhua.sso.default.user}")
    private Long defaultUser;
    /**
     * 默认推荐的用户列表id
     * <p>
     * 在配置文件中手动配置
     */
    @Value("tanhua.sso.default.recommend.users")
    private String defaultRecommendUsers;
    
    /**
     * 查询推荐用户(今日佳人)信息
     *
     * @param token
     *     token
     *
     * @return {@link TodayBest} 今日佳人信息
     *     <p>
     *     查询失败返回null
     *     <p>
     *     没有信息则返回默认推荐
     */
    public TodayBest queryTodayBest(String token) {
        // 校验token是否有效，通过SSO的接口进行校验
        User user = this.userService.queryUserByToken(token);
        if (null == user) {
            log.debug("token无效~ token = " + token);
            // token非法或已经过期
            return null;
        }
        
        // 查询Mongodb获取推荐用户的id
        TodayBest todayBest = this.recommendUserService.queryTodayBest(user.getId());
        if (null == todayBest) {
            log.debug("查询今日佳人为null, 使用默认推荐 ~");
            // 设置默认的推荐用户
            todayBest = new TodayBest();
            todayBest.setId(defaultUser);
            // 缘分值设置为固定值
            todayBest.setFateValue(80L);
        }
        
        // 查询mysql补全推荐用户的个人信息
        UserInfo userInfo = this.userInfoService.queryUserInfoByUserId(todayBest.getId());
        if (null == userInfo) {
            log.debug("推荐用户详细信息为null ~");
            return null;
        }
        // 补全信息
        setUserInfo(userInfo, todayBest);
        
        return todayBest;
    }
    
    /**
     * 重复代码抽取
     * <p>
     * 将{@link UserInfo}的信息封装到{@link TodayBest}
     * <p>
     * {@code Avatar Nickname Tags Gender Age}
     */
    private void setUserInfo(UserInfo userInfo, TodayBest todayBest) {
        todayBest.setAvatar(userInfo.getLogo());
        todayBest.setNickname(userInfo.getNickName());
        todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));
        todayBest.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
        todayBest.setAge(userInfo.getAge());
    }
    
    /**
     * 查询今日佳人详情
     *
     * @param userId 佳人id
     *
     * @return {@link TodayBest}
     */
    public TodayBest queryTodayBest(Long userId) {
        // 获取当前用户
        User user = UserThreadLocal.get();
    
        TodayBest todayBest = new TodayBest();
        // 查询mysql补全信息
        UserInfo userInfo = this.userInfoService.queryById(userId);
        todayBest.setId(userId);
        todayBest.setAge(userInfo.getAge());
        todayBest.setAvatar(userInfo.getLogo());
        todayBest.setGender(userInfo.getSex().name().toLowerCase());
        todayBest.setNickname(userInfo.getNickName());
        todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));
        // 远程调用 查询mongodb获取缘分值
        double score = this.recommendUserService.queryScore(userId, user.getId());
        if(score == 0){
            // 查询失败 使用默认分值
            score = 98;
        }
        // 填充缘分值
        todayBest.setFateValue(Double.valueOf(score).longValue());
    
        return todayBest;
    }
    
    /**
     * 查询推荐用户列表
     *
     * @param queryParam
     *     查询参数封装
     * @param token
     *     token
     *
     * @return 分页结果封装
     */
    public PageResult queryRecommendation(String token, RecommendUserQueryParam queryParam) {
        // 校验token是否有效，通过SSO的接口进行校验
        User user = this.userService.queryUserByToken(token);
        if (null == user) {
            // token非法或已经过期
            return null;
        }
        
        // 封装结果
        PageResult pageResult = new PageResult();
        pageResult.setPage(queryParam.getPage());
        pageResult.setPagesize(queryParam.getPagesize());
        
        // 查询Mongodb获取推荐用户列表 仅携带id
        PageInfo<RecommendUser> pageInfo = this.recommendUserService
            .queryRecommendUserList(user.getId(), queryParam.getPage(), queryParam.getPagesize());
        if (pageInfo == null) {
            // 查询失败
            return pageResult;
        }
        // 取用户列表
        List<RecommendUser> records = pageInfo.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            // 没有查询到推荐列表 使用默认推荐列表
            String[] ss = StringUtils.split(defaultRecommendUsers, ',');
            for (String s : ss) {
                // 封装recommendUser对象
                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setUserId(Long.valueOf(s));
                recommendUser.setToUserId(user.getId());
                // 使用随机得分
                recommendUser.setScore(RandomUtils.nextDouble(70, 99));
                
                records.add(recommendUser);
            }
        }
        
        // 收集推荐用户的id
        Set<Long> userIds = new HashSet<>();
        for (RecommendUser record : records) {
            userIds.add(record.getUserId());
        }
        
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        // 以用户id为条件查询所有推荐用户信息
        // WHERE user_id in #{userIds}
        queryWrapper.in("user_id", userIds);
        
        // todo 开启条件过滤
         /*if (StringUtils.isNotEmpty(queryParam.getGender())) {
             // 需要性别参数查询
             // WHERE sex = ?
             queryWrapper.eq("sex", StringUtils.equals(queryParam.getGender(), "man") ? 1 : 2);
         }*/
         /*if (StringUtils.isNotEmpty(queryParam.getCity())) {
             // 需要城市参数查询
             // WHERE city like '%?%'
             queryWrapper.like("city", queryParam.getCity());
         }*/
         /*if (queryParam.getAge() != null) {
             // 设置年龄参数，条件：小于等于
             // WHERE age <= ?
             queryWrapper.le("age", queryParam.getAge());
         }*/
        // 去Mysql查询符合条件的用户信息
        List<UserInfo> userInfoList = this.userInfoService.queryUserInfoList(queryWrapper);
        if (CollectionUtils.isEmpty(userInfoList)) {
            // 没有查询到用户的基本信息
            return pageResult;
        }
        
        // 结合过滤条件筛选推荐用户
        List<TodayBest> todayBests = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            for (RecommendUser record : records) {
                // 推荐用户列表中的id == 用户信息列表中的id
                if (record.getUserId().longValue() == userInfo.getUserId().longValue()) {
                    // 封装推荐用户信息(从Mysql的返回对象中获取)
                    TodayBest todayBest = new TodayBest();
                    todayBest.setId(userInfo.getUserId());
                    setUserInfo(userInfo, todayBest);
                    // 封装缘分值(从Mongodb的返回对象中获取)
                    double score = Math.floor(record.getScore());//取整,98.2 -> 98
                    todayBest.setFateValue(Double.valueOf(score).longValue());
                    // 添加到列表中
                    todayBests.add(todayBest);
                    break;
                }
            }
        }
        
        // 按照缘分值进行倒序排序
        todayBests.sort((o1, o2) -> new Long(
            o2.getFateValue() - o1.getFateValue()).intValue());
        
        // 封装处理后的s推荐用户列表
        pageResult.setItems(todayBests);
        return pageResult;
    }
}
