package com.yx.tanhua.common.pojo;

import com.yx.tanhua.common.enums.SexEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * UserInfo 用户信息
 *
 * @author Yaoxi
 * @date 2021/01/17 11:40:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo extends BasePojo {
    /**
     *
     */
    private Long id;
    
    /**
     * 用户id
     */
    private Long userId;
    
    /**
     * 昵称
     */
    private String nickName;
    
    /**
     * 用户头像
     */
    private String logo;
    
    /**
     * 用户标签：多个用逗号分隔
     */
    private String tags;
    
    /**
     * 性别，1-男，2-女，3-未知
     */
    private SexEnum sex;
    
    /**
     * 用户年龄
     */
    private Integer age;
    
    /**
     * 学历
     */
    private String edu;
    
    /**
     * 居住城市
     */
    private String city;
    
    /**
     * 生日
     */
    private String birthday;
    
    /**
     * 封面图片
     */
    private String coverPic;
    
    /**
     * 行业
     */
    private String industry;
    
    /**
     * 收入
     */
    private String income;
    
    /**
     * 婚姻状态
     */
    private String marriage;
}

