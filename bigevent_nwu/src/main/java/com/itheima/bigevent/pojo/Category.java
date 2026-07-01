package com.itheima.bigevent.pojo;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;
@Data
//因为无法使用驼峰转换字段，所以直接保持字段名和变量名一致
public class Category {
    //定义分组
    //如果说某个校验项没有指定分组，默认属于Default分组
    //分组之间可以继承，a EXTENDS B ,那么A中就拥有b中所有的校验项
    public interface AddGroup extends Default{}
    public interface UpdateGroup extends Default{}
    @NotNull(groups = UpdateGroup.class)
    private Integer id;//主键ID
    @NotEmpty
    private String category_name;//分类名称
    @NotEmpty
    private String category_alias;//分类别名
    private Integer create_user;//创建人ID
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime create_time;//创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") 
    private LocalDateTime update_time;//更新时间
}
