package in._10h.java.springaurorafailover.standarddriver;

import in._10h.java.springaurorafailover.standarddriver.repositories.raw.direct.RawDirectTestEntity;
import in._10h.java.springaurorafailover.standarddriver.repositories.raw.direct.RawDirectTestRepository;
import in._10h.java.springaurorafailover.standarddriver.repositories.raw.proxy.RawProxyTestRepository;
import in._10h.java.springaurorafailover.standarddriver.repositories.wrapper.driversplit.WrapperDriversplitTestRepository;
import in._10h.java.springaurorafailover.standarddriver.repositories.wrapper.selfsplit.WrapperSelfsplitTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;

@Service
public class MainService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainService.class);
    private final JdbcClient rawDirectJdbcClient;
    private final JdbcClient rawProxyJdbcClient;
    private final JdbcClient wrapperSelfsplitJdbcClient;
    private final JdbcClient wrapperDriversplitJdbcClient;
    private final RawDirectTestRepository rawDirectTestRepository;
    private final RawProxyTestRepository rawProxyTestRepository;
    private final WrapperSelfsplitTestRepository wrapperSelfsplitTestRepository;
    private final WrapperDriversplitTestRepository wrapperDriversplitTestRepository;

    public MainService(
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
    }

    public record GetResult(Integer testCount, Integer readOnlyFlag) {
        @Override
        public String toString() {
            return "testCount: " + testCount + ", readOnlyFlag: " + this.readOnlyFlag;
        }
    }
    public record PostResult(Integer testCount, Integer readOnlyFlag, Integer insertedId) {
        @Override
        public String toString() {
            return "testCount: " + testCount + ", readOnlyFlag: " + this.readOnlyFlag + ", inserted id: " + this.insertedId;
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, transactionManager = "rawDirectTransactionManager")
    public GetResult testRawDirect() {
        final Integer readOnlyFlag = this.rawDirectJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final var allTest = Objects.requireNonNull(this.rawDirectTestRepository.findAll());
        LOGGER.info("testCount = {}, readOnlyFlag = {}", allTest.size(), readOnlyFlag);
        return new GetResult(allTest.size(), readOnlyFlag);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "rawDirectTransactionManager")
    public PostResult testUpdateRawDirect() {
        final Integer readOnlyFlag = this.rawDirectJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final var newEntity = new RawDirectTestEntity();
        this.rawDirectTestRepository.save(newEntity);
        final var allTest = Objects.requireNonNull(this.rawDirectTestRepository.findAll());
        LOGGER.info("testCount = {}, readOnlyFlag = {}, inserted id = {}", allTest.size(), readOnlyFlag, newEntity.getId());
        return new PostResult(allTest.size(), readOnlyFlag, newEntity.getId());
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, transactionManager = "rawProxyTransactionManager")
    public GetResult testRawProxy() {
        final Integer readOnlyFlag = this.rawProxyJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        LOGGER.info("readOnlyFlag = {}", readOnlyFlag);
        final var allTest = Objects.requireNonNull(this.rawProxyTestRepository.findAll());
        return new GetResult(allTest.size(), readOnlyFlag);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "rawProxyTransactionManager")
    public PostResult testUpdateRawProxy() {
        final Integer readOnlyFlag = this.rawProxyJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final var newEntity = new RawDirectTestEntity();
        this.rawDirectTestRepository.save(newEntity);
        final var allTest = Objects.requireNonNull(this.rawProxyTestRepository.findAll());
        LOGGER.info("testCount = {}, readOnlyFlag = {}, inserted id = {}", allTest.size(), readOnlyFlag, newEntity.getId());
        return new PostResult(allTest.size(), readOnlyFlag, newEntity.getId());
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, transactionManager = "wrapperSelfsplitTransactionManager")
    public GetResult testWrapperSelfsplit() {
        final Integer readOnlyFlag = this.wrapperSelfsplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final var allTest = Objects.requireNonNull(this.wrapperSelfsplitTestRepository.findAll());
        LOGGER.info("testCount = {}, readOnlyFlag = {}", allTest.size(), readOnlyFlag);
        return new GetResult(allTest.size(), readOnlyFlag);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "wrapperSelfsplitTransactionManager")
    public PostResult testUpdateWrapperSelfsplit() {
        final Integer readOnlyFlag = this.wrapperSelfsplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final var newEntity = new RawDirectTestEntity();
        this.rawDirectTestRepository.save(newEntity);
        final var allTest = Objects.requireNonNull(this.wrapperSelfsplitTestRepository.findAll());
        LOGGER.info("testCount = {}, readOnlyFlag = {}, inserted id = {}", allTest.size(), readOnlyFlag, newEntity.getId());
        return new PostResult(allTest.size(), readOnlyFlag, newEntity.getId());
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, transactionManager = "wrapperDriversplitTransactionManager")
    public GetResult testWrapperDriversplit() {
        final Integer readOnlyFlag = this.wrapperDriversplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final var allTest = Objects.requireNonNull(this.wrapperDriversplitTestRepository.findAll());
        LOGGER.info("testCount = {}, readOnlyFlag = {}", allTest.size(), readOnlyFlag);
        return new GetResult(allTest.size(), readOnlyFlag);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "wrapperDriversplitTransactionManager")
    public PostResult testUpdateWrapperDriversplit() {
        final Integer readOnlyFlag = this.wrapperDriversplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final var newEntity = new RawDirectTestEntity();
        this.rawDirectTestRepository.save(newEntity);
        final var allTest = Objects.requireNonNull(this.wrapperDriversplitTestRepository.findAll());
        LOGGER.info("testCount = {}, readOnlyFlag = {}, inserted id = {}", allTest.size(), readOnlyFlag, newEntity.getId());
        return new PostResult(allTest.size(), readOnlyFlag, newEntity.getId());
    }
}
