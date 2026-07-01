package com.itheima.bigevent.pojo;

import lombok.Data;
import java.time.LocalDateTime;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
@Data
public class Article {
    private Integer id;//主键ID
    @NotEmpty
    @Pattern(regexp = "^\\S{1,10}$")
    private String title;//文章标题
    @NotEmpty
    private String content;//文章内容
    @NotEmpty
    @URL
    private String cover_img;//封面图像
    @Pattern(regexp = "已发布|草稿", message = "state 必须是 已发布 或 草稿")
    private String state;//发布状态 已发布|草稿
    @NotNull
    private Integer category_id;//文章分类id
    private Integer create_user;//创建人ID
    private LocalDateTime create_time;//创建时间
    private LocalDateTime update_time;//更新时间
}
