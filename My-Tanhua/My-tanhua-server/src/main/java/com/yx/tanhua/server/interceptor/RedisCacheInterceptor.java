package com.yx.tanhua.server.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yx.tanhua.server.utils.Cache;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Redis缓存拦截器
 * <p>
 * SpringMVC自定义拦截器需要实现{@link HandlerInterceptor}接口
 * <p>
 * {@link HandlerInterceptor} 处理程序拦截器 允许自定义处理程序执行链
 * 应用程序可以为某些处理程序组注册任意数量的现有或自定义拦截器 以添加常见的预处理行为 而无需修改每个处理程序实现
 *
 * @author Yaoxi
 * @date 2021/01/22 19:28:21
 */
@Component
public class RedisCacheInterceptor implements HandlerInterceptor {
    
    /**
     * JSON转换用
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();
    /**
     * 缓存全局开关
     */
    @Value("${tanhua.cache.enable}")
    private Boolean enable;
    /**
     * 查询redis用
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 拦截Redis缓存命中
     * <p>
     * 预先处理
     * <p>
     * 在Controller调用前执行
     *
     * @param request
     *     HttpServletRequest
     * @param response
     *     HttpServletResponse
     * @param handler
     *     请求目标的封装 如果是请求Controller则会被封装为HandlerMethod
     *
     * @return boolean 是否放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        // 缓存的全局开关的校验
        if (!enable) {
            // 未启用缓存 直接放行
            return true;
        }
        
        // 校验handler是否是HandlerMethod
        // 即校验请求的资源是否不是静态资源
        if (!(handler instanceof HandlerMethod)) {
            // 不是访问Controller的 直接放行
            return true;
        }
        
        // 判断是否为get请求
        // 即判断是否是查询请求 是否有GetMapping注解
        if (!((HandlerMethod) handler).hasMethodAnnotation(GetMapping.class)) {
            // 不是查询请求不走缓存 直接放行
            return true;
        }
        
        // 判断是否添加了@Cache注解
        if (!((HandlerMethod) handler).hasMethodAnnotation(Cache.class)) {
            // 没有添加@Cache注解标记的 直接放行
            return true;
        }
        
        
        String redisKey = createRedisKey(request);
        // 获取缓存
        String cacheData = this.redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isEmpty(cacheData)) {
            //缓存未命中
            return true;
        }
        
        // 将data数据进行响应
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        assert cacheData != null;
        response.getWriter().write(cacheData);
        
        return false;
    }
    
    /**
     * 生成redis中的key
     * <p>
     * 规则：SERVER_CACHE_DATA_MD5(url + param + token)
     * <p>
     * 使用MD5加密 压缩key的长度
     *
     * @param request
     *     需要从请求中获取信息
     *
     * @return 生成的key
     */
    public static String createRedisKey(HttpServletRequest request) throws Exception {
        String url = request.getRequestURI();
        String param = MAPPER.writeValueAsString(request.getParameterMap());
        String token = request.getHeader("Authorization");
        
        String data = url + "_" + param + "_" + token;
        return "SERVER_CACHE_DATA_" + DigestUtils.md5Hex(data);
    }
}