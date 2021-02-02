package com.yx.tanhua.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 对自动填充字段处理
 *
 * @author Yaoxi
 * @date 2021/01/17 11:41:25
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 插入时对公共字段的填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 根据字段名获取字段值
        Object created = getFieldValByName("created", metaObject);
        if (null == created) {
            //字段为空，可以进行填充
            setFieldValByName("created", new Date(), metaObject);
        }
        // 根据字段名获取字段值
        Object updated = getFieldValByName("updated", metaObject);
        if (null == updated) {
            //字段为空，可以进行填充
            setFieldValByName("updated", new Date(), metaObject);
        }
    }
    
    /**
     * 更新时对公共字段的填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 填充updated字段值
        setFieldValByName("updated", new Date(), metaObject);
    }
}
