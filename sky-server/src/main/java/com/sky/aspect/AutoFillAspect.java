package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
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
 * 公共字段自动填充切面
 */
@Aspect
@Component 
@Slf4j 
public class AutoFillAspect {

    // 切入点：拦截所有 Mapper 接口中被 @AutoFill 注解标注的方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {
    }

    // 前置通知：在切入点方法执行之前执行
    // 我们只控制前置，后面的操作由框架负责，不用自己去调用proceed()方法
    // @Around完全接管整个调用流程，需要自己调用proceed()方法来执行原方法
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("公共字段自动填充：{}", joinPoint.getSignature());

        // 1. 拿到方法上的 @AutoFill 注解，判断是 INSERT 还是 UPDATE
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        // 2. 拿到方法参数（约定：第一个参数是实体对象）
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0 || args[0] == null) {
            return;
        }
        Object entity = args[0];

        // 3. 准备要填充的值
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        try {
            if (operationType == OperationType.INSERT) {
                invokeSet(entity, AutoFillConstant.SET_CREATE_TIME, now);
                invokeSet(entity, AutoFillConstant.SET_CREATE_USER, currentId);
            }
            invokeSet(entity, AutoFillConstant.SET_UPDATE_TIME, now);
            invokeSet(entity, AutoFillConstant.SET_UPDATE_USER, currentId);
        } catch (Exception e) {
            log.error("公共字段填充失败", e);
        }
    }

    private void invokeSet(Object entity, String setterName, Object value) throws Exception {
        // 获取实体类的 Class 对象
        Class<?> clazz = entity.getClass();
        // 通过反射获取对应的 setter 方法，并调用它来设置值
        // setterName 是方法名，value.getClass() 是参数类型
        Method setter = clazz.getMethod(setterName, value.getClass());
        // 调用 setter 方法，将值设置到实体对象中
        setter.invoke(entity, value);
    }
}