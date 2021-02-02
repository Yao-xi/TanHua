package com.yx.tanhua.server.config;

import com.yx.tanhua.server.interceptor.MyTestInterceptor;
import com.yx.tanhua.server.interceptor.RedisCacheInterceptor;
import com.yx.tanhua.server.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置相关
 *
 * @author Yaoxi
 * @date 2021/01/22 19:48:17
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * Redis缓存拦截器
     */
    @Autowired
    private RedisCacheInterceptor redisCacheInterceptor;
    
    /**
     * token验证拦截器
     */
    @Autowired
    private TokenInterceptor tokenInterceptor;
    
    @Autowired
    private MyTestInterceptor myTestInterceptor;
    
    /**
     * 添加SpringMVC拦截器
     *
     * @param registry
     *     配置映射的拦截器列表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 测试用拦截器
        registry.addInterceptor(myTestInterceptor).addPathPatterns("/**");
        // 添加token验证拦截器
        registry.addInterceptor(this.tokenInterceptor).addPathPatterns("/**");
        // 添加Redis缓存拦截器
        // "/**" 拦截所有路径及其所有子路径
        registry.addInterceptor(this.redisCacheInterceptor).addPathPatterns("/**");
    }
    
}