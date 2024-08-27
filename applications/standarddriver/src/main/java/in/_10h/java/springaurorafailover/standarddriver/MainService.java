package in._10h.java.springaurorafailover.standarddriver;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import in._10h.java.springaurorafailover.standarddriver.repositories.raw.direct.RawDirectTestRepository;
import in._10h.java.springaurorafailover.standarddriver.repositories.raw.proxy.RawProxyTestRepository;
import in._10h.java.springaurorafailover.standarddriver.repositories.wrapper.driversplit.WrapperDriversplitTestRepository;
import in._10h.java.springaurorafailover.standarddriver.repositories.wrapper.selfsplit.WrapperSelfsplitTestRepository;

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

    public record GetResult(Integer testCount, Integer sessionReadOnlyFlag, Integer readerInstanceFlag) {
        @Override
        public String toString() {
            return "testCount: " + testCount + ", sessionReadOnlyFlag(0:ReadWrite/1:ReadOnly): " + this.sessionReadOnlyFlag + ", readerInstanceFlag(0:writer/1:reader): " + this.readerInstanceFlag;
        }
    }
    public record PostResult(Integer testCount, Integer sessionReadOnlyFlag, Integer readerInstanceFlag, Integer insertedId) {
        @Override
        public String toString() {
            return "testCount: " + testCount + ", sessionReadOnlyFlag(0:ReadWrite/1:ReadOnly): " + this.sessionReadOnlyFlag + ", readerInstanceFlag(0:writer/1:reader): " + this.readerInstanceFlag + ", inserted id: " + this.insertedId;
        }
    }
    public record PutResult(Integer testCount, Integer sessionReadOnlyFlag, Integer readerInstanceFlag, Integer updatedId, Integer intValBefore, Integer intValAfter) {
        @Override
        public String toString() {
            return "testCount: " + testCount + ", sessionReadOnlyFlag(0:ReadWrite/1:ReadOnly): " + this.sessionReadOnlyFlag + ", readerInstanceFlag(0:writer/1:reader): " + this.readerInstanceFlag + ", updated id: " + this.updatedId + ", intValue:(before: " + this.intValBefore + ", after: " + this.intValAfter + ")";
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, transactionManager = "rawDirectTransactionManager")
    @Retryable
    public GetResult testRawDirect() {
        final Integer sessionReadOnlyFlag = this.rawDirectJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final Integer readerInstanceFlag = this.rawDirectJdbcClient.sql("SELECT @@innodb_read_only;")
            .query(Integer.class)
            .single();
        final var allTest = Objects.requireNonNull(this.rawDirectTestRepository.findAll());
        LOGGER.info("testCount = {}, sessionReadOnlyFlag = {}, readerInstanceFlag = {}", allTest.size(), sessionReadOnlyFlag, readerInstanceFlag);
        return new GetResult(allTest.size(), sessionReadOnlyFlag, readerInstanceFlag);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "rawDirectTransactionManager")
    @Retryable
    public PostResult testInsertRawDirect() {
        final Integer sessionReadOnlyFlag = this.rawDirectJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final Integer readerInstanceFlag = this.rawDirectJdbcClient.sql("SELECT @@innodb_read_only;")
                .query(Integer.class)
                .single();
        final var newEntity = new Test();
        newEntity.setTextVal("data-source=raw-direct");
        this.rawDirectTestRepository.create(newEntity);
        final var allTest = Objects.requireNonNull(this.rawDirectTestRepository.findAll());
        LOGGER.info("testCount = {}, sessionReadOnlyFlag = {}, inserted id = {}", allTest.size(), sessionReadOnlyFlag, newEntity.getId());
        return new PostResult(allTest.size(), sessionReadOnlyFlag, readerInstanceFlag, newEntity.getId());
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "rawDirectTransactionManager")
    @Retryable
    public Optional<PutResult> testUpdateRawDirect(final Integer id) {
        final Integer sessionReadOnlyFlag = this.rawDirectJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final Integer readerInstanceFlag = this.rawDirectJdbcClient.sql("SELECT @@innodb_read_only;")
                .query(Integer.class)
                .single();
        final var entity = this.rawDirectTestRepository.findOne(id);
        return entity.map(ent -> {
            final var intValBefore = ent.getIntVal();
            ent.setIntVal(ent.getIntVal() + 1);
            this.rawDirectTestRepository.update(ent);
            return new PutResult(readerInstanceFlag, sessionReadOnlyFlag, readerInstanceFlag, id, intValBefore, ent.getIntVal());
        });
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, transactionManager = "rawProxyTransactionManager")
    @Retryable
    public GetResult testRawProxy() {
        final Integer sessionReadOnlyFlag = this.rawProxyJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final Integer readerInstanceFlag = this.rawProxyJdbcClient.sql("SELECT @@innodb_read_only;")
                .query(Integer.class)
                .single();
        LOGGER.info("sessionReadOnlyFlag = {}", sessionReadOnlyFlag);
        final var allTest = Objects.requireNonNull(this.rawProxyTestRepository.findAll());
        return new GetResult(allTest.size(), sessionReadOnlyFlag, readerInstanceFlag);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "rawProxyTransactionManager")
    @Retryable
    public PostResult testInsertRawProxy() {
        final Integer sessionReadOnlyFlag = this.rawProxyJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final Integer readerInstanceFlag = this.rawProxyJdbcClient.sql("SELECT @@innodb_read_only;")
                .query(Integer.class)
                .single();
        final var newEntity = new Test();
        newEntity.setTextVal("data-source=raw-proxy");
        this.rawProxyTestRepository.create(newEntity);
        final var allTest = Objects.requireNonNull(this.rawProxyTestRepository.findAll());
        LOGGER.info("testCount = {}, sessionReadOnlyFlag = {}, inserted id = {}", allTest.size(), sessionReadOnlyFlag, newEntity.getId());
        return new PostResult(allTest.size(), sessionReadOnlyFlag, readerInstanceFlag, newEntity.getId());
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "rawProxyTransactionManager")
    @Retryable
    public Optional<PutResult> testUpdateRawProxy(final Integer id) {
        final Integer sessionReadOnlyFlag = this.rawProxyJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final Integer readerInstanceFlag = this.rawProxyJdbcClient.sql("SELECT @@innodb_read_only;")
                .query(Integer.class)
                .single();
        final var entity = this.rawProxyTestRepository.findOne(id);
        return entity.map(ent -> {
            final var intValBefore = ent.getIntVal();
            ent.setIntVal(ent.getIntVal() + 1);
            this.rawProxyTestRepository.update(ent);
            return new PutResult(readerInstanceFlag, sessionReadOnlyFlag, readerInstanceFlag, id, intValBefore, ent.getIntVal());
        });
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, transactionManager = "wrapperSelfsplitTransactionManager")
    @Retryable
    public GetResult testWrapperSelfsplit() {
        final Integer sessionReadOnlyFlag = this.wrapperSelfsplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final Integer readerInstanceFlag = this.wrapperSelfsplitJdbcClient.sql("SELECT @@innodb_read_only;")
                .query(Integer.class)
                .single();
        final var allTest = Objects.requireNonNull(this.wrapperSelfsplitTestRepository.findAll());
        LOGGER.info("testCount = {}, sessionReadOnlyFlag = {}", allTest.size(), sessionReadOnlyFlag);
        return new GetResult(allTest.size(), sessionReadOnlyFlag, readerInstanceFlag);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "wrapperSelfsplitTransactionManager")
    @Retryable
    public PostResult testInsertWrapperSelfsplit() {
        final Integer sessionReadOnlyFlag = this.wrapperSelfsplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final Integer readerInstanceFlag = this.wrapperSelfsplitJdbcClient.sql("SELECT @@innodb_read_only;")
                .query(Integer.class)
                .single();
        final var newEntity = new Test();
        newEntity.setTextVal("data-source=wrapper-selfsplit");
        this.wrapperSelfsplitTestRepository.create(newEntity);
        final var allTest = Objects.requireNonNull(this.wrapperSelfsplitTestRepository.findAll());
        LOGGER.info("testCount = {}, sessionReadOnlyFlag = {}, inserted id = {}", allTest.size(), sessionReadOnlyFlag, newEntity.getId());
        return new PostResult(allTest.size(), sessionReadOnlyFlag, readerInstanceFlag, newEntity.getId());
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "wrapperSelfsplitTransactionManager")
    @Retryable
    public Optional<PutResult> testUpdateWrapperSelfsplit(final Integer id) {
        final Integer sessionReadOnlyFlag = this.wrapperSelfsplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final Integer readerInstanceFlag = this.wrapperSelfsplitJdbcClient.sql("SELECT @@innodb_read_only;")
                .query(Integer.class)
                .single();
        final var entity = this.wrapperSelfsplitTestRepository.findOne(id);
        return entity.map(ent -> {
            final var intValBefore = ent.getIntVal();
            ent.setIntVal(ent.getIntVal() + 1);
            this.wrapperSelfsplitTestRepository.update(ent);
            return new PutResult(readerInstanceFlag, sessionReadOnlyFlag, readerInstanceFlag, id, intValBefore, ent.getIntVal());
        });
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, transactionManager = "wrapperDriversplitTransactionManager")
    @Retryable
    public GetResult testWrapperDriversplit() {
        final Integer sessionReadOnlyFlag = this.wrapperDriversplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final Integer readerInstanceFlag = this.wrapperDriversplitJdbcClient.sql("SELECT @@innodb_read_only;")
                .query(Integer.class)
                .single();
        final var allTest = Objects.requireNonNull(this.wrapperDriversplitTestRepository.findAll());
        LOGGER.info("testCount = {}, sessionReadOnlyFlag = {}", allTest.size(), sessionReadOnlyFlag);
        return new GetResult(allTest.size(), sessionReadOnlyFlag, readerInstanceFlag);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "wrapperDriversplitTransactionManager")
    @Retryable
    public PostResult testInsertWrapperDriversplit() {
        final Integer sessionReadOnlyFlag = this.wrapperDriversplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final Integer readerInstanceFlag = this.wrapperDriversplitJdbcClient.sql("SELECT @@innodb_read_only;")
                .query(Integer.class)
                .single();
        final var newEntity = new Test();
        newEntity.setTextVal("data-source=wrapper-driversplit");
        this.wrapperDriversplitTestRepository.create(newEntity);
        final var allTest = Objects.requireNonNull(this.wrapperDriversplitTestRepository.findAll());
        LOGGER.info("testCount = {}, sessionReadOnlyFlag = {}, inserted id = {}", allTest.size(), sessionReadOnlyFlag, newEntity.getId());
        return new PostResult(allTest.size(), sessionReadOnlyFlag, readerInstanceFlag, newEntity.getId());
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, transactionManager = "wrapperDriversplitTransactionManager")
    @Retryable
    public Optional<PutResult> testUpdateWrapperDriversplit(final Integer id) {
        final Integer sessionReadOnlyFlag = this.wrapperDriversplitJdbcClient.sql("SELECT @@SESSION.transaction_read_only;")
                .query(Integer.class)
                .single();
        final Integer readerInstanceFlag = this.wrapperDriversplitJdbcClient.sql("SELECT @@innodb_read_only;")
                .query(Integer.class)
                .single();
        final var entity = this.wrapperDriversplitTestRepository.findOne(id);
        return entity.map(ent -> {
            final var intValBefore = ent.getIntVal();
            ent.setIntVal(ent.getIntVal() + 1);
            this.wrapperDriversplitTestRepository.update(ent);
            return new PutResult(readerInstanceFlag, sessionReadOnlyFlag, readerInstanceFlag, id, intValBefore, ent.getIntVal());
        });
    }
}
