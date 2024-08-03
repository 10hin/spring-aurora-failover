package in._10h.java.springaurorafailover.awsdriver;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TestRepository {
    @Select("SELECT id FROM test")
    List<Test> findAll();
}
