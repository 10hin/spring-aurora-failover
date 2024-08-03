package in._10h.java.springaurorafailover.standarddriver;

import in._10h.java.springaurorafailover.standarddriver.repositories.raw.direct.RawDirectTestRepository;
import in._10h.java.springaurorafailover.standarddriver.repositories.raw.proxy.RawProxyTestRepository;
import in._10h.java.springaurorafailover.standarddriver.repositories.wrapper.driversplit.WrapperDriversplitTestRepository;
import in._10h.java.springaurorafailover.standarddriver.repositories.wrapper.selfsplit.WrapperSelfsplitTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zaxxer.hikari.HikariDataSource;

import java.util.Objects;

import javax.sql.DataSource;

@RestController
public class MainController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
    private final JdbcClient rawDirectJdbcClient;
    private final JdbcClient rawProxyJdbcClient;
    private final JdbcClient wrapperSelfsplitJdbcClient;
    private final JdbcClient wrapperDriversplitJdbcClient;
    private final RawDirectTestRepository rawDirectTestRepository;
    private final RawProxyTestRepository rawProxyTestRepository;
    private final WrapperSelfsplitTestRepository wrapperSelfsplitTestRepository;
    private final WrapperDriversplitTestRepository wrapperDriversplitTestRepository;

    public MainController(
            @Qualifier("rawDirectWriterDataSource") final HikariDataSource rawDirectWriterDataSource,
            @Qualifier("rawDirectReaderDataSource") final HikariDataSource rawDirectReaderDataSource,
            @Qualifier("rawProxyWriterDataSource") final HikariDataSource rawProxyWriterDataSource,
            @Qualifier("rawProxyReaderDataSource") final HikariDataSource rawProxyReaderDataSource,
            @Qualifier("wrapperSelfsplitWriterDataSource") final HikariDataSource wrapperSelfsplitWriterDataSource,
            @Qualifier("wrapperSelfsplitReaderDataSource") final HikariDataSource wrapperSelfsplitReaderDataSource,
            @Qualifier("wrapperDriversplitDataSource") final HikariDataSource wrapperDriversplitDataSource,
            @Qualifier("rawDirectJdbcClient") final JdbcClient rawDirectJdbcClient,
            @Qualifier("rawProxyJdbcClient") final JdbcClient rawProxyJdbcClient,
            @Qualifier("wrapperSelfsplitJdbcClient") final JdbcClient wrapperSelfsplitJdbcClient,
            @Qualifier("wrapperDriversplitJdbcClient") final JdbcClient wrapperDriversplitJdbcClient,
            final RawDirectTestRepository rawDirectTestRepository,
            final RawProxyTestRepository rawProxyTestRepository,
            final WrapperSelfsplitTestRepository wrapperSelfsplitTestRepository,
            final WrapperDriversplitTestRepository wrapperDriversplitTestRepository
    ) {
        this.rawDirectJdbcClient = rawDirectJdbcClient;
        this.rawProxyJdbcClient = rawProxyJdbcClient;
        this.wrapperSelfsplitJdbcClient = wrapperSelfsplitJdbcClient;
        this.wrapperDriversplitJdbcClient = wrapperDriversplitJdbcClient;
        this.rawDirectTestRepository = rawDirectTestRepository;
        this.rawProxyTestRepository = rawProxyTestRepository;
        this.wrapperSelfsplitTestRepository = wrapperSelfsplitTestRepository;
        this.wrapperDriversplitTestRepository = wrapperDriversplitTestRepository;
        LOGGER.info("rawDirectWriterDataSource.url: {}", rawDirectWriterDataSource.getJdbcUrl());
        LOGGER.info("rawDirectReaderDataSource.url: {}", rawDirectReaderDataSource.getJdbcUrl());
        LOGGER.info("rawProxyWriterDataSource.url: {}", rawProxyWriterDataSource.getJdbcUrl());
        LOGGER.info("rawProxyReaderDataSource.url: {}", rawProxyReaderDataSource.getJdbcUrl());
        LOGGER.info("wrapperSelfsplitWriterDataSource.url: {}", wrapperSelfsplitWriterDataSource.getJdbcUrl());
        LOGGER.info("wrapperSelfsplitReaderDataSource.url: {}", wrapperSelfsplitReaderDataSource.getJdbcUrl());
        LOGGER.info("wrapperDriversplitDataSource.url: {}", wrapperDriversplitDataSource.getJdbcUrl());
    }

    @GetMapping(path = "/test", headers = {"data-source=raw-direct"})
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, transactionManager = "rawDirectTransactionManager")
    public String testRawDirect() {
        final var readOnlyFlag = this.rawDirectJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
            .query(Integer.class)
            .single();
        LOGGER.info("readOnlyFlag = {}", readOnlyFlag);
        final var allTest = Objects.requireNonNull(this.rawDirectTestRepository.findAll());
        return "testCount: " + allTest.size() + ", readOnlyFlag: " + readOnlyFlag;
    }

    @PostMapping(path = "/test", headers = {"data-source=raw-direct"})
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "rawDirectTransactionManager")
    public String testUpdateRawDirect() {
        final var readOnlyFlag = this.rawDirectJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        LOGGER.info("readOnlyFlag = {}", readOnlyFlag);
        final var allTest = Objects.requireNonNull(this.rawDirectTestRepository.findAll());
        return "testCount: " + allTest.size() + ", readOnlyFlag: " + readOnlyFlag;
    }

    @GetMapping(path = "/test", headers = {"data-source=raw-proxy"})
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, transactionManager = "rawProxyTransactionManager")
    public String testRawProxy() {
        final var readOnlyFlag = this.rawProxyJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        LOGGER.info("readOnlyFlag = {}", readOnlyFlag);
        final var allTest = Objects.requireNonNull(this.rawProxyTestRepository.findAll());
        return "testCount: " + allTest.size() + ", readOnlyFlag: " + readOnlyFlag;
    }

    @PostMapping(path = "/test", headers = {"data-source=raw-proxy"})
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "rawProxyTransactionManager")
    public String testUpdateRawProxy() {
        final var readOnlyFlag = this.rawProxyJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        LOGGER.info("readOnlyFlag = {}", readOnlyFlag);
        final var allTest = Objects.requireNonNull(this.rawProxyTestRepository.findAll());
        return "testCount: " + allTest.size() + ", readOnlyFlag: " + readOnlyFlag;
    }

    @GetMapping(path = "/test", headers = {"data-source=wrapper-selfsplit"})
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, transactionManager = "wrapperSelfsplitTransactionManager")
    public String testWrapperSelfsplit() {
        final var readOnlyFlag = this.wrapperSelfsplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        LOGGER.info("readOnlyFlag = {}", readOnlyFlag);
        final var allTest = Objects.requireNonNull(this.wrapperSelfsplitTestRepository.findAll());
        return "testCount: " + allTest.size() + ", readOnlyFlag: " + readOnlyFlag;
    }

    @PostMapping(path = "/test", headers = {"data-source=wrapper-selfsplit"})
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "wrapperSelfsplitTransactionManager")
    public String testUpdateWrapperSelfsplit() {
        final var readOnlyFlag = this.wrapperSelfsplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        LOGGER.info("readOnlyFlag = {}", readOnlyFlag);
        final var allTest = Objects.requireNonNull(this.wrapperSelfsplitTestRepository.findAll());
        return "testCount: " + allTest.size() + ", readOnlyFlag: " + readOnlyFlag;
    }

    @GetMapping(path = "/test", headers = {"data-source=wrapper-driversplit"})
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, transactionManager = "wrapperDriversplitTransactionManager")
    public String testWrapperDriversplit() {
        final var readOnlyFlag = this.wrapperDriversplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        LOGGER.info("readOnlyFlag = {}", readOnlyFlag);
        final var allTest = Objects.requireNonNull(this.wrapperDriversplitTestRepository.findAll());
        return "testCount: " + allTest.size() + ", readOnlyFlag: " + readOnlyFlag;
    }

    @PostMapping(path = "/test", headers = {"data-source=wrapper-driversplit"})
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "wrapperDriversplitTransactionManager")
    public String testUpdateWrapperDriversplit() {
        final var readOnlyFlag = this.wrapperDriversplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        LOGGER.info("readOnlyFlag = {}", readOnlyFlag);
        final var allTest = Objects.requireNonNull(this.wrapperDriversplitTestRepository.findAll());
        return "testCount: " + allTest.size() + ", readOnlyFlag: " + readOnlyFlag;
    }
}
