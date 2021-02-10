package com.yx.tanhua.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLikeListVo {
    
    private Long id;
    private Integer age;
    private String avatar;
    private String city;
    private String education;
    private String gender;
    private Integer marriage;
    private String nickname;
    private Integer matchRate;
}
