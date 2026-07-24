package com.itheima.bigevent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserSecurityMapper {
    @Insert("INSERT INTO user_security_log (user_id,event_type,event_detail,ip_address,create_time) VALUES (#{userId},#{eventType},#{eventDetail},#{ipAddress},NOW())")
    void addLog(@Param("userId") Integer userId, @Param("eventType") String eventType,
                @Param("eventDetail") String eventDetail, @Param("ipAddress") String ipAddress);

    @Insert("INSERT INTO user_session (session_id,user_id,login_ip,user_agent,status,login_time,last_active_time) VALUES (#{sessionId},#{userId},#{loginIp},#{userAgent},'active',NOW(),NOW())")
    void createSession(@Param("sessionId") String sessionId, @Param("userId") Integer userId,
                       @Param("loginIp") String loginIp, @Param("userAgent") String userAgent);

    @Update("UPDATE user SET last_login_time=NOW(),update_time=NOW() WHERE id=#{userId}")
    void updateLastLogin(@Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM user_session WHERE session_id=#{sessionId} AND user_id=#{userId} AND status='active'")
    int isSessionActive(@Param("sessionId") String sessionId, @Param("userId") Integer userId);

    @Update("UPDATE user_session SET last_active_time=NOW() WHERE session_id=#{sessionId} AND user_id=#{userId} AND status='active'")
    void touchSession(@Param("sessionId") String sessionId, @Param("userId") Integer userId);

    @Update("UPDATE user_session SET status='logged_out',logout_time=NOW() WHERE session_id=#{sessionId} AND user_id=#{userId} AND status='active'")
    int logoutSession(@Param("sessionId") String sessionId, @Param("userId") Integer userId);

    @Update("UPDATE user_session SET status='revoked',logout_time=NOW() WHERE session_id=#{sessionId} AND user_id=#{userId} AND status='active'")
    int revokeSession(@Param("sessionId") String sessionId, @Param("userId") Integer userId);

    @Update("UPDATE user_session SET status='revoked',logout_time=NOW() WHERE user_id=#{userId} AND status='active'")
    int revokeAllSessions(@Param("userId") Integer userId);

    @Select("SELECT id,event_type AS eventType,event_detail AS eventDetail,ip_address AS ipAddress,create_time AS createTime FROM user_security_log WHERE user_id=#{userId} ORDER BY id DESC LIMIT #{limit}")
    List<Map<String,Object>> listLogs(@Param("userId") Integer userId, @Param("limit") int limit);

    @Select("SELECT session_id AS sessionId,login_ip AS loginIp,user_agent AS userAgent,status,login_time AS loginTime,last_active_time AS lastActiveTime,logout_time AS logoutTime FROM user_session WHERE user_id=#{userId} ORDER BY login_time DESC LIMIT #{limit}")
    List<Map<String,Object>> listSessions(@Param("userId") Integer userId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM user_session WHERE user_id=#{userId} AND status='active'")
    int activeSessionCount(@Param("userId") Integer userId);

    @Select("SELECT MAX(create_time) FROM user_security_log WHERE user_id=#{userId} AND event_type='password_change'")
    LocalDateTime lastPasswordChangeTime(@Param("userId") Integer userId);
}
