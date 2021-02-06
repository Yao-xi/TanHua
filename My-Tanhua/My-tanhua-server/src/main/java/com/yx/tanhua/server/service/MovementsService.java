package com.yx.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.common.pojo.UserInfo;
import com.yx.tanhua.common.service.PicUploadService;
import com.yx.tanhua.common.vo.PicUploadResult;
import com.yx.tanhua.dubbo.server.api.QuanZiApi;
import com.yx.tanhua.dubbo.server.api.VisitorsApi;
import com.yx.tanhua.dubbo.server.pojo.Publish;
import com.yx.tanhua.dubbo.server.pojo.Visitors;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import com.yx.tanhua.server.utils.RelativeDateFormat;
import com.yx.tanhua.server.utils.UserThreadLocal;
import com.yx.tanhua.server.vo.Movements;
import com.yx.tanhua.server.vo.PageResult;
import com.yx.tanhua.server.vo.VisitorsVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class MovementsService {
    
    /**
     * 远程注入
     */
    @SuppressWarnings("unused")
    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;
    
    @Reference(version = "1.0.0")
    private VisitorsApi visitorsApi;
    
    /**
     * 图片上传业务
     */
    @Autowired
    private PicUploadService picUploadService;
    @Autowired
    private UserInfoService userInfoService;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 发布动态 保存发布信息
     *
     * @param textContent
     *     文字动态
     * @param location
     *     位置
     * @param multipartFile
     *     图片动态
     * @param latitude
     *     纬度
     * @param longitude
     *     经度
     *
     * @return PublishId
     */
    public String savePublish(String textContent,
                              String location,
                              String latitude,
                              String longitude,
                              MultipartFile[] multipartFile) {
        
        // 查询当前的登录信息
        User user = UserThreadLocal.get();
        if (null == user) {
            return null;
        }
        
        // 封装发布对象
        Publish publish = new Publish();
        publish.setUserId(user.getId());    // 用户id
        publish.setText(textContent);       // 动态文本
        publish.setLocationName(location);  // 坐标位置
        publish.setLatitude(latitude);      // 纬度
        publish.setLongitude(longitude);    // 经度
        publish.setSeeType(1);              // 可查看性
        
        // 图片动态处理
        //  - 上传图片到OSS
        //  - 把url封装到发布对象中
        //  - 图片不唯一
        List<String> picUrls = new ArrayList<>();
        for (MultipartFile file : multipartFile) {
            // 图片上传到OSS
            PicUploadResult picUploadResult = this.picUploadService.upload(file);
            picUrls.add(picUploadResult.getName());
        }
        // 封装url到发布对象
        publish.setMedias(picUrls);
        // 远程调用 保存数据
        return quanZiApi.savePublish(publish);
    }
    
    /**
     * 点赞
     *
     * @param publishId
     *     发布id
     *
     * @return {@link Long} 点赞数
     */
    public Long likeComment(String publishId) {
        // 获取当前用户信息
        User user = UserThreadLocal.get();
        // 远程调用 点赞
        boolean success = this.quanZiApi.saveLikeComment(user.getId(), publishId);
        if (!success) {
            // 调用失败
            return null;
        }
        
        // 点赞数
        Long likeCount = 0L;
        
        // 保存点赞数到redis
        String key = "QUANZI_COMMENT_LIKE_" + publishId;
        if (Boolean.FALSE.equals(this.redisTemplate.hasKey(key))) {
            // 缓存中没有值的话 远程调用 查询点赞数
            likeCount = this.quanZiApi.queryCommentCount(publishId, 1);
            // 写入缓存
            this.redisTemplate.opsForValue().set(key, String.valueOf(likeCount));
        } else {
            // 缓存中的值自增
            // (之前远程调用保存操作的时候已经修改过数据库中的数据了 这里可以直接自增)
            likeCount = this.redisTemplate.opsForValue().increment(key);
        }
        
        // 在缓存中记录用户是否已点赞
        String userKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" + publishId;
        this.redisTemplate.opsForValue().set(userKey, "1");
        
        // 返回点赞数
        return likeCount;
    }
    
    /**
     * 取消点赞
     *
     * @param publishId
     *     发布id
     *
     * @return {@link Long} 点赞数
     */
    public Long cancelLikeComment(String publishId) {
        // 获取当前用户信息
        User user = UserThreadLocal.get();
        // 远程调用 取消点赞
        boolean success = this.quanZiApi.removeComment(user.getId(), publishId, 1);
        if (success) {
            // 取消点赞成功
            // 缓存中记录的数量递减
            String key = "QUANZI_COMMENT_LIKE_" + publishId;
            Long likeCount = this.redisTemplate.opsForValue().decrement(key);
            
            // 缓存中删除已点赞的记录
            String userKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" + publishId;
            this.redisTemplate.delete(userKey);
            // 返回点赞数
            return likeCount;
        }
        return null;
    }
    
    /**
     * 喜欢
     * <p>
     * 业务逻辑同点赞一致
     *
     * @param publishId
     *     发布id
     *
     * @return {@link Long} 喜欢数
     */
    public Long loveComment(String publishId) {
        User user = UserThreadLocal.get();
        boolean success = this.quanZiApi.saveLoveComment(user.getId(), publishId);
        if (!success) {
            return null;
        }
        
        Long loveCount = 0L;
        
        //保存喜欢数到redis
        String key = "QUANZI_COMMENT_LOVE_" + publishId;
        if (Boolean.FALSE.equals(this.redisTemplate.hasKey(key))) {
            loveCount = this.quanZiApi.queryCommentCount(publishId, 3);
            this.redisTemplate.opsForValue().set(key, String.valueOf(loveCount));
        } else {
            loveCount = this.redisTemplate.opsForValue().increment(key);
        }
        
        //记录已喜欢
        String userKey = "QUANZI_COMMENT_LOVE_USER_" + user.getId() + "_" + publishId;
        this.redisTemplate.opsForValue().set(userKey, "1");
        
        return loveCount;
    }
    
    /**
     * 取消喜欢
     * <p>
     * 业务逻辑同取消点赞一致
     *
     * @param publishId
     *     发布id
     *
     * @return {@link Long} 喜欢数
     */
    public Long cancelLoveComment(String publishId) {
        User user = UserThreadLocal.get();
        boolean success = this.quanZiApi.removeComment(user.getId(), publishId, 3);
        if (success) {
            String key = "QUANZI_COMMENT_LOVE_" + publishId;
            //数量递减
            Long loveCount = this.redisTemplate.opsForValue().decrement(key);
            
            //删除已喜欢
            String userKey = "QUANZI_COMMENT_LOVE_USER_" + user.getId() + "_" + publishId;
            this.redisTemplate.delete(userKey);
            
            return loveCount;
        }
        return null;
    }
    
    /**
     * 查询单条动态详细内容
     *
     * @param publishId
     *     发布id
     *
     * @return {@link Movements} 动态详细内容的封装
     */
    public Movements queryById(String publishId) {
        // 远程调用 查询
        Publish publish = this.quanZiApi.queryPublishById(publishId);
        if (null == publish) {
            return null;
        }
        // 封装movements对象
        Movements movements = getMovements(publish);
        // 查用户的详细信息 因为需要发布者的数据
        UserInfo userInfo = this.userInfoService.queryUserInfoByUserId(publish.getUserId());
        if (null == userInfo) {
            return null;
        }
        // 继续封装movements对象
        this.fillValueToMovements(movements, userInfo);
        
        return movements;
    }
    
    /**
     * 重复代码抽取
     * <p>
     * 构造 {@link Movements} 对象
     * <p>
     * 并填充 id 文本 媒体数据 用户id 发布时间
     */
    private Movements getMovements(Publish publish) {
        Movements movements = new Movements();
        movements.setId(publish.getId().toHexString()); // 设置id
        movements.setImageContent(publish.getMedias().toArray(new String[]{})); // 媒体数据，图片或小视频
        movements.setTextContent(publish.getText()); // 动态文本
        movements.setUserId(publish.getUserId()); // 设置该动态的用户id
        movements.setCreateDate(RelativeDateFormat.format(new Date(publish.getCreated()))); // 发布时间 10分钟前
        return movements;
    }
    
    /**
     * 重复代码抽取
     * <p>
     * 填充movement的数据
     * <p>
     * 把用户信息 点赞 喜欢 评论 等数据 都封装到movements中
     *
     * @param movements
     *     动态详细信息对象
     * @param userInfo
     *     用户信息对象
     */
    private void fillValueToMovements(Movements movements, UserInfo userInfo) {
        // -------- 填充用户信息数据 --------
        fillUserInfoToMovements(movements, userInfo);
        
        movements.setDistance("1.2公里"); // todo 距离
        
        // -------- 填充评论相关数据 --------
        fillCommentToMovements(movements);
        
        // -------- 填充点赞相关数据 --------
        /*
        // 查询是否点赞
        String userKey = "QUANZI_COMMENT_LIKE_USER_" + userInfo.getUserId() + "_" + movements.getId();
        //noinspection ConstantConditions
        movements.setHasLiked(this.redisTemplate.hasKey(userKey) ? 1 : 0); //是否点赞（1是，0否）
        // 计算点赞数
        String likeCount = this.redisTemplate.opsForValue()
            .get("QUANZI_COMMENT_LIKE_" + movements.getId());
        //noinspection ConstantConditions
        if (StringUtils.isNotEmpty(likeCount) && likeCount.matches("^[0-9]*$")) {
            movements.setLikeCount(Integer.valueOf(likeCount)); //点赞数
        } else {
            movements.setLikeCount(0);
        }*/
        fillLikeToMovements(movements, userInfo.getUserId());
        
        // -------- 填充喜欢相关数据 --------
        /*
        // 查询是否喜欢
        String userLoveKey = "QUANZI_COMMENT_LOVE_USER_" + userInfo.getUserId() + "_" + movements.getId();
        //noinspection ConstantConditions
        movements.setHasLoved(this.redisTemplate.hasKey(userLoveKey) ? 1 : 0); //是否喜欢（1是，0否）
        // 计算喜欢数
        String loveCount = this.redisTemplate.opsForValue()
            .get("QUANZI_COMMENT_LOVE_" + movements.getId());
        //noinspection ConstantConditions
        if (StringUtils.isNotEmpty(loveCount) && loveCount.matches("^[0-9]*$")) {
            movements.setLoveCount(Integer.valueOf(loveCount)); //喜欢数
        } else {
            movements.setLoveCount(0);
        }*/
        fillLoveToMovements(movements, userInfo.getUserId());
    }
    
    /**
     * 代码块抽取
     * <p>
     * 填充用户信息数据
     */
    private void fillUserInfoToMovements(Movements movements, UserInfo userInfo) {
        // 填充年龄
        movements.setAge(userInfo.getAge());
        // 填充头像
        movements.setAvatar(userInfo.getLogo());
        // 填充性别
        movements.setGender(userInfo.getSex().name().toLowerCase());
        // 填充昵称
        movements.setNickname(userInfo.getNickName());
        // 填充标签
        movements.setTags(StringUtils.split(userInfo.getTags(), ','));
    }
    
    /**
     * 代码块抽取
     * <p>
     * 设置评论数
     * <p>
     * 业务逻辑同点赞
     */
    private void fillCommentToMovements(Movements movements) {
        String key = "QUANZI_COMMENT_" + movements.getId();
        String commentCount = this.redisTemplate.opsForValue().get(key);
        //noinspection ConstantConditions
        if (StringUtils.isNotEmpty(commentCount) && commentCount.matches("^[0-9]*$")) {
            movements.setCommentCount(Integer.valueOf(commentCount));
        } else {
            // 没查到的话先查后写缓存
            Long count = quanZiApi.queryCommentCount(movements.getId(), 2);
            redisTemplate.opsForValue().set(key, String.valueOf(count));
            movements.setCommentCount(Math.toIntExact(count));
        }
    }
    
    /**
     * 代码块抽取
     * <p>
     * 设置点赞
     */
    private void fillLikeToMovements(Movements movements, Long userId) {
        //4.2 查询redis中 "我已点赞"记录
        String likeUserKey = "QUANZI_COMMENT_LIKE_USER_" + userId + "_" + movements.getId();
        //noinspection ConstantConditions
        movements.setHasLiked(redisTemplate.hasKey(likeUserKey) ? 1 : 0);  // 是否点赞(1是,0否)
        
        //4.3 查询redis中 该动态的"点赞数量"
        String likeKey = "QUANZI_COMMENT_LIKE_" + movements.getId();
        String likeCountStr = redisTemplate.opsForValue().get(likeKey);
        //4.4 判断如果redis中的数据不为null 并且是数字的话
        //noinspection ConstantConditions
        if (StringUtils.isNotEmpty(likeCountStr) && likeCountStr.matches("^[0-9]*$")) {
            // 点赞数
            movements.setLikeCount(Integer.parseInt(likeCountStr));
        } else {
            movements.setLikeCount(0);
        }
    }
    
    /**
     * 代码块抽取
     * <p>
     * 设置喜欢
     */
    private void fillLoveToMovements(Movements movements, Long userId) {
        //4.5 是否喜欢(1是,0否)
        String loveUserKey = "QUANZI_COMMENT_LOVE_USER_" + userId + "_" + movements.getId();
        //noinspection ConstantConditions
        movements.setHasLoved(redisTemplate.hasKey(loveUserKey) ? 1 : 0);
        
        //4.6 喜欢数
        String loveKey = "QUANZI_COMMENT_LOVE_" + movements.getId();
        String loveCountStr = redisTemplate.opsForValue().get(loveKey);
        //noinspection ConstantConditions
        if (StringUtils.isNotEmpty(loveCountStr) && loveCountStr.matches("^[0-9]*$")) {
            movements.setLoveCount(Integer.parseInt(loveCountStr));
        } else {
            movements.setLoveCount(0);
        }
    }
    
    /**
     * 查看自己的所有动态
     */
    public PageResult queryAlbumList(Long userId, Integer page, Integer pageSize) {
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPagesize(pageSize);
        // 远程调用查询动态发布列表
        PageInfo<Publish> albumPageInfo = this.quanZiApi.queryAlbumList(userId, page, pageSize);
        
        List<Long> userIds = new ArrayList<>();
        List<Movements> movementsList = new ArrayList<>();
        if (getPageInfoUserIdsMovementsList(albumPageInfo, userIds, movementsList)) {
            return pageResult;
        }
        
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        List<UserInfo> userInfos = this.userInfoService.queryList(queryWrapper);
        Map<Long, UserInfo> userInfoMap = new HashMap<>();
        userInfos.forEach(userInfo -> userInfoMap.put(userInfo.getId(), userInfo));
        
        for (Movements movements : movementsList) {
            this.fillValueToMovements(movements, userInfoMap.get(movements.getUserId()));
        }
        
        pageResult.setItems(movementsList);
        
        return pageResult;
    }
    
    /**
     * 重复代码抽取
     */
    private boolean getPageInfoUserIdsMovementsList(PageInfo<Publish> pageInfo, List<Long> userIds,
                                                    List<Movements> movementsList) {
        // 获取好友动态内容的List对象
        List<Publish> records = pageInfo.getRecords();
        
        if (CollectionUtils.isEmpty(records)) {
            //没有动态信息
            return true;
        }
        
        
        for (Publish record : records) {
            // 构造并封装对象
            Movements movements = getMovements(record);
            // 添加进列表
            movementsList.add(movements);
        }
        
        
        for (Movements movements : movementsList) {
            if (!userIds.contains(movements.getUserId())) {
                userIds.add(movements.getUserId());
            }
        }
        return false;
    }
    
    public List<VisitorsVo> queryVisitorsList() {
        // 获取当前用户
        User user = UserThreadLocal.get();
        
        /*
         * 如果redis中存在上次查询的时间
         * 就按照这个时间之后查询
         * 如果没有就查询前5个
         **/
        
        List<Visitors> visitors = null;
        // 查redis
        String redisKey = "visitor_date_" + user.getId();
        String value = this.redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isEmpty(value)) {
            // 缓存中没有就查询前5个
            visitors = this.visitorsApi.topVisitor(user.getId(), 5);
        } else {
            // 缓存中存在上次查询的时间
            //noinspection ConstantConditions
            visitors = this.visitorsApi.topVisitor(user.getId(), Long.valueOf(value));
        }
        
        // 更新查询时间 (在用户点击查看访客详情的时候更新 URI:/users/friends/4)
        // this.redisTemplate.opsForValue().set(redisKey, String.valueOf(System.currentTimeMillis()));
        
        if (CollectionUtils.isEmpty(visitors)) {
            // 查询结果为空
            return Collections.emptyList();
        }
        
        // 补充查询结果缺少的字段
        List<Long> userIds = new ArrayList<>();
        for (Visitors visitor : visitors) {
            userIds.add(visitor.getVisitorUserId());
        }
        
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("user_id", userIds);
        // 查询mysql
        List<UserInfo> userInfoList = this.userInfoService.queryList(queryWrapper);
        // 用Map优化双层for
        Map<Long, UserInfo> userInfoMap = new HashMap<>();
        userInfoList.forEach(userInfo -> userInfoMap.put(userInfo.getId(), userInfo));
        
        List<VisitorsVo> visitorsVoList = new ArrayList<>();
        
        /*for (Visitors visitor : visitors) {
            for (UserInfo userInfo : userInfoList) {
                if (visitor.getVisitorUserId().longValue() == userInfo.getUserId().longValue()) {
                    
                    VisitorsVo visitorsVo = new VisitorsVo();
                    visitorsVo.setAge(userInfo.getAge());
                    visitorsVo.setAvatar(userInfo.getLogo());
                    visitorsVo.setGender(userInfo.getSex().name().toLowerCase());
                    visitorsVo.setId(userInfo.getUserId());
                    visitorsVo.setNickname(userInfo.getNickName());
                    visitorsVo.setTags(StringUtils.split(userInfo.getTags(), ','));
                    visitorsVo.setFateValue(visitor.getScore().intValue());
                    
                    visitorsVoList.add(visitorsVo);
                    break;
                }
            }
        }*/
        for (Visitors visitor : visitors) {
            // 封装visitorsVo对象
            VisitorsVo visitorsVo = new VisitorsVo();
            UserInfo userInfo = userInfoMap.get(visitor.getVisitorUserId());
            visitorsVo.setAge(userInfo.getAge());
            visitorsVo.setAvatar(userInfo.getLogo());
            visitorsVo.setGender(userInfo.getSex().name().toLowerCase());
            visitorsVo.setId(userInfo.getUserId());
            visitorsVo.setNickname(userInfo.getNickName());
            visitorsVo.setTags(StringUtils.split(userInfo.getTags(), ','));
            visitorsVo.setFateValue(visitor.getScore().intValue());
            visitorsVoList.add(visitorsVo);
        }
        
        return visitorsVoList;
    }
    
    /**
     * 查询动态 (已废弃)
     * <p>
     * 参考{@link MovementsService#queryPublishList(User, Integer, Integer)}
     *
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     * @param isRecommend
     *     是否查询推荐动态
     *
     * @return {@link PageResult}
     */
    @Deprecated
    public PageResult queryPublishList(Integer page, Integer pageSize, boolean isRecommend) {
        
        // 获取当前登录的用户信息
        User user = UserThreadLocal.get();
        
        // 最终的返回值
        PageResult pageResult = new PageResult();
        // 封装基本的分页信息
        pageResult.setPagesize(pageSize);
        pageResult.setPage(page);
        pageResult.setCounts(0);
        pageResult.setPages(0);
        
        PageInfo<Publish> pageInfo;
        if (isRecommend) {
            // 远程调用 查询推荐动态的分页数据
            pageInfo = this.quanZiApi.queryPublishList(null, page, pageSize);
        } else {
            // 远程调用 查询好友动态的分页数据
            pageInfo = this.quanZiApi.queryPublishList(user.getId(), page, pageSize);
        }
        
        
        // 获取发布动态的好友id列表
        List<Long> userIds = new ArrayList<>();
        // 获取动态的详细内容对象列表
        List<Movements> movementsList = new ArrayList<>();
        if (getPageInfoUserIdsMovementsList(pageInfo, userIds, movementsList)) {
            return pageResult;
        }
        
        // 构造查询条件
        QueryWrapper<UserInfo> queryWrapper =
            new QueryWrapper<UserInfo>().in("user_id", userIds);
        // 查询Mysql获取用户的详细信息
        List<UserInfo> userInfos = this.userInfoService.queryUserInfoList(queryWrapper);
        for (Movements movements : movementsList) {
            for (UserInfo userInfo : userInfos) {
                if (movements.getUserId().longValue() == userInfo.getUserId().longValue()) {
                    // 封装对象
                    fillValueToMovements(movements, userInfo);
                    /*
                    movements.setAge(userInfo.getAge());
                    movements.setAvatar(userInfo.getLogo());
                    movements.setGender(userInfo.getSex().name().toLowerCase());
                    movements.setNickname(userInfo.getNickName());
                    movements.setTags(StringUtils.split(userInfo.getTags(), ','));
                    movements.setDistance("1.2公里");
                    
                    // 封装评论数据
                    movements.setCommentCount(10);
                    
                    // 封装点赞数据
                    String userKey = "QUANZI_COMMENT_LIKE_USER_" + user.getId() + "_" + movements.getId();
                    movements.setHasLiked(Boolean.TRUE.equals(
                        this.redisTemplate.hasKey(userKey)) ? 1 : 0);// 是否点赞 (1是 0否)
                    
                    movements.setLikeCount(100); // 点赞数
                    
                    // 封装喜欢数据
                    movements.setHasLoved(0);
                    movements.setLoveCount(80);
                    */
                    break;
                }
            }
        }
        
        // 封装返回值
        pageResult.setItems(movementsList);
        return pageResult;
    }
    
    /**
     * 分页查询-推荐动态
     *
     * @param page
     *     页码
     * @param pageSize
     *     每页显示个数
     *
     * @return 动态列表
     */
    public PageResult queryRecommendPublishList(Integer page, Integer pageSize) {
        return queryPublishList(null, page, pageSize);
    }
    
    /**
     * 动态列表
     *
     * @param user
     *     当前用户,如果为null,则查询"推荐动态"
     * @param page
     *     页码
     * @param pageSize
     *     每页显示个数
     *
     * @return {@link PageResult} 分页查询结果
     */
    @SuppressWarnings("DuplicatedCode")
    private PageResult queryPublishList(User user, Integer page, Integer pageSize) {
        // 1.创建分页查询条件
        PageResult pageResult = new PageResult();
        pageResult.setCounts(0);    //默认总条数为0
        pageResult.setPages(0);     //默认总页数为0
        pageResult.setPagesize(pageSize);
        pageResult.setPage(page);
        
        PageInfo<Publish> pageInfo = null;
        
        // 2.获取用户ID
        Long userId = null; // 默认没有用户id 即查询的是"推荐动态"
        
        // user == null 表示查询的是推荐
        // user != null 表示查询好友动态 此时pageInfo暂时为null
        if (user == null) {
            // 查询redis中推荐动态
            String key = "QUANZI_PUBLISH_RECOMMEND_" + UserThreadLocal.get().getId();
            String value = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotEmpty(value)) {
                // ------ 缓存命中 ------
                // 处理value value="10091,10092,10093,10094"
                String[] pids = StringUtils.split(value, ',');
                // 计算分页后的开始索引
                int startIndex = (page - 1) * pageSize;
                // 防止索引越界
                if (startIndex < pids.length && startIndex >= 0) {
                    // 计算结束索引
                    int endIndex = startIndex + pageSize - 1;
                    // 防止索引越界
                    if (endIndex >= pids.length) {
                        endIndex = pids.length - 1;
                    }
                    // 构造list集合存储pid
                    List<Long> pidList = new ArrayList<>();
                    for (int i = startIndex; i <= endIndex; i++) {
                        pidList.add(Long.valueOf(pids[i]));
                    }
                    // 远程调用 批量查询pid对应的动态
                    List<Publish> publishList = this.quanZiApi.queryPublishByPids(pidList);
                    // 封装pageInfo
                    pageInfo = new PageInfo<>();
                    pageInfo.setRecords(publishList);
                }
            }
        }
        
        // 如果没有查询到推荐 (查询好友动态也不会查到推荐数据)
        if (pageInfo == null) {
            // 默认查询逻辑
            if (user != null) {
                // 如果用户id不为null 则表示有用户 则查询的是"用户好友的动态信息"
                userId = user.getId();
            }
            pageInfo = this.quanZiApi.queryPublishList(userId, page, pageSize);
        }
        // 校验是否有查到分页信息 若没有 则直接返回默认值
        if (pageInfo == null) {
            // 查询失败
            return pageResult;
        }
        
        // 设置分页相关数据
        Integer counts = pageInfo.getTotal();
        pageResult.setCounts(counts);
        int pages = counts % pageSize == 0 ? counts / pageSize : counts / pageSize + 1;
        pageResult.setPages(pages);
        
        // 5.获取"动态列表"
        List<Publish> records = pageInfo.getRecords();
        // 6.判断是否有"动态列表" 如果没有 则直接返回默认值
        if (CollectionUtils.isEmpty(records)) {
            // 没有查询到动态数据
            return pageResult;
        }
        // 7.如果有"动态列表" 则获取"每条动态的状态信息(点赞数之类的)"
        pageResult.setItems(fillValueToMovements(records));
        return pageResult;
    }
    
    /**
     * 根据"动态列表",则获取"每条动态的状态信息(点赞数之类的)"
     *
     * @param records
     *     动态信息列表
     *
     * @return 动态详细信息列表(包含点赞数, 用户标签等信息)
     */
    private List<Movements> fillValueToMovements(List<Publish> records) {
        // 1.获取当前用户信息
        User user = UserThreadLocal.get();
        // 2.定义存储"所有动态信息"的集合
        List<Movements> movementsList = new ArrayList<>();
        // 3.定义集合,用来存储"动态信息列表"中,所有的用户id
        List<Long> userIds = new ArrayList<>();
        // 4.遍历"动态列表", 处理每条"动态"的"状态信息(点赞数)"
        for (Publish publish : records) {
            // 4.1 创建"动态详细信息"对象
            // 填充基本数据
            Movements movements = getMovements(publish);
            // todo 距离
            movements.setDistance("1.2公里");
            // 填充评论数
            fillCommentToMovements(movements);
            // 填充点赞数据
            fillLikeToMovements(movements, user.getId());
            // 填充喜欢数据
            fillLoveToMovements(movements, user.getId());
            
            // 添加"动态"到"动态列表"中
            movementsList.add(movements);
            
            // 4.7 如果"用户列表"中不存在当前动态所属的用户 则将该用户添加到"用户列表"中
            if (!userIds.contains(publish.getUserId())) {
                userIds.add(publish.getUserId());
            }
        }
        
        
        // 5.构造查询条件
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.in("user_id", userIds);
        // 查询用户的详细信息 用于显示每条"朋友圈动态"中用户相关的内容(年龄,婚姻状况等)
        List<UserInfo> userInfoList = userInfoService.queryUserInfoList(userInfoQueryWrapper);
        
        // 6.把用户详细信息,添加到每条"朋友圈动态信息"中
        for (Movements movements : movementsList) {
            for (UserInfo userInfo : userInfoList) {
                if (movements.getUserId().longValue() == userInfo.getUserId().longValue()) {
                    fillUserInfoToMovements(movements, userInfo);
                    //找到了,就不需要继续循环了
                    break;
                }
            }
        }
        
        return movementsList;
    }
    
    /**
     * 分页查询-好友动态
     *
     * @param page
     *     页码
     * @param pageSize
     *     每页显示个数
     *
     * @return 动态列表
     */
    public PageResult queryUserPublishList(Integer page, Integer pageSize) {
        // 获取当前登录的用户信息
        User user = UserThreadLocal.get();
        // 查询好友动态
        return queryPublishList(user, page, pageSize);
    }
    
    /**
     * 动态列表
     *
     * @param user
     *     当前用户,如果为null,则查询"推荐动态"
     * @param page
     *     页码
     * @param pageSize
     *     每页显示个数
     *
     * @return {@link PageResult} 分页查询结果
     */
    @SuppressWarnings("DuplicatedCode")
    @Deprecated
    private PageResult queryPublishList_old(User user, Integer page, Integer pageSize) {
        // 1.创建分页查询对象
        PageResult pageResult = new PageResult();
        pageResult.setCounts(0);    //默认总条数为0
        pageResult.setPages(0);     //默认总页数为0
        pageResult.setPagesize(pageSize);
        pageResult.setPage(page);
        
        // 2.获取用户ID
        // 默认没有用户id,表示没有用户,即查询的是"推荐动态"
        Long userId = null;
        // 3.判断用户是否为空,获取用户id
        if (user != null) {
            // 如果用户id不为null,则表示有用户,则查询的是"用户好友的动态信息"
            userId = user.getId();
        }
        // 4.远程调用 查询好友动态列表信息
        PageInfo<Publish> pageInfo = quanZiApi.queryPublishList(userId, page, pageSize);
        if (pageInfo == null) {
            // 没有分页信息 返回默认值
            return pageResult;
        }
        // 5.有分页信息 则更新分页查询信息
        Integer counts = pageInfo.getTotal();
        pageResult.setCounts(counts);
        int pages = counts % pageSize == 0 ? counts / pageSize : counts / pageSize + 1;
        pageResult.setPages(pages);
        
        // 6.获取"动态列表"
        List<Publish> publishes = pageInfo.getRecords();
        if (CollectionUtils.isEmpty(publishes)) {
            // 没有查询到动态数据 返回默认值
            return pageResult;
        }
        // 7.如果有"动态列表" 则获取"每条动态的状态信息(点赞数之类的)"
        pageResult.setItems(fillValueToMovements(publishes));
        return pageResult;
    }
}