package in._10h.java.springaurorafailover.standarddriver;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfiguration {

    @Bean("rawDirectWriterDataSourceProperties")
    @ConfigurationProperties("spring.datasource.raw.direct.writer")
    public DataSourceProperties rawDirectWriterDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("rawDirectReaderDataSourceProperties")
    @ConfigurationProperties("spring.datasource.raw.direct.reader")
    public DataSourceProperties rawDirectReaderDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("rawDirectWriterDataSource")
    @ConfigurationProperties("spring.datasource.raw.direct.writer.hikari")
    public HikariDataSource rawDirectWriterDataSource(
            @Qualifier("rawDirectWriterDataSourceProperties")
            final DataSourceProperties props
    ) {
        return props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("rawDirectReaderDataSource")
    @ConfigurationProperties("spring.datasource.raw.direct.reader.hikari")
    public HikariDataSource rawDirectReaderDataSource(
            @Qualifier("rawDirectReaderDataSourceProperties")
            final DataSourceProperties props
    ) {
        return props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("rawDirectDataSource")
    @Primary
    public DataSource rawDirectDataSource(
            @Qualifier("rawDirectWriterDataSource")
            final HikariDataSource rawDirectWriterDataSource,
            @Qualifier("rawDirectReaderDataSource")
            final HikariDataSource rawDirectReaderDataSource
    ) {
        final var dataSource = new LazyConnectionDataSourceProxy(rawDirectWriterDataSource);
        dataSource.setReadOnlyDataSource(rawDirectReaderDataSource);
        return dataSource;
    }

    @Bean("rawDirectJdbcClient")
    @Primary
    public JdbcClient rawDirectJdbcClient(
            @Qualifier("rawDirectDataSource")
            final DataSource ds
    ) {
        return JdbcClient.create(ds);
    }

    @Bean("rawDirectSqlSessionFactory")
    @Primary
    public SqlSessionFactory rawDirectSqlSessionFactory(
            @Qualifier("rawDirectDataSource")
            final DataSource ds
    ) throws Exception {
        final SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(ds);
        return factory.getObject();
    }

    @Bean("rawDirectConfigurer")
    public MapperScannerConfigurer rawDirectConfigurer() {
        final MapperScannerConfigurer c = new MapperScannerConfigurer();
        c.setBasePackage("in._10h.java.springaurorafailover.standarddriver.repositories.raw.direct");
        c.setSqlSessionFactoryBeanName("rawDirectSqlSessionFactory");
        return c;
    }

    @Bean("rawDirectTransactionManager")
    @Primary
    public DataSourceTransactionManager rawDirectTransactionManager(
            @Qualifier("rawDirectDataSource")
            final DataSource ds
    ) {
        return new DataSourceTransactionManager(ds);
    }

    @Bean("rawProxyWriterDataSourceProperties")
    @ConfigurationProperties("spring.datasource.raw.proxy.writer")
    public DataSourceProperties rawProxyWriterDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("rawProxyReaderDataSourceProperties")
    @ConfigurationProperties("spring.datasource.raw.proxy.reader")
    public DataSourceProperties rawProxyReaderDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("rawProxyWriterDataSource")
    @ConfigurationProperties("spring.datasource.raw.proxy.writer.hikari")
    public HikariDataSource rawProxyWriterDataSource(
            @Qualifier("rawProxyWriterDataSourceProperties")
            final DataSourceProperties props
    ) {
        return props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("rawProxyReaderDataSource")
    @ConfigurationProperties("spring.datasource.raw.proxy.reader.hikari")
    public HikariDataSource rawProxyReaderDataSource(
            @Qualifier("rawProxyReaderDataSourceProperties")
            final DataSourceProperties props
    ) {
        return props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("rawProxyDataSource")
    public DataSource rawProxyDataSource(
            @Qualifier("rawProxyWriterDataSource")
            final HikariDataSource rawProxyWriterDataSource,
            @Qualifier("rawProxyReaderDataSource")
            final HikariDataSource rawProxyReaderDataSource
    ) {
        final var dataSource = new LazyConnectionDataSourceProxy(rawProxyWriterDataSource);
        dataSource.setReadOnlyDataSource(rawProxyReaderDataSource);
        return dataSource;
    }

    @Bean("rawProxyJdbcClient")
    public JdbcClient rawProxyJdbcClient(
            @Qualifier("rawProxyDataSource")
            final DataSource ds
    ) {
        return JdbcClient.create(ds);
    }

    @Bean("rawProxySqlSessionFactory")
    public SqlSessionFactory rawProxySqlSessionFactory(
            @Qualifier("rawProxyDataSource")
            final DataSource ds
    ) throws Exception {
        final SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(ds);
        return factory.getObject();
    }

    @Bean("rawProxyConfigurer")
    public MapperScannerConfigurer rawProxyConfigurer() {
        final MapperScannerConfigurer c = new MapperScannerConfigurer();
        c.setBasePackage("in._10h.java.springaurorafailover.standarddriver.repositories.raw.proxy");
        c.setSqlSessionFactoryBeanName("rawProxySqlSessionFactory");
        return c;
    }

    @Bean("rawProxyTransactionManager")
    public DataSourceTransactionManager rawProxyTransactionManager(
            @Qualifier("rawProxyDataSource")
            final DataSource ds
    ) {
        return new DataSourceTransactionManager(ds);
    }

}
