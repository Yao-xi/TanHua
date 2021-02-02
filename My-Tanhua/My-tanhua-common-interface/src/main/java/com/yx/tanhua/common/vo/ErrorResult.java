package com.yx.tanhua.common.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 发生错误需要返回该对象 未发生错误响应200
 *
 * @author Yaoxi
 * @date 2021/01/17 11:41:16
 */
@Data
@Builder
public class ErrorResult {
    private String errCode;
    private String errMessage;
}
/*
 * @Builder 提供一种实例的构造方式
 * 会提供一个 xxxBuilder 的静态内部类
 * xxxBuilder 拥有和xxx类一样的字段
 *
 * 可以通过 xxx.builder().字段名(字段值).字段名(字段值).build(); 构建xxx类的对象
 * xxx.builder() 方法获取到 xxxBuilder 对象
 * xxxBuilder.字段名(字段值) 方法为其字段赋值 并返回 this
 * xxxBuilder.build() 方法获取到通过xxx的全参构造赋予的和xxxBuilder对象字段值一样的xxx对象
 * */