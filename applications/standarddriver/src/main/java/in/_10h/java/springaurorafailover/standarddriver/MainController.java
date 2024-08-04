package in._10h.java.springaurorafailover.standarddriver;

import in._10h.java.springaurorafailover.standarddriver.repositories.raw.direct.RawDirectTestEntity;
import in._10h.java.springaurorafailover.standarddriver.repositories.raw.direct.RawDirectTestRepository;
import in._10h.java.springaurorafailover.standarddriver.repositories.raw.proxy.RawProxyTestRepository;
import in._10h.java.springaurorafailover.standarddriver.repositories.wrapper.driversplit.WrapperDriversplitTestRepository;
import in._10h.java.springaurorafailover.standarddriver.repositories.wrapper.selfsplit.WrapperSelfsplitTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zaxxer.hikari.HikariDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
public class MainController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
    private final MainService mainService;

    public MainController(
            @Qualifier("rawDirectWriterDataSource") final HikariDataSource rawDirectWriterDataSource,
            @Qualifier("rawDirectReaderDataSource") final HikariDataSource rawDirectReaderDataSource,
            @Qualifier("rawProxyWriterDataSource") final HikariDataSource rawProxyWriterDataSource,
            @Qualifier("rawProxyReaderDataSource") final HikariDataSource rawProxyReaderDataSource,
            @Qualifier("wrapperSelfsplitWriterDataSource") final HikariDataSource wrapperSelfsplitWriterDataSource,
            @Qualifier("wrapperSelfsplitReaderDataSource") final HikariDataSource wrapperSelfsplitReaderDataSource,
            @Qualifier("wrapperDriversplitDataSource") final HikariDataSource wrapperDriversplitDataSource,
            final MainService mainService
    ) {
        LOGGER.info("rawDirectWriterDataSource.url: {}", rawDirectWriterDataSource.getJdbcUrl());
        LOGGER.info("rawDirectReaderDataSource.url: {}", rawDirectReaderDataSource.getJdbcUrl());
        LOGGER.info("rawProxyWriterDataSource.url: {}", rawProxyWriterDataSource.getJdbcUrl());
        LOGGER.info("rawProxyReaderDataSource.url: {}", rawProxyReaderDataSource.getJdbcUrl());
        LOGGER.info("wrapperSelfsplitWriterDataSource.url: {}", wrapperSelfsplitWriterDataSource.getJdbcUrl());
        LOGGER.info("wrapperSelfsplitReaderDataSource.url: {}", wrapperSelfsplitReaderDataSource.getJdbcUrl());
        LOGGER.info("wrapperDriversplitDataSource.url: {}", wrapperDriversplitDataSource.getJdbcUrl());
        this.mainService = mainService;
    }

    @GetMapping(path = "/test", headers = {"data-source=raw-direct"})
    public String testRawDirect() {
        try {
            return this.mainService.testRawDirect().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @PostMapping(path = "/test", headers = {"data-source=raw-direct"})
    public String testUpdateRawDirect() {
        try {
            return this.mainService.testUpdateRawDirect().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @GetMapping(path = "/test", headers = {"data-source=raw-proxy"})
    public String testRawProxy() {
        try {
            return this.mainService.testRawProxy().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @PostMapping(path = "/test", headers = {"data-source=raw-proxy"})
    public String testUpdateRawProxy() {
        try {
            return this.mainService.testUpdateRawProxy().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @GetMapping(path = "/test", headers = {"data-source=wrapper-selfsplit"})
    public String testWrapperSelfsplit() {
        try {
            return this.mainService.testWrapperSelfsplit().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @PostMapping(path = "/test", headers = {"data-source=wrapper-selfsplit"})
    public String testUpdateWrapperSelfsplit() {
        try {
            return this.mainService.testUpdateWrapperSelfsplit().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @GetMapping(path = "/test", headers = {"data-source=wrapper-driversplit"})
    public String testWrapperDriversplit() {
        try {
            return this.mainService.testWrapperDriversplit().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }

    @PostMapping(path = "/test", headers = {"data-source=wrapper-driversplit"})
    public String testUpdateWrapperDriversplit() {
        try {
            return this.mainService.testUpdateWrapperDriversplit().toString();
        } catch (Error | RuntimeException err) {
            LOGGER.warn("failure: {}", err.getClass().getName());
            throw err;
        }
    }
}
