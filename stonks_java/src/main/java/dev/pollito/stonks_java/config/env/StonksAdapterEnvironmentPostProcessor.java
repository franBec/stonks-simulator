package dev.pollito.stonks_java.config.env;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class StonksAdapterEnvironmentPostProcessor implements EnvironmentPostProcessor {

  private static final String PROPERTY_PREFIX = "stonks.adapters.";
  private static final String DB_PROPERTY = PROPERTY_PREFIX + "db";

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    String dbMode = environment.getProperty(DB_PROPERTY, "h2").toLowerCase();

    Map<String, Object> properties = new HashMap<>();

    if ("postgresql".equals(dbMode)) {
      properties.put("spring.datasource.url", "jdbc:postgresql://localhost:5432/stonks");
      properties.put("spring.datasource.driver-class-name", "org.postgresql.Driver");
      properties.put("spring.datasource.username", "${POSTGRES_USER:stonks}");
      properties.put("spring.datasource.password", "${POSTGRES_PASSWORD:}");
      properties.put("spring.jpa.database-platform", "org.hibernate.dialect.PostgreSQLDialect");
      properties.put("spring.h2.console.enabled", false);
    } else {
      properties.put(
          "spring.datasource.url", "jdbc:h2:mem:stonks;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
      properties.put("spring.datasource.driver-class-name", "org.h2.Driver");
      properties.put("spring.datasource.username", "sa");
      properties.put("spring.datasource.password", "");
      properties.put("spring.jpa.database-platform", "org.hibernate.dialect.H2Dialect");
      properties.put("spring.h2.console.enabled", true);
    }

    properties.put("spring.jpa.hibernate.ddl-auto", "none");

    MapPropertySource propertySource =
        new MapPropertySource("stonksAdapterDbProperties", properties);
    environment.getPropertySources().addLast(propertySource);
  }
}
