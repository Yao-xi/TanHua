package com.yx.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 封装查询过滤参数
 *
 * @author Yaoxi
 * @date 2021/01/21 21:04:16
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendUserQueryParam {
    /**
     * 当前页数
     */
    private Integer page = 1;
    /**
     * 页尺寸
     */
    private Integer pagesize = 10;
    /**
     * 性别 {@code man} {@code woman}
     */
    private String gender;
    /**
     * 近期登陆时间
     */
    private String lastLogin;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 居住地
     */
    private String city;
    /**
     * 学历
     */
    private String education;
}
