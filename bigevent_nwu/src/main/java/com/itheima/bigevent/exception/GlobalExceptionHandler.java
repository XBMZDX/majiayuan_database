package com.itheima.bigevent.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.itheima.bigevent.pojo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class )
    public Result handleException(Exception e)
    {
        log.error("请求处理失败", e);
        return Result.error("操作失败，请检查输入或稍后重试");

    }
}
