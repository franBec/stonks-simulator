package dev.pollito.stonks_java;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.core.ApplicationModules;

class ModulithVerificationTest {

  private static final Logger log = LoggerFactory.getLogger(ModulithVerificationTest.class);

  @Test
  void verifyAndPrintModuleStructure() {
    var modules = ApplicationModules.of(StonksJavaApplication.class);
    modules.verify();
    log.info("\n{}", modules);
  }
}
