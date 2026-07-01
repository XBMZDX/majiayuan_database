package com.itheima.bigevent.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import com.itheima.bigevent.pojo.Article;

@Mapper
public interface ArticleMapper
{
    //新增文章
    @Insert("insert into article(title, content, cover_img, state, category_id, create_user, create_time, update_time) " +
            "values(#{title}, #{content}, #{cover_img}, #{state}, #{category_id}, #{create_user}, #{create_time}, #{update_time})")
    void add(Article article);

    //分列表查询
    List<Article> list(Integer userId, Integer category_id, String state);

    //文章更新
    @Update("UPDATE article SET " +
            "title = #{title}, " +
            "content = #{content}, " +
            "cover_img = #{cover_img}, " +
            "state = #{state}, " +
            "category_id = #{category_id}, " +
            "update_time = #{update_time} " +
            "WHERE id = #{id}")
    void update(Article article);

    //文章删除
    void delete(Integer id);
}
