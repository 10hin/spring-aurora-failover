package in._10h.java.springaurorafailover.standarddriver.repositories.raw.proxy;

import in._10h.java.springaurorafailover.standarddriver.Test;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RawProxyTestRepository {
    @Select("SELECT id FROM test")
    List<Test> findAll();
}
