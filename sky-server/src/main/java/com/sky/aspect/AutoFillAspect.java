package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，实现公共字段自动填充autofill逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    /**
     * 前置通知，在通知中进行公共字段的赋值
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        /////////////////////重要////////////////////////////////////
        //可先进行调试，是否能进入该方法 提前在mapper方法添加AutoFill注解
        log.info("开始进行公共字段自动填充...");

        // 获取被拦截的数据库操作类型 （反射）
        MethodSignature sig = (MethodSignature) joinPoint.getSignature(); // 拦截到的方法签名
        AutoFill autoFill = sig.getMethod().getAnnotation(AutoFill.class); // 获取方法上的autofill注解
        OperationType op = autoFill.value(); // 从注解的value属性中获取到数据库操作类型 (insert/ update)

        // 获取数据库操作方法的操作对象（需要修改的实体对象）
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0){
            return; // 特殊情况，如果没有参数则不继续操作 （一般正常情况下不会发生）
        }
        Object entity = args[0]; // 从参数获取要操作的实体

        // 准备需要填充的数据值
        LocalDateTime now = LocalDateTime.now();
        Long userId = BaseContext.getCurrentId();

        // 根据操作类型对实体对应字段填充数据 （通过反射）
        if (op == OperationType.INSERT){
            try {
                // 通过反射获取实体的set方法，再invoke这些set方法来修改相应字段
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setCreateUser.invoke(entity, userId);
                setUpdateUser.invoke(entity, userId);
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else if (op == OperationType.UPDATE){
            try {
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateUser.invoke(entity, userId);
                setUpdateTime.invoke(entity, now);
            }catch (Exception e){
                e.printStackTrace();
            }
        } // 实现公共字段自动填充值 （注解，AOP）

    }
}
