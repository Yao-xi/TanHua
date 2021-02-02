package com.yx.tanhua.sso.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.sso.mapper.UserMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户 Service
 *
 * @author Yaoxi
 * @date 2021/01/17 11:39:59
 */
@Service
@Slf4j
public class UserService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private UserMapper userMapper;
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    @Autowired
    private HuanXinService huanXinService;
    
    /**
     * 用户登录
     *
     * @param phone
     *     手机号
     * @param code
     *     验证码
     *
     * @return 正常返回拼接字符串 {@code token + "|" + isNew}
     *     <p>
     *     验证码错误返回 {@code null}
     */
    public String login(String phone, String code) {
        String redisKey = "CHECK_CODE_" + phone;
        boolean isNew = false;
        
        // 校验验证码
        String redisData = this.redisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.equals(code, redisData)) {
            return null; //验证码错误
        }
        
        // 验证码在校验完成后，需要废弃
        this.redisTemplate.delete(redisKey);
        
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mobile", phone);
        // 根据手机号查询用户信息
        User user = this.userMapper.selectOne(queryWrapper);
        
        // 未查到用户信息
        if (null == user) {
            // 需要注册该用户
            user = new User();
            user.setMobile(phone);
            user.setPassword(DigestUtils.md5Hex("123456"));
            
            // 注册新用户
            this.userMapper.insert(user);
            isNew = true;
            
            // 注册环信
            huanXinService.register(user.getId());
        }
        
        // 生成token
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        String token = Jwts.builder()
            .setClaims(claims) //payload，存放数据的位置，不能放置敏感数据，如：密码等
            .signWith(SignatureAlgorithm.HS256, secret) //设置加密方法和加密盐
            .setExpiration(new DateTime().plusDays(12).toDate()) //设置过期时间，12小时后过期 todo 修改过期时间
            .compact();
        
        try { // try-catch: MQ发消息失败 不影响后续运行
            //存用户登录的消息到MQ 为了给其他系统使用
            Map<String, Object> msg = new HashMap<>();
            msg.put("id", user.getId());
            msg.put("date", System.currentTimeMillis());
            
            // topic: tanhua-sso-login
            // 消息内容: msg
            this.rocketMQTemplate.convertAndSend("tanhua-sso-login", msg);
        } catch (MessagingException e) {
            log.error("发送消息失败！", e);
        }
        
        return token + "|" + isNew;
    }
    
    /**
     * 根据Token 查询用户
     *
     * @param token
     *     token
     *
     * @return {@link User} 查询到的用户
     */
    public User queryUserByToken(String token) {
        try {
            // 通过token解析数据
            Map<String, Object> body = Jwts.parser().setSigningKey(secret)
                .parseClaimsJws(token).getBody();
            
            User user = new User();
            // 从token中获取用户的id
            user.setId(Long.valueOf(body.get("id").toString()));
            
            // 需要返回user对象中的mobile，需要查询数据库获取到mobile数据
            // 如果每次都查询数据库，必然会导致性能问题，需要把用户的手机号缓存到Redis
            // 数据缓存时，需要设置过期时间，过期时间要与token的时间一致
            // 如果用户修改了手机号，需要同步修改redis中的数据
            
            String redisKey = "TANHUA_USER_MOBILE_" + user.getId();
            if (BooleanUtils.isTrue(this.redisTemplate.hasKey(redisKey))) {
                // 如果有缓存 从缓存中获取手机号
                String mobile = this.redisTemplate.opsForValue().get(redisKey);
                user.setMobile(mobile);
            } else {
                // 没有缓存 查询数据库
                User u = this.userMapper.selectById(user.getId());
                user.setMobile(u.getMobile());
                
                //将手机号写入到redis中
                //在jwt中的过期时间的单位为：秒
                long timeout = Long.parseLong(body.get("exp").toString()) * 1000 - System.currentTimeMillis();
                this.redisTemplate.opsForValue().set(redisKey, u.getMobile(), timeout, TimeUnit.MILLISECONDS);
            }
            
            return user;
        } catch (ExpiredJwtException e) {
            log.info("token已经过期！ token = " + token);
        } catch (Exception e) {
            log.error("token不合法！ token = " + token, e);
        }
        return null;
    }
}
