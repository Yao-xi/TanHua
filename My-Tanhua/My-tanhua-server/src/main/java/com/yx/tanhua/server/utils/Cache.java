package com.yx.tanhua.server.utils;

import java.lang.annotation.*;

/**
 * 被标记为Cache的Controller进行缓存，其他情况不进行缓存
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {
    /**
     * 缓存时间，默认为300秒
     */
    String time() default "300";
}