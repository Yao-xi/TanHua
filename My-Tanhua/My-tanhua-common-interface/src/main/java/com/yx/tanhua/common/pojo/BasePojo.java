package com.yx.tanhua.common.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.yx.tanhua.common.handler.MyMetaObjectHandler;
import lombok.Data;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;

/**
 * Pojo基类
 *
 * @author Yaoxi
 * @date 2021/01/17 11:40:31
 */
@Data
public abstract class BasePojo {
    
    /**
     * 创建时间
     * <p>
     * 插入时自动填充
     * <p>
     * {@link MyMetaObjectHandler#insertFill(MetaObject)}
     */
    @TableField(fill = FieldFill.INSERT)
    private Date created;
    
    /**
     * 最后修改时间
     * <p>
     * 插入和更新时自动填充
     * <p>
     * {@link MyMetaObjectHandler#updateFill(MetaObject)}
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updated;
}
