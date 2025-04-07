package it.gov.pagopa.pu.send.config;

import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import javax.sql.DataSource;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "emfCitizen",
        transactionManagerRef = "tmCitizen",
        basePackages = {"it.gov.pagopa.pu.send.citizen.repository"}
)
public class CitizenDataSourceConfig {

  @Bean(name="dsCitizen")
  @ConfigurationProperties("spring.datasource.citizen")
  public DataSource citizenDataSource()  {
    return DataSourceBuilder.create().build();
  }

  @Bean(name = "emfCitizen")
  public LocalContainerEntityManagerFactoryBean citizenEntityManagerFactory(
          @Qualifier("dsCitizen") DataSource dataSource,
          EntityManagerFactoryBuilder builder,
          LocalValidatorFactoryBean validatorFactoryBean) {

    return builder.dataSource(dataSource)
            .packages("it.gov.pagopa.pu.send.citizen.model")
            .properties(Map.of(
                    "hibernate.physical_naming_strategy", CamelCaseToUnderscoresNamingStrategy.class.getName(),
                    "hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName(),
              "jakarta.persistence.validation.factory", validatorFactoryBean
            ))
            .persistenceUnit("citizen")
            .build();
  }

  @Bean(name = "tmCitizen")
  public PlatformTransactionManager citizenTransactionManager(
          @Qualifier("emfCitizen") EntityManagerFactory citizenEntityManagerFactory) {

    return new JpaTransactionManager(citizenEntityManagerFactory);
  }
}
