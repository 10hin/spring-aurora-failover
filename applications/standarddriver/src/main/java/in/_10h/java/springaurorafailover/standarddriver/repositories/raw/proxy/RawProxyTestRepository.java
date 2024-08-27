package in._10h.java.springaurorafailover.standarddriver.repositories.raw.proxy;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

import in._10h.java.springaurorafailover.standarddriver.Test;

@Mapper
public interface RawProxyTestRepository {

    @Select("SELECT id FROM test")
    @Results(
        id = "test",
        value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "text_val", property = "textVal", id = true),
            @Result(column = "int_val", property = "intVal", id = true),
        }
    )
    List<Test> findAll();

    @Select("SELECT `id`, `text_val`, `int_val` FROM `test` WHERE `id` = #{id}")
    @ResultMap("test")
    Optional<Test> findOne(Integer id);

    @Insert("INSERT INTO test () VALUES ()")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    @ResultMap("test")
    void create(Test test);

    @Update("UPDATE `test` SET `text_val` = #{textVal}, `int_val` = #{intVal} WHERE `id` = #{id}")
    @ResultMap("test")
    void update(Test test);

}
