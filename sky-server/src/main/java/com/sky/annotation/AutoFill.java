package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解；用于进行公共字段自动填充值
 */
@Target(ElementType.METHOD) // 标识注解位置 该注解是加在方法上的
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    // 数据库操作类型 枚举类型OperationType，含有Insert, Update两种数据库操作
    OperationType value(); // Value()是注解的属性，类型为枚举类型OperationType （不是方法）
}
