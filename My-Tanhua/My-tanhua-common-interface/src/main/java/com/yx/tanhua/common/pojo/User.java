package com.yx.tanhua.common.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * User 用户
 *
 * @author Yaoxi
 * @date 2021/01/17 11:40:38
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends BasePojo {
    private Long id;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 密码
     * <p>
     * json序列化时忽略
     */
    @JsonIgnore
    private String password;
}

