package com.yx.tanhua.server.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yx.tanhua.common.pojo.Announcement;
import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.common.pojo.UserInfo;
import com.yx.tanhua.dubbo.server.api.QuanZiApi;
import com.yx.tanhua.dubbo.server.api.UsersApi;
import com.yx.tanhua.dubbo.server.pojo.Comment;
import com.yx.tanhua.dubbo.server.pojo.Users;
import com.yx.tanhua.dubbo.server.vo.PageInfo;
import com.yx.tanhua.server.utils.UserThreadLocal;
import com.yx.tanhua.server.vo.Contacts;
import com.yx.tanhua.server.vo.MessageAnnouncement;
import com.yx.tanhua.server.vo.MessageLike;
import com.yx.tanhua.server.vo.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IMService {
    
    @Reference(version = "1.0.0")
    private UsersApi usersApi;
    @Reference(version = "1.0.0")
    private QuanZiApi quanZiApi;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${tanhua.sso.url}")
    private String url;
    
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private AnnouncementService announcementService;
    
    /**
     * 添加好友
     *
     * @param userId
     *     好友id
     *
     * @return boolean 是否添加成功
     */
    public boolean contactUser(Long userId) {
        // 获取当前用户
        User user = UserThreadLocal.get();
        // 环信会添加双方,所以这里也需要添加双方好友
        // 封装数据
        Users users = new Users();
        users.setUserId(user.getId());
        users.setFriendId(userId);
        // 给A添加B的好友
        String id = this.usersApi.saveUsers(users);
        // 封装数据
        Users users2 = new Users();
        users2.setUserId(userId);
        users2.setFriendId(user.getId());
        // 给B添加A的好友
        String id2 = this.usersApi.saveUsers(users2);
        
        if (StringUtils.isNotEmpty(id) && StringUtils.isNotEmpty(id2)) {
            // 通过SSO模块 注册好友关系到环信
            String targetUrl = url + "/user/huanxin/contacts/" +
                               users.getUserId() + "/" + users.getFriendId();
            ResponseEntity<Void> responseEntity =
                this.restTemplate.postForEntity(targetUrl, null, Void.class);
            // 返回添加结果
            return responseEntity.getStatusCode().is2xxSuccessful();
        }
        
        return false;
    }
    
    /**
     * 查询联系人列表
     *
     * @param keyword
     *     关键词
     * @param page
     *     当前页码
     * @param pageSize
     *     每页条数
     *
     * @return {@link PageResult}
     */
    public PageResult queryContactsList(Integer page, Integer pageSize, String keyword) {
        // 获取当前用户
        User user = UserThreadLocal.get();
        // 远程调用 查询好友列表
        List<Users> usersList = this.usersApi.queryAllUsersList(user.getId());
        // 得到所有用户好友的ID集合
        List<Long> fUserIds = new ArrayList<>();
        for (Users users : usersList) {
            fUserIds.add(users.getFriendId());
        }
        
        // 查询mysql 获取好友的信息
        QueryWrapper<UserInfo> queryWrapper =
            new QueryWrapper<UserInfo>().in("user_id", fUserIds);
        // 可以根据昵称模糊查询
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like("nick_name", keyword);
        }
        // 分页查询mysql
        IPage<UserInfo> pages =
            this.userInfoService.queryUserInfoList(page, pageSize, queryWrapper);
        
        // 定义封装联系人集合
        List<Contacts> contactsList = new ArrayList<>();
        for (UserInfo userInfo : pages.getRecords()) {
            // 封装contacts对象
            Contacts contacts = new Contacts();
            contacts.setCity(StringUtils.substringBefore(userInfo.getCity(), "-"));
            contacts.setUserId(userInfo.getUserId().toString());
            contacts.setNickname(userInfo.getNickName());
            contacts.setGender(userInfo.getSex().name().toLowerCase());
            contacts.setAvatar(userInfo.getLogo());
            contacts.setAge(userInfo.getAge());
            
            contactsList.add(contacts);
        }
        
        // 封装返回值
        PageResult pageResult = new PageResult();
        pageResult.setPagesize(pageSize);
        pageResult.setPage(page);
        pageResult.setPages(0);
        pageResult.setCounts(0);
        pageResult.setItems(contactsList);
        
        return pageResult;
    }
    
    /**
     * 查询点赞列表
     */
    public PageResult queryMessageLikeList(Integer page, Integer pageSize) {
        return this.messageCommentList(1, page, pageSize);
    }
    
    /**
     * 重复代码抽取
     *
     * @param type
     *     评论类型
     *     <p>
     *     1-点赞
     *     2-评论
     *     3-喜欢
     */
    private PageResult messageCommentList(Integer type, Integer page, Integer pageSize) {
        // 获取当前用户
        User user = UserThreadLocal.get();
        // 远程调用查询分页信息
        PageInfo<Comment> pageInfo =
            this.quanZiApi.queryCommentListByUser(user.getId(), type, page, pageSize);
        // 构造默认分页返回结果
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPages(0);
        pageResult.setCounts(0);
        pageResult.setPagesize(pageSize);
        // 获取评论记录
        List<Comment> records = pageInfo.getRecords();
        // 获取评论的用户id
        List<Long> userIds = new ArrayList<>();
        for (Comment comment : records) {
            userIds.add(comment.getUserId());
        }
        // 查mysql 获取用户详细信息
        QueryWrapper<UserInfo> queryWrapper =
            new QueryWrapper<UserInfo>().in("user_id", userIds);
        List<UserInfo> userInfoList = this.userInfoService.queryList(queryWrapper);
        // 转Map
        Map<Long, UserInfo> userInfoMap = new HashMap<>();
        for (UserInfo userInfo : userInfoList) {
            userInfoMap.put(userInfo.getUserId(), userInfo);
        }
        
        List<MessageLike> messageLikeList = new ArrayList<>();
        for (Comment record : records) {
            // 封装messageLike对象
            MessageLike messageLike = new MessageLike();
            messageLike.setId(record.getId().toHexString());
            UserInfo userInfo = userInfoMap.get(record.getUserId());
            messageLike.setAvatar(userInfo.getLogo());
            messageLike.setNickname(userInfo.getNickName());
            messageLike.setCreateDate(new DateTime(record.getCreated()).toString("yyyy-MM-dd HH:mm"));
            
            messageLikeList.add(messageLike);
        }
        
        pageResult.setItems(messageLikeList);
        return pageResult;
    }
    
    /**
     * 查询评论列表
     */
    public PageResult queryMessageCommentList(Integer page, Integer pageSize) {
        return this.messageCommentList(2, page, pageSize);
    }
    
    /**
     * 查询喜欢列表
     */
    public PageResult queryMessageLoveList(Integer page, Integer pageSize) {
        return this.messageCommentList(3, page, pageSize);
    }
    
    /**
     * 查询公告消息列表
     */
    public PageResult queryMessageAnnouncementList(Integer page, Integer pageSize) {
        // 查询Mysql 获取公告信息
        IPage<Announcement> announcementPage =
            this.announcementService.queryList(page, pageSize);
        // 构造公告消息列表
        List<MessageAnnouncement> messageAnnouncementList = new ArrayList<>();
        
        for (Announcement record : announcementPage.getRecords()) {
            // 封装公告消息
            MessageAnnouncement messageAnnouncement = new MessageAnnouncement();
            messageAnnouncement.setId(record.getId().toString());
            messageAnnouncement.setTitle(record.getTitle());
            messageAnnouncement.setDescription(record.getDescription());
            messageAnnouncement.setCreateDate(new DateTime(record.getCreated()).toString("yyyy-MM-dd HH:mm"));
            // 添加到列表中
            messageAnnouncementList.add(messageAnnouncement);
        }
        // 封装分页数据
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setPages(0);
        pageResult.setCounts(0);
        pageResult.setPagesize(pageSize);
        pageResult.setItems(messageAnnouncementList);
        
        return pageResult;
    }
}
