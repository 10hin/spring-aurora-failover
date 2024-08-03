package in._10h.java.springaurorafailover.awsdriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class MainController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
    private final JdbcClient jdbcClient;
    private final TestRepository testRepository;

    public MainController(
            final JdbcClient jdbcClient,
            final TestRepository testRepository
    ) {
        this.jdbcClient = jdbcClient;
        this.testRepository = testRepository;
    }

    @GetMapping("/test")
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
    public String test() {
        final var readOnlyFlag = this.jdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        LOGGER.info("readOnlyFlag = {}", readOnlyFlag);
        final var allTest = Objects.requireNonNull(this.testRepository.findAll());
        return "testCount: " + allTest.size() + ", readOnlyFlag: " + readOnlyFlag;
    }

    @PostMapping("/test")
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public String testUpdate() {
        final var readOnlyFlag = this.jdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        LOGGER.info("readOnlyFlag = {}", readOnlyFlag);
        final var allTest = Objects.requireNonNull(this.testRepository.findAll());
        return "testCount: " + allTest.size() + ", readOnlyFlag: " + readOnlyFlag;
    }
}
