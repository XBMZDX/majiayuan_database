package com.itheima.bigevent.mapper;

import com.itheima.bigevent.pojo.LabInstrument;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface LabInstrumentMapper {

    @Select("SELECT * FROM lab_instruments ORDER BY id DESC")
    List<LabInstrument> list();

    @Select("SELECT * FROM lab_instruments WHERE id = #{id}")
    LabInstrument findById(Integer id);

    @Insert("INSERT INTO lab_instruments (name, image, scope, location, model, conditions, method, method_name) VALUES (#{name}, #{image}, #{scope}, #{location}, #{model}, #{conditions}, #{method}, #{methodName})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LabInstrument instrument);

    @Update("UPDATE lab_instruments SET name=#{name}, image=#{image}, scope=#{scope}, location=#{location}, model=#{model}, conditions=#{conditions}, method=#{method}, method_name=#{methodName} WHERE id=#{id}")
    void update(LabInstrument instrument);

    @Delete("DELETE FROM lab_instruments WHERE id = #{id}")
    void delete(Integer id);
}
