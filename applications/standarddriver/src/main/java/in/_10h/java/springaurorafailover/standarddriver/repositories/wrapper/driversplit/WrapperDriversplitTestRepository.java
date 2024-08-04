package in._10h.java.springaurorafailover.standarddriver.repositories.wrapper.driversplit;

import in._10h.java.springaurorafailover.standarddriver.Test;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

@Mapper
public interface WrapperDriversplitTestRepository {
    @Select("SELECT id FROM test")
    List<Test> findAll();
    @Insert("INSERT INTO test () VALUES ()")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void save(WrapperDriversplitTestEntity test);
}
