package in._10h.java.springaurorafailover.standarddriver;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class DataSourceConfiguration {

    @Bean("writerDataSourceProperties")
    @ConfigurationProperties("spring.datasource.writer")
    public DataSourceProperties writerDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("readerDataSourceProperties")
    @ConfigurationProperties("spring.datasource.reader")
    public DataSourceProperties readerDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("writerDataSource")
    @ConfigurationProperties("spring.datasource.writer.hikari")
    public HikariDataSource writerDataSource(@Qualifier("writerDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("readerDataSource")
    @ConfigurationProperties("spring.datasource.reader.hikari")
    public HikariDataSource readerDataSource(@Qualifier("readerDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("dataSource")
    @Primary
    public DataSource dataSource(
        @Qualifier("writerDataSource") HikariDataSource writerDataSource,
        @Qualifier("readerDataSource") HikariDataSource readerDataSource
    ) {
        final var dataSource = new LazyConnectionDataSourceProxy(writerDataSource);
        dataSource.setReadOnlyDataSource(readerDataSource);
        return dataSource;
    }

}
