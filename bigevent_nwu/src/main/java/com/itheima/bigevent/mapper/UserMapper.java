package com.itheima.bigevent.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Mapper;


import com.itheima.bigevent.pojo.User;
@Mapper
public interface UserMapper
{   //查询
    @Select("Select * from user where username = #{username}")
    User findByUserName(String username);
    //添加
    @Insert("insert into user(username,password,create_time,update_time)"+
    "values(#{username},#{password},now(),now())")
    void add(String username,String password);
    //更新
    @Update("update user set nickname = #{nickname},email = #{email},update_time = #{update_time} where id = #{id}")
    void update(User user);
    //更新头像
    @Update("update user set user_pic = #{avatarUrl},update_time = now() where id = #{id}") 
    void updateAvatar(String avatarUrl,Integer id);
    //更新密码
    @Update("update user set password = #{md5String},update_time = now(), token_version = token_version + 1 where id = #{id}")
    void updatePwd(String md5String,Integer id);
}
