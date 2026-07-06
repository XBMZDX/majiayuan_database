package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.Coffin;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CoffinMapper {

    @Select("SELECT * FROM coffin WHERE burial_id = #{burialId} ORDER BY id ASC")
    List<Coffin> listByBurial(Integer burialId);

    @Select("SELECT COUNT(*) FROM coffin WHERE burial_id = #{burialId}")
    Integer countByBurial(Integer burialId);
}
