package dev.pollito.stonks_java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.modulith.Modulithic;

@Modulithic(
    systemName = "Stonks Simulator",
    sharedModules = {"cobol", "config", "util", "generated"})
@SpringBootApplication
@ConfigurationPropertiesScan
public class StonksJavaApplication {

  public static void main(String[] args) {
    SpringApplication.run(StonksJavaApplication.class, args);
  }
}
