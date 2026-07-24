package com.itheima.bigevent.pojo;



import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
//lombok 在编译阶段为实体类自动生成setter、getter方法
//pom文件引入依赖 在实体上添加注解
@Data//添加这个注解之后，在编译的时候就会自动为实体类生成方法
public class User {
    @NotNull
    private Integer id;//主键ID
    private String username;//用户名
    @JsonIgnore//让springmvc把当前对象转换成json字符串的时候，忽略password，最终json字符串中就没有password这个属性了
    private String password;//密码

    @NotEmpty
    //正则表达式
    @Pattern(regexp = "^\\S{1,10}$")
    private String nickname;//昵称

    @NotEmpty
    @Email
    private String email;//邮箱
    private String userPic;//用户头像地址
    private Integer tokenVersion;
    private LocalDateTime lastLoginTime;
    private LocalDateTime create_time;//创建时间
    private LocalDateTime update_time;//更新时间
}
