package com.yx.tanhua.server.interceptor;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yx.tanhua.server.utils.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 使用{@link ControllerAdvice}与{@link ResponseBodyAdvice}实现对响应数据进行预处理
 * <p>
 * {@link ResponseBodyAdvice} 对响应体增强
 * <p>
 * 相当于AOP后置通知 可以拦截response
 * <p>
 * {@link ControllerAdvice} 可以实现
 * <ul>
 * <li>全局异常处理</li>
 * <li>全局数据绑定</li>
 * <li>全局数据预处理</li>
 * </ul>
 *
 * @author Yaoxi
 * @date 2021/01/22 20:36:10
 */
@ControllerAdvice
@Slf4j
public class MyResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    
    /**
     * JSON用
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();
    /**
     * 全局缓存开关
     */
    @Value("${tanhua.cache.enable}")
    private Boolean enable;
    /**
     * redis用
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    /**
     * 判断是否要执行beforeBodyWrite方法，true为执行，false不执行
     *
     * @param returnType
     *     Controller方法封装
     *
     * @return boolean
     */
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        log.debug("MyResponseBodyAdvice.supports 参数列表:");
        log.debug("\tMethodParameter returnType = " + returnType);
        log.debug("\tClass converterType = " + converterType);
        // 开关处于开启状态 && 是get请求 && 包含@Cache注解
        return enable && returnType.hasMethodAnnotation(GetMapping.class)
               && returnType.hasMethodAnnotation(Cache.class);
    }
    
    /**
     * 对response处理的执行方法
     *
     * @param body
     *     响应正文
     * @param returnType
     *     Controller方法封装
     * @param request
     *     Spring对请求的封装
     * @param response
     *     Spring对响应的封装
     *
     * @return {@link Object} body经过修改后的实例(可能是新的)
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        log.debug("MyResponseBodyAdvice.beforeBodyWrite 参数列表:");
        log.debug("\tMethodParameter returnType = " + returnType);
        log.debug("\tMediaType selectedContentType = " + selectedContentType);
        log.debug("\tClass selectedConverterType = " + selectedConverterType);
        if (null == body) {
            // body为空
            return null;
        }
        
        try {
            // 设置redisValue
            String redisValue = null;
            if (body instanceof String) {
                // String类型用String存储
                redisValue = (String) body;
            } else {
                // 非String类型转JSON字符串
                redisValue = MAPPER.writeValueAsString(body);
            }
            
            // 类型转换 ServerHttpRequest --> HttpServletRequest
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            // 生成redisKey
            String redisKey =
                RedisCacheInterceptor.createRedisKey(servletRequest);
            
            // 获取方法的@Cache注解
            Cache cache = returnType.getMethodAnnotation(Cache.class);
            if (cache != null) {
                // 缓存到redis 时间单位是秒
                this.redisTemplate.opsForValue()
                    .set(redisKey, redisValue, Long.parseLong(cache.time()), TimeUnit.SECONDS);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return body;
    }
}