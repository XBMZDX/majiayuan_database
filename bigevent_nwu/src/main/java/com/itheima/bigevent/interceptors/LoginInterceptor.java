package com.itheima.bigevent.interceptors;

import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.UserService;
import com.itheima.bigevent.utils.JwtUtil;
import com.itheima.bigevent.utils.ThreadLocalUtil;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor
{
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)throws Exception
    {
        //令牌验证
        String token =  request.getHeader("Authorization");
         //验证token
        try
        {
            Map<String,Object> claims = JwtUtil.parseToken(token);
            ThreadLocalUtil.set(claims);
            //放行
            return true;
        } 
        catch (Exception e)
        {
            response.setStatus(401);
            //不放行
            return false;   
        }
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        //清空Treadlocal 中的数据
        ThreadLocalUtil.remove();
    }
}
