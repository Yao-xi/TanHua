package com.yx.tanhua.server.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Component
@Slf4j
public class MyTestInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        log.debug("request: URI=" + request.getRequestURI());
        log.debug("request: ParameterMap=");
        request.getParameterMap()
            .forEach((key, value) -> log.debug("        |    "
                                               + key + ":"
                                               + Arrays.toString(value)));
        return true;
    }
}
