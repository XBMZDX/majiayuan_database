package com.itheima.bigevent.interceptors;

import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.itheima.bigevent.mapper.UserSecurityMapper;
import com.itheima.bigevent.pojo.User;
import com.itheima.bigevent.service.UserService;
import com.itheima.bigevent.utils.JwtUtil;
import com.itheima.bigevent.utils.ThreadLocalUtil;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor
{
    @Resource
    private UserService userService;
    @Resource
    private UserSecurityMapper securityMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)throws Exception
    {
        //令牌验证
        String token =  request.getHeader("Authorization");
         //验证token
        try
        {
            Map<String,Object> claims = JwtUtil.parseToken(token);
            String username = claims.get("username") instanceof String ? (String) claims.get("username") : null;
            Object tokenVersion = claims.get("tokenVersion");
            String sessionId = claims.get("sessionId") instanceof String ? (String) claims.get("sessionId") : null;
            User currentUser = username == null ? null : userService.findByUserName(username);
            if (currentUser == null || !(tokenVersion instanceof Number)
                    || sessionId == null
                    || ((Number) tokenVersion).intValue() != (currentUser.getTokenVersion() == null ? 0 : currentUser.getTokenVersion())
                    || securityMapper.isSessionActive(sessionId, currentUser.getId()) == 0) {
                response.setStatus(401);
                return false;
            }
            securityMapper.touchSession(sessionId, currentUser.getId());
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
