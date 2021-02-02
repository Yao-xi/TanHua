package com.yx.tanhua.server.interceptor;

import com.yx.tanhua.common.pojo.User;
import com.yx.tanhua.server.service.UserService;
import com.yx.tanhua.server.utils.NoAuthorization;
import com.yx.tanhua.server.utils.UserThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 统一完成根据token查询用User的功能
 * <p>
 * 在Controller方法调用前 先校验token 查询用户
 */
@Component
public class TokenInterceptor implements HandlerInterceptor {
    
    @Autowired
    private UserService userService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 校验请求的资源是不是方法
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 请求的方法是否携带 @NoAuthorization注解
            NoAuthorization noAnnotation = handlerMethod.getMethod().getAnnotation(NoAuthorization.class);
            if (noAnnotation != null) {
                // 如果该方法被标记为无需验证token，直接返回即可
                return true;
            }
        } else {
            // 静态资源放行
            return true;
        }
        // ---- todo 是否要考虑静态资源的问题?
        // 获取token
        String token = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(token)) {
            // 请求携带token 根据token查询用户信息
            User user = this.userService.queryUserByToken(token);
            if (null != user) {
                //将user对象和当前线程绑定
                UserThreadLocal.set(user);
                return true;
            }
        }
        
        //请求头中如不存在Authorization直接返回false
        response.setStatus(401); //无权限访问
        return false;
    }
}