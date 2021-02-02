package com.yx.tanhua.server.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mybatis plus 拦截器
 */
@Configuration
public class PageConfig {

    // /**
    //  * 3.4.0之前的版本用这个
    //  * @return
    //  */
    // @Bean
    // public PaginationInterceptor paginationInterceptor(){
    //     return  new PaginationInterceptor();
    // }

    /**
     * 3.4.0之后提供的拦截器的配置方式
     */
   @Bean
   public MybatisPlusInterceptor mybatisPlusInterceptor(){
       MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
       mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
       return mybatisPlusInterceptor;
   }
}