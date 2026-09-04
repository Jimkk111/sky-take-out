package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 数据库唯一键冲突的报错信息，如：Duplicate entry 'admin' for key 'employee.uk_username'
     */
    private static final Pattern DUPLICATE_ENTRY_PATTERN = Pattern.compile("Duplicate entry '(.*?)'");

    /**
     * 用户名唯一键名称
     */
    private static final String USERNAME_UNIQUE_KEY = "uk_username";

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获SQL唯一键冲突异常（如新增员工时用户名重复）
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.warn("唯一键冲突：{}", ex.getMessage());
        return Result.error(buildDuplicateMessage(ex.getMessage()));
    }

    /**
     * MyBatis-Spring会将底层的SQL唯一键冲突翻译为DuplicateKeyException抛出
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(DuplicateKeyException ex){
        log.warn("唯一键冲突：{}", ex.getMessage());
        return Result.error(buildDuplicateMessage(ex.getMessage()));
    }

    /**
     * 解析唯一键冲突信息，转换为用户可读的错误提示
     */
    private String buildDuplicateMessage(String exMessage) {
        Matcher matcher = DUPLICATE_ENTRY_PATTERN.matcher(exMessage);
        if (matcher.find() && exMessage.contains(USERNAME_UNIQUE_KEY)) {
            return "用户名 '" + matcher.group(1) + "' " + MessageConstant.ALREADY_EXISTS;
        }
        return MessageConstant.DUPLICATE_DATA;
    }
}
