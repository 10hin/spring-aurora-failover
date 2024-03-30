package in._10h.java.springaurorafailover.standarddriver;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    private final JdbcClient jdbcClient;

    public MainController(final JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @GetMapping("/test")
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public String test() {
        return this.jdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
            .query(Integer.class)
            .single()
            .toString();

    }

    @PostMapping("/test")
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public String testUpdate() {
        return this.jdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
            .query(Integer.class)
            .single()
            .toString();
    }
}
