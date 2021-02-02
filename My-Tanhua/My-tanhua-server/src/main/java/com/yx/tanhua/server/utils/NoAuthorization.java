package com.yx.tanhua.server.utils;

import java.lang.annotation.*;

/**
 * 不需要验证token的方法
 *
 * @author Yaoxi
 * @date 2021/01/25 21:28:23
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented //标记注解
public @interface NoAuthorization {

}