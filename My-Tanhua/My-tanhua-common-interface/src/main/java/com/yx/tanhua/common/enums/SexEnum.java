package com.yx.tanhua.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 性别 枚举
 * <p>
 * 枚举 表示固定几个值,值作为对象,不能进行基本运算
 *
 * @author Yaoxi
 * @date 2021/01/17 11:41:29
 */
public enum SexEnum implements IEnum<Integer> {
    MAN(1, "man"),
    WOMAN(2, "woman"),
    UNKNOWN(3, "未知");
    
    private int value;
    /**
     * 描述
     */
    private String desc;
    
    
    /**
     * 枚举中只能定义私有构造
     */
    SexEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    
    @Override
    public Integer getValue() {
        return this.value;
    }
    
    @Override
    public String toString() {
        return this.desc;
    }
}
