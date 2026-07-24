package com.itheima.bigevent.interceptors;

import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(LoginInterceptor.class);
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
            int expectedTokenVersion = currentUser == null || currentUser.getTokenVersion() == null
                    ? 0 : currentUser.getTokenVersion();
            int activeSessionCount = currentUser == null || sessionId == null
                    ? 0 : securityMapper.isSessionActive(sessionId, currentUser.getId());
            boolean valid = currentUser != null
                    && tokenVersion instanceof Number
                    && sessionId != null
                    && ((Number) tokenVersion).intValue() == expectedTokenVersion
                    && activeSessionCount > 0;
            if (!valid) {
                log.warn("认证被拒绝: path={}, user={}, tokenVersion={}, expectedTokenVersion={}, hasSessionId={}, activeSessionCount={}",
                        request.getRequestURI(), username, tokenVersion, expectedTokenVersion, sessionId != null, activeSessionCount);
                response.setStatus(401);
                return false;
            }
            ThreadLocalUtil.set(claims);
            //放行
            return true;
        } 
        catch (Exception e)
        {
            Throwable rootCause = e;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }
            log.warn("认证校验异常: path={}, reason={}, rootReason={}, detail={}",
                    request.getRequestURI(),
                    e.getClass().getSimpleName(),
                    rootCause.getClass().getSimpleName(),
                    rootCause.getMessage());
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
