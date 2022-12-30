package pl.alior.sil.example.testcontainers.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "pl.alior.sil.example.testcontainers.data.repository.mwapp", transactionManagerRef = "mwappTransactionManager", entityManagerFactoryRef = "mwappContainerEntityManagerFactory")
public class DatabaseConfig {
    @Bean
    @ConfigurationProperties("datasource.mwapp")
    public DataSource dataSource() {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        return dataSourceBuilder.build();
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        vendor.setGenerateDdl(false);
        vendor.setDatabase(Database.POSTGRESQL);
        vendor.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");
        vendor.setShowSql(true);
        return vendor;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean mwappContainerEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setJpaVendorAdapter(jpaVendorAdapter());
        emf.setPackagesToScan("pl.alior.sil.example.testcontainers.data.entity.mwapp");
        return emf;
    }

    @Bean
    public PlatformTransactionManager mwappTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
