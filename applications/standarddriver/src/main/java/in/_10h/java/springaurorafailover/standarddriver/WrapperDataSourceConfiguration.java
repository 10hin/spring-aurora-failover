package in._10h.java.springaurorafailover.standarddriver;

import java.util.Properties;

//import com.mysql.cj.conf.PropertyDefinition;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.retry.annotation.EnableRetry;

import software.amazon.jdbc.PropertyDefinition;
import software.amazon.jdbc.ds.AwsWrapperDataSource;

import javax.sql.DataSource;

@Configuration
@EnableRetry
public class WrapperDataSourceConfiguration {

    // selfsplit

    @Bean("wrapperSelfsplitWriterDataSourceProperties")
    @ConfigurationProperties("spring.datasource.wrapper.selfsplit.writer")
    public DataSourceProperties wrapperSelfsplitWriterDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("wrapperSelfsplitReaderDataSourceProperties")
    @ConfigurationProperties("spring.datasource.wrapper.selfsplit.reader")
    public DataSourceProperties wrapperSelfsplitReaderDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("wrapperSelfsplitWriterDataSource")
    @ConfigurationProperties("spring.datasource.wrapper.selfsplit.writer.hikari")
    public HikariDataSource wrapperSelfsplitWriterDataSource(@Qualifier("wrapperSelfsplitWriterDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("wrapperSelfsplitReaderDataSource")
    @ConfigurationProperties("spring.datasource.wrapper.selfsplit.reader.hikari")
    public HikariDataSource wrapperSelfsplitReaderDataSource(@Qualifier("wrapperSelfsplitReaderDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("wrapperSelfsplitDataSource")
    public DataSource wrapperSelfsplitDataSource(
            @Qualifier("wrapperSelfsplitWriterDataSource") HikariDataSource wrapperSelfsplitWriterDataSource,
            @Qualifier("wrapperSelfsplitReaderDataSource") HikariDataSource wrapperSelfsplitReaderDataSource
    ) {
        final var dataSource = new LazyConnectionDataSourceProxy(wrapperSelfsplitWriterDataSource);
        dataSource.setReadOnlyDataSource(wrapperSelfsplitReaderDataSource);
        return dataSource;
    }

    @Bean("wrapperSelfsplitJdbcClient")
    public JdbcClient wrapperSelfsplitJdbcClient(
            @Qualifier("wrapperSelfsplitDataSource") DataSource ds
    ) {
        return JdbcClient.create(ds);
    }

    @Bean("wrapperSelfsplitSqlSessionFactory")
    public SqlSessionFactory wrapperSelfsplitSqlSessionFactory(
            @Qualifier("wrapperSelfsplitDataSource") DataSource ds
    ) throws Exception {
        final SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(ds);
        return factory.getObject();
    }

    @Bean("wrapperSelfsplitConfigurer")
    public MapperScannerConfigurer wrapperSelfsplitConfigurer() {
        final MapperScannerConfigurer c = new MapperScannerConfigurer();
        c.setBasePackage("in._10h.java.springaurorafailover.standarddriver.repositories.wrapper.selfsplit");
        c.setSqlSessionFactoryBeanName("wrapperSelfsplitSqlSessionFactory");
        return c;
    }

    @Bean("wrapperSelfsplitTransactionManager")
    public DataSourceTransactionManager wrapperSelfsplitTransactionManager(
            @Qualifier("wrapperSelfsplitDataSource") DataSource ds
    ) {
        return new DataSourceTransactionManager(ds);
    }


    // driversplit

    @Bean("wrapperDriversplitDataSourceProperties")
    @ConfigurationProperties("spring.datasource.wrapper.driversplit")
    public DataSourceProperties wrapperDriversplitDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("wrapperDriversplitDataSource")
    @ConfigurationProperties("spring.datasource.wrapper.driversplit.hikari")
    public HikariDataSource wrapperDriversplitDataSource(@Qualifier("wrapperDriversplitDataSourceProperties") DataSourceProperties props) {
        final var ds = props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        // ds.setDataSourceClassName(AwsWrapperDataSource.class.getName());
        ds.addDataSourceProperty("jdbcProtocol", "jdbc:mysql:");
        ds.addDataSourceProperty("serverName", "spring-aurora-failover.cluster-cjtkdvugsiy3.ap-northeast-1.rds.amazonaws.com");
        ds.addDataSourceProperty("serverPort", "3306");
        ds.addDataSourceProperty("database", "spring_aurora_failover");
        ds.addDataSourceProperty("targetDataSourceClassName", "com.mysql.cj.jdbc.MysqlDataSource");

        final var targetDataSourceProps = new Properties();
        targetDataSourceProps.setProperty(PropertyDefinition.PLUGINS.name, "readWriteSplitting,failover,efm2");

        ds.addDataSourceProperty("targetDataSourceProperties", targetDataSourceProps);

        return ds;
    }

    @Bean("wrapperDriversplitJdbcClient")
    public JdbcClient wrapperDriversplitJdbcClient(
            @Qualifier("wrapperDriversplitDataSource") DataSource ds
    ) {
        return JdbcClient.create(ds);
    }

    @Bean("wrapperDriversplitSqlSessionFactory")
    public SqlSessionFactory wrapperDriversplitSqlSessionFactory(
            @Qualifier("wrapperDriversplitDataSource") DataSource ds
    ) throws Exception {
        final SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(ds);
        return factory.getObject();
    }

    @Bean("wrapperDriversplitConfigurer")
    public MapperScannerConfigurer wrapperDriversplitConfigurer() {
        final MapperScannerConfigurer c = new MapperScannerConfigurer();
        c.setBasePackage("in._10h.java.springaurorafailover.standarddriver.repositories.wrapper.driversplit");
        c.setSqlSessionFactoryBeanName("wrapperDriversplitSqlSessionFactory");
        return c;
    }

    @Bean("wrapperDriversplitTransactionManager")
    public DataSourceTransactionManager wrapperDriversplitTransactionManager(
            @Qualifier("wrapperDriversplitDataSource") DataSource ds
    ) {
        return new DataSourceTransactionManager(ds);
    }

}
