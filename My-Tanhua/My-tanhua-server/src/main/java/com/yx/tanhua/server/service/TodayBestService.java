package com.yx.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yx.tanhua.common.enums.SexEnum;
import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.common.pojo.UserInfo;
import com.yx.tanhua.dubbo.server.api.UserLikeApi;
import com.yx.tanhua.dubbo.server.api.UserLocationApi;
import com.yx.tanhua.dubbo.server.pojo.RecommendUser;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import com.yx.tanhua.dubbo.server.vo.UserLocationVo;
import com.yx.tanhua.server.pojo.Question;
import com.yx.tanhua.server.utils.UserThreadLocal;
import com.yx.tanhua.server.vo.NearUserVo;
import com.yx.tanhua.server.vo.PageResult;
import com.yx.tanhua.server.vo.RecommendUserQueryParam;
import com.yx.tanhua.server.vo.TodayBest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class TodayBestService {
    private static final ObjectMapper MAPPER = new ObjectMapper();
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
    
    @Value("${tanhua.sso.url}")
    private String ssoUrl;
    
    @Autowired
    private QuestionService questionService;
    @Autowired
    private RestTemplate restTemplate;
    @Reference(version = "1.0.0")
    private UserLikeApi userLikeApi;
    @Autowired
    private IMService imService;
    
    @Reference(version = "1.0.0")
    private UserLocationApi userLocationApi;
    
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
     * 探花-展现卡片数据
     *
     * @return {@link List<TodayBest>}
     */
    public List<TodayBest> queryCardsList() {
        // 获取当前用户
        User user = UserThreadLocal.get();
        // 分页查询尺寸 使用固定值50
        int count = 50;
        // 分页查询推荐用户
        PageInfo<RecommendUser> pageInfo =
            this.recommendUserService.queryRecommendUserList(user.getId(), 1, count);
        if (CollectionUtils.isEmpty(pageInfo.getRecords())) {
            // 未查到则使用默认推荐列表
            String[] ss = StringUtils.split(defaultRecommendUsers, ',');
            for (String s : ss) {
                RecommendUser recommendUser = new RecommendUser();
                recommendUser.setUserId(Long.valueOf(s));
                recommendUser.setToUserId(user.getId());
                pageInfo.getRecords().add(recommendUser);
            }
        }
        // 获取所有的推荐用户
        List<RecommendUser> records = pageInfo.getRecords();
        // 展示个数 最多10个
        int showCount = Math.min(10, records.size());
        // 从所有的推荐用户中随机选
        List<RecommendUser> newRecords = new ArrayList<>();
        for (int i = 0; i < showCount; i++) {
            // 随机选出推荐的好友
            newRecords.add(records.get(RandomUtils.nextInt(0, records.size() - 1)));
        }
        // 整理用户id
        Set<Long> userIds = new HashSet<>();
        for (RecommendUser record : newRecords) {
            userIds.add(record.getUserId());
        }
        
        // 查mysql补全用户信息
        QueryWrapper<UserInfo> queryWrapper =
            new QueryWrapper<UserInfo>().in("user_id", userIds);
        List<UserInfo> userInfos = this.userInfoService.queryList(queryWrapper);
        List<TodayBest> todayBests = new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            // 补全用户信息
            TodayBest todayBest = new TodayBest();
            todayBest.setId(userInfo.getUserId());
            setUserInfo(userInfo, todayBest);
            // todayBest.setAvatar(userInfo.getLogo());
            // todayBest.setNickname(userInfo.getNickName());
            // todayBest.setTags(StringUtils.split(userInfo.getTags(), ','));
            // todayBest.setGender(userInfo.getSex().getValue() == 1 ? "man" : "woman");
            // todayBest.setAge(userInfo.getAge());
            todayBest.setFateValue(0L);
            
            todayBests.add(todayBest);
        }
        
        return todayBests;
    }
    
    /**
     * 喜欢
     *
     * @param likeUserId
     *     对方的id
     */
    public Boolean likeUser(Long likeUserId) {
        // 获取当前用户
        User user = UserThreadLocal.get();
        // 远程调用 保存喜欢记录
        String id = this.userLikeApi.saveUserLike(user.getId(), likeUserId);
        if (StringUtils.isEmpty(id)) {
            return false;
        }
        // 检查是否是相互喜欢
        if (this.userLikeApi.isMutualLike(user.getId(), likeUserId)) {
            // 相互喜欢成为好友
            this.imService.contactUser(likeUserId);
        }
        return true;
    }
    
    /**
     * 不喜欢
     *
     * @param likeUserId
     *     对方的id
     */
    public Boolean disLikeUser(Long likeUserId) {
        User user = UserThreadLocal.get();
        return this.userLikeApi.deleteUserLike(user.getId(), likeUserId);
    }
    
    /**
     * 搜附近
     *
     * @param gender
     *     性别
     * @param distance
     *     距离
     *
     * @return {@link List<NearUserVo>}
     */
    public List<NearUserVo> queryNearUser(String gender, String distance) {
        // 获取当前用户
        User user = UserThreadLocal.get();
        // 查询当前用户的位置信息
        UserLocationVo userLocationVo = this.userLocationApi.queryByUserId(user.getId());
        Double longitude = userLocationVo.getLongitude();
        Double latitude = userLocationVo.getLatitude();
        
        // 根据当前用户的位置信息查询附近的好友
        List<UserLocationVo> userLocationList = this.userLocationApi
            .queryUserFromLocation(longitude, latitude, Integer.valueOf(distance));
        
        if (CollectionUtils.isEmpty(userLocationList)) {
            // 未查到附近的好友
            return Collections.emptyList();
        }
        
        // 打包用户id
        List<Long> userIds = new ArrayList<>();
        for (UserLocationVo locationVo : userLocationList) {
            userIds.add(locationVo.getUserId());
        }
        
        QueryWrapper<UserInfo> queryWrapper =
            new QueryWrapper<UserInfo>().in("user_id", userIds);
        if (StringUtils.equalsIgnoreCase(gender, "man")) {
            queryWrapper.in("sex", SexEnum.MAN);
        } else if (StringUtils.equalsIgnoreCase(gender, "woman")) {
            queryWrapper.in("sex", SexEnum.WOMAN);
        }
        // 查mysql获取用户信息
        List<UserInfo> userInfoList = this.userInfoService.queryList(queryWrapper);
        Map<Long, UserInfo> userInfoMap = new HashMap<>();
        userInfoList.forEach(userInfo -> userInfoMap.put(userInfo.getId(), userInfo));
        
        List<NearUserVo> nearUserVoList = new ArrayList<>();
        
        for (UserLocationVo locationVo : userLocationList) {
            
            if (locationVo.getUserId().longValue() == user.getId().longValue()) {
                // 排除自己
                continue;
            }
            
            UserInfo userInfo = userInfoMap.get(locationVo.getUserId());
            // 封装NearUserVo
            NearUserVo nearUserVo = new NearUserVo();
            nearUserVo.setUserId(userInfo.getUserId());
            nearUserVo.setAvatar(userInfo.getLogo());
            nearUserVo.setNickname(userInfo.getNickName());
    
            nearUserVoList.add(nearUserVo);
            /*
            for (UserInfo userInfo : userInfoList) {
                if (locationVo.getUserId().longValue() == userInfo.getUserId().longValue()) {
                    NearUserVo nearUserVo = new NearUserVo();
                
                    nearUserVo.setUserId(userInfo.getUserId());
                    nearUserVo.setAvatar(userInfo.getLogo());
                    nearUserVo.setNickname(userInfo.getNickName());
                
                    nearUserVoList.add(nearUserVo);
                    break;
                }
            }
            */
        }
        
        return nearUserVoList;
    }
    
    /**
     * 回复陌生人问题 发送消息给对方
     *
     * @param userId
     *     陌生人id
     * @param reply
     *     回复内容
     *
     * @return {@link Boolean}
     */
    public Boolean replyQuestion(Long userId, String reply) {
        // 获取当前用户信息
        User user = UserThreadLocal.get();
        UserInfo userInfo = this.userInfoService.queryById(user.getId());
        
        // 构建消息内容
        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", user.getId().toString());
        msg.put("nickname", userInfo.getNickName());
        msg.put("strangerQuestion", this.queryQuestion(userId));
        msg.put("reply", reply);
        
        try {
            // 消息内容转json
            String msgStr = MAPPER.writeValueAsString(msg);
            
            // 构造访问sso模块的url
            String targetUrl = this.ssoUrl + "/user/huanxin/messages";
            
            // 设置请求头 application/x-www-form-urlencoded
            // application/x-www-form-urlencoded:
            //   url编码
            //   数据被编码成以 '&' 分隔的键-值对, 同时以 '=' 分隔键和值.
            //   非字母或数字的字符会被 percent-encoding(用"%"编码)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // 构造请求参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("target", userId.toString());
            params.add("msg", msgStr);
            
            // 构造Http实体对象
            HttpEntity<MultiValueMap<String, String>> httpEntity =
                new HttpEntity<>(params, headers);
            
            // 发http请求
            ResponseEntity<Void> responseEntity =
                this.restTemplate.postForEntity(targetUrl, httpEntity, Void.class);
            
            return responseEntity.getStatusCodeValue() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * 查询问题
     *
     * @param userId
     *     用户id
     *
     * @return {@link String} 问题内容
     */
    public String queryQuestion(Long userId) {
        Question question = this.questionService.queryQuestion(userId);
        if (question != null) {
            return question.getTxt();
        }
        return "";
    }
    
    /**
     * 查询今日佳人详情
     *
     * @param userId
     *     佳人id
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
        if (score == 0) {
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
