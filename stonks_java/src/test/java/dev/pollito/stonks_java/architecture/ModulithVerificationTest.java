package dev.pollito.stonks_java.architecture;

import dev.pollito.stonks_java.StonksJavaApplication;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.core.ApplicationModules;

// Architecture test — enforces Spring Modulith module dependency rules and prevents illegal
// cross-module couplings from creeping in.
class ModulithVerificationTest {

  private static final Logger log = LoggerFactory.getLogger(ModulithVerificationTest.class);

  @Test
  void verifyAndPrintModuleStructure() {
    var modules = ApplicationModules.of(StonksJavaApplication.class);
    modules.verify();
    log.info("\n{}", modules);
  }
}
