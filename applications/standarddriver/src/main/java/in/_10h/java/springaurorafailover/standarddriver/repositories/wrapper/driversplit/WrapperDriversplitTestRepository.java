package in._10h.java.springaurorafailover.standarddriver.repositories.wrapper.driversplit;

import in._10h.java.springaurorafailover.standarddriver.Test;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WrapperDriversplitTestRepository {
    @Select("SELECT id FROM test")
    List<Test> findAll();
}
