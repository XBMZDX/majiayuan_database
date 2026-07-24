package com.itheima.bigevent.controller;

import com.itheima.bigevent.mapper.UserSecurityMapper;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.pojo.User;
import com.itheima.bigevent.pojo.UserProfileUpdateRequest;
import com.itheima.bigevent.service.UserService;
import com.itheima.bigevent.utils.AliOssUtil;
import com.itheima.bigevent.utils.JwtUtil;
import com.itheima.bigevent.utils.PasswordUtil;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/user")
@Validated
@CrossOrigin
public class UserController {
    private static final long AVATAR_MAX_SIZE = 5L * 1024 * 1024;
    private static final Set<String> AVATAR_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    @Autowired
    private UserService userService;
    @Autowired
    private UserSecurityMapper securityMapper;

    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$") String username,
                           @Pattern(regexp = "^\\S{5,16}$") String password) {
        if (userService.findByUserName(username) != null) return Result.error("用户名已被占用");
        userService.register(username, password);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$") String username,
                                @Pattern(regexp = "^\\S{5,16}$") String password,
                                HttpServletRequest request) {
        User loginUser = userService.findByUserName(username);
        if (loginUser == null) return Result.error("用户名错误");
        if (!PasswordUtil.matches(password, loginUser.getPassword())) return Result.error("密码错误");
        if (PasswordUtil.needsUpgrade(loginUser.getPassword())) {
            userService.upgradeLegacyPassword(loginUser.getId(), password);
            loginUser = userService.findByUserName(username);
        }

        String sessionId = UUID.randomUUID().toString();
        String ipAddress = clientIp(request);
        securityMapper.createSession(sessionId, loginUser.getId(), ipAddress, safeText(request.getHeader("User-Agent"), 500));
        securityMapper.updateLastLogin(loginUser.getId());
        securityMapper.addLog(loginUser.getId(), "login", "登录成功", ipAddress);
        Map<String,Object> claims = new HashMap<>();
        claims.put("id", loginUser.getId());
        claims.put("username", loginUser.getUsername());
        claims.put("tokenVersion", loginUser.getTokenVersion() == null ? 0 : loginUser.getTokenVersion());
        claims.put("sessionId", sessionId);
        return Result.success(JwtUtil.genToken(claims));
    }

    @GetMapping("/userInfo")
    public Result<User> userInfo() {
        return Result.success(currentUser());
    }

    @GetMapping("/security/overview")
    public Result<Map<String,Object>> securityOverview() {
        Integer userId = currentUserId();
        Map<String,Object> result = new LinkedHashMap<>();
        result.put("user", currentUser());
        result.put("lastPasswordChangeTime", securityMapper.lastPasswordChangeTime(userId));
        result.put("activeSessionCount", securityMapper.activeSessionCount(userId));
        return Result.success(result);
    }

    @PutMapping("/update")
    public Result update(@RequestBody @Validated UserProfileUpdateRequest request, HttpServletRequest servletRequest) {
        User user = new User();
        user.setId(currentUserId());
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        userService.update(user);
        securityMapper.addLog(user.getId(), "profile_update", "更新基本资料", clientIp(servletRequest));
        return Result.success();
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String,String>> uploadAvatar(@RequestPart("file") MultipartFile file, HttpServletRequest servletRequest) {
        if (file == null || file.isEmpty()) return Result.error("请选择头像图片");
        if (file.getSize() > AVATAR_MAX_SIZE) return Result.error("头像图片不能超过 5MB");
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        String extension = extensionOf(originalName);
        if (!AVATAR_EXTENSIONS.contains(extension)) return Result.error("头像仅支持 JPG、PNG、WEBP 或 GIF 格式");

        User user = currentUser();
        String newUrl = null;
        try {
            String objectName = "user-avatar/" + user.getId() + "/" + UUID.randomUUID() + "." + extension;
            newUrl = AliOssUtil.uploadFile(objectName, file.getInputStream());
            if (!StringUtils.hasLength(newUrl)) return Result.error("头像上传失败");
            String oldUrl = user.getUserPic();
            userService.updateAvatar(newUrl);
            deleteOssUrl(oldUrl);
            securityMapper.addLog(user.getId(), "avatar_update", "更新头像", clientIp(servletRequest));
            return Result.success(Map.of("avatarUrl", newUrl));
        } catch (Exception exception) {
            deleteOssUrl(newUrl);
            return Result.error("头像上传失败，请稍后重试");
        }
    }

    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> params, HttpServletRequest servletRequest) {
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");
        if (!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)) {
            return Result.error("缺少必要参数");
        }
        if (!newPwd.matches("^\\S{5,16}$")) return Result.error("新密码应为 5-16 位非空字符");
        User loginUser = currentUser();
        if (!PasswordUtil.matches(oldPwd, loginUser.getPassword())) return Result.error("原密码填写错误");
        if (!rePwd.equals(newPwd)) return Result.error("两次密码填写不相同");

        userService.updatePwd(newPwd);
        securityMapper.revokeAllSessions(loginUser.getId());
        securityMapper.addLog(loginUser.getId(), "password_change", "修改密码，已使全部旧会话失效", clientIp(servletRequest));
        return Result.success();
    }

    @GetMapping("/security/logs")
    public Result<List<Map<String,Object>>> securityLogs(@RequestParam(defaultValue = "20") int limit) {
        return Result.success(securityMapper.listLogs(currentUserId(), Math.max(1, Math.min(limit, 100))));
    }

    @GetMapping("/sessions")
    public Result<List<Map<String,Object>>> sessions(@RequestParam(defaultValue = "20") int limit) {
        String currentSessionId = currentSessionId();
        List<Map<String,Object>> sessions = securityMapper.listSessions(currentUserId(), Math.max(1, Math.min(limit, 100)));
        sessions.forEach(session -> session.put("current", Objects.equals(currentSessionId, session.get("sessionId"))));
        return Result.success(sessions);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result revokeSession(@PathVariable String sessionId, HttpServletRequest servletRequest) {
        int count = securityMapper.revokeSession(sessionId, currentUserId());
        if (count == 0) return Result.error("会话不存在或已失效");
        securityMapper.addLog(currentUserId(), "session_revoke", "手动下线一个登录会话", clientIp(servletRequest));
        return Result.success();
    }

    @PostMapping("/logout")
    public Result logout(HttpServletRequest servletRequest) {
        securityMapper.logoutSession(currentSessionId(), currentUserId());
        securityMapper.addLog(currentUserId(), "logout", "退出登录", clientIp(servletRequest));
        return Result.success();
    }

    private User currentUser() {
        String username = String.valueOf(ThreadLocalUtil.<Map<String,Object>>get().get("username"));
        User user = userService.findByUserName(username);
        if (user == null) throw new IllegalStateException("登录信息无效");
        return user;
    }

    private Integer currentUserId() {
        Object id = ThreadLocalUtil.<Map<String,Object>>get().get("id");
        if (!(id instanceof Number)) throw new IllegalStateException("登录信息无效");
        return ((Number) id).intValue();
    }

    private String currentSessionId() {
        Object sessionId = ThreadLocalUtil.<Map<String,Object>>get().get("sessionId");
        if (!(sessionId instanceof String) || ((String) sessionId).isBlank()) throw new IllegalStateException("登录信息无效");
        return (String) sessionId;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) return forwarded.split(",")[0].trim();
        String realIp = request.getHeader("X-Real-IP");
        return StringUtils.hasText(realIp) ? realIp : request.getRemoteAddr();
    }

    private String extensionOf(String fileName) {
        int position = fileName.lastIndexOf('.');
        return position < 0 ? "" : fileName.substring(position + 1).toLowerCase(Locale.ROOT);
    }

    private String safeText(String text, int maxLength) {
        return text == null ? null : text.substring(0, Math.min(text.length(), maxLength));
    }

    private void deleteOssUrl(String url) {
        if (!StringUtils.hasText(url) || !url.contains(".oss-")) return;
        try {
            String path = URI.create(url).getPath();
            if (StringUtils.hasText(path) && path.length() > 1) AliOssUtil.deleteFile(path.substring(1));
        } catch (Exception ignored) {
            // The database has already been updated. A failed cleanup must not make the avatar update fail.
        }
    }
}
