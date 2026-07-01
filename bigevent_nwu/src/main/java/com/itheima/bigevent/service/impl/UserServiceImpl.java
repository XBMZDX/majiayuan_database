package com.itheima.bigevent.service.impl;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itheima.bigevent.mapper.UserMapper;
import com.itheima.bigevent.pojo.User;
import com.itheima.bigevent.service.UserService;
import com.itheima.bigevent.utils.Md5Util;
import com.itheima.bigevent.utils.ThreadLocalUtil;

@Service
public class UserServiceImpl implements UserService
{
    @Autowired
    private UserMapper userMapper;
    @Override
    public User findByUserName(String username)
    {
        User u =  userMapper.findByUserName(username);
        return u;
    }
    @Override
    public void register (String username,String password)
    {
        //加密处理
        String md5String = Md5Util.getMD5String(password);
        //添加
        userMapper.add(username,md5String);
    }
    //更新
    @Override
    public void update(User user)
    {
        user.setUpdate_time(LocalDateTime.now());
        userMapper.update(user);
    }
    //更换头像
    @Override
    public void updateAvatar(String avatarUrl)
    {
        //用Threadlocal获取用户id
        Map<String,Object> map =  ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");

        userMapper.updateAvatar(avatarUrl,id);
    }
    //更新密码
    @Override
    public void updatePwd(String newPwd)
    {
        //用Threadlocal获取用户id
        Map<String,Object> map =  ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");

        userMapper.updatePwd(Md5Util.getMD5String(newPwd),id);
    }    
}
