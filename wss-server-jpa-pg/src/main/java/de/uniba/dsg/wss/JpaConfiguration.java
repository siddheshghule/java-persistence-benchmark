package de.uniba.dsg.wss;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configures all beans which are required for JPA-based persistence.
 *
 * @author Benedikt Full
 */
@Configuration
@EnableJpaRepositories(basePackages = "de.uniba.dsg.wss.data.access")
@EnableTransactionManagement
@EnableRetry
public class JpaConfiguration {

  private static final Map<String, String> PROPERTY_KEYS =
      Map.of(
          "wss.jpa.hibernate.jdbc.time_zone",
          "spring.jpa.properties.hibernate.jdbc.time_zone",
          "wss.jpa.hibernate.ddl-auto",
          "hibernate.ddl-auto",
          "wss.jpa.hibernate.dialect",
          "hibernate.dialect");
  private final Environment environment;

  @Autowired
  public JpaConfiguration(Environment environment) {
    this.environment = environment;
  }

  @Bean
  @Primary
  public DataSource dataSource() {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(
        environment.getRequiredProperty("wss.jpa.datasource.driverClassName"));
    dataSource.setUrl(environment.getRequiredProperty("wss.jpa.datasource.url"));
    dataSource.setUsername(environment.getRequiredProperty("wss.jpa.datasource.username"));
    dataSource.setPassword(environment.getRequiredProperty("wss.jpa.datasource.password"));
    dataSource.setSchema(environment.getProperty("wss.jpa.datasource.schema"));
    return dataSource;
  }

  @Bean
  @Primary
  public EntityManagerFactory entityManagerFactory() {
    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setGenerateDdl(true);

    LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    factory.setJpaVendorAdapter(vendorAdapter);
    factory.setPackagesToScan("de.uniba.dsg.wss.data.model");
    factory.setDataSource(dataSource());
    Map<String, Object> props = new HashMap<>(PROPERTY_KEYS.size());
    PROPERTY_KEYS.forEach(
        (jpbKey, springKey) -> props.put(springKey, environment.getRequiredProperty(jpbKey)));
    factory.setJpaPropertyMap(props);
    factory.afterPropertiesSet();

    return factory.getObject();
  }

  @Bean
  @Primary
  public PlatformTransactionManager transactionManager() {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory());
    return transactionManager;
  }
}
