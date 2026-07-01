package com.itheima.bigevent.controller;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.service.UserService;
import com.itheima.bigevent.utils.JwtUtil;
import com.itheima.bigevent.utils.Md5Util;
import com.itheima.bigevent.utils.ThreadLocalUtil;


import org.springframework.util.StringUtils;
import jakarta.validation.constraints.Pattern;

import com.itheima.bigevent.pojo.User;

@RestController
@RequestMapping("/user")
@Validated
@CrossOrigin
public class UserController
{
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$")String username,@Pattern(regexp = "^\\S{5,16}$")String password)
    {   
        //查询用户
        User u = userService.findByUserName(username);
        if(u == null)
        {
            //没有被占用，则要完成注册
            userService.register(username,password);
            return Result.success();
        }
        else
        {
            //被占用
            return  Result.error("用户名已被占用");
        }
        //注册
    }
    //登录
    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$")String username,@Pattern(regexp = "^\\S{5,16}$")String password)
    {
        //根据用户名查询用户
        User loginUser =  userService.findByUserName(username);
        //判断该用户是否存在
        if (loginUser == null)
        {
            return Result.error("用户名错误");    
        }
        //判断密码是否正确
        //loginUser对象中的password是密文，我们需要对参数中的password进行加密，然后再进行比较
        if(Md5Util.getMD5String(password).equals(loginUser.getPassword()))
        {
            Map<String,Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername()); 
            String token = JwtUtil.genToken(claims);
            return Result.success(token);
        }
        return  Result.error("密码错误");
    }
    //获取用户信息
    //代码优化
    @GetMapping("/userInfo") 
    public Result<User>userInfo(/*@RequestHeader(name = "Authorization")String token*/)
    {
        //根据用户名查询用户
       /*Map<String,Object> map =  JwtUtil.parseToken(token);
       String username = (String)map.get("username");*/
       Map<String,Object>map =  ThreadLocalUtil.get();
       String username = (String) map.get("username");
        User user =  userService.findByUserName(username);
        return Result.success(user);
    }
    //更新数据
    @PutMapping("/update")
    public Result update(@RequestBody@Validated User user)
    {
        userService.update(user);
        return Result.success();
    }
    //更换头像
    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl)
    {
        userService.updateAvatar(avatarUrl);
        return Result.success();
    }
    //更换密码
    @PatchMapping("/updatePwd")
    public Result updatePwd(@RequestBody Map<String,String> params)
    {
        //校验参数
        //1、先获取三个参数
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");

        //2、验证非空
        if (!StringUtils.hasLength(oldPwd)||!StringUtils.hasLength(newPwd)||!StringUtils.hasLength(rePwd))
        {
            return Result.error("缺少必要参数");    
        }
        //校验原密码是否正确
        //调用userservice 根据用户名拿到密码，在和old_pwd比对
        //找到用户名
        Map<String,Object>map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        User loginUser = userService.findByUserName(username);
        //根据用户名拿到密码,但是我们根据用户名拿到的密码是一个加密后的密码，无法和加密前的进行比对
        //所以要对所输入的old_pwd进行加密，之后再比对
        if(!loginUser.getPassword().equals(Md5Util.getMD5String(oldPwd)))
        {
            return Result.error("原密码填写错误");
        }
        //在完成原密码和之前的密码比对之后，要确认newpwd和repwd是否相同
        if (!rePwd.equals(newPwd))
        {
            return Result.error("两次密码填写不相同");    
        }
        //调用service完成密码更新

        userService.updatePwd(newPwd);
        return Result.success();

    }
    
}
