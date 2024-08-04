package in._10h.java.springaurorafailover.standarddriver.repositories.raw.proxy;

import in._10h.java.springaurorafailover.standarddriver.Test;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RawProxyTestRepository {
    @Select("SELECT id FROM test")
    List<Test> findAll();
    @Insert("INSERT INTO test () VALUES ()")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    void save(RawProxyTestEntity test);
}
