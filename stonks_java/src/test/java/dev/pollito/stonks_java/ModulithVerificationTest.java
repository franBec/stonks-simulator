package dev.pollito.stonks_java;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.core.ApplicationModules;

// Architecture test (not E2E) — enforces Spring Modulith module dependency rules at
// compile/verify time via ApplicationModules.verify(). Not a behavioral test; purely
// structural. Runs once to prevent illegal cross-module dependencies from creeping in.
class ModulithVerificationTest {

  private static final Logger log = LoggerFactory.getLogger(ModulithVerificationTest.class);

  @Test
  void verifyAndPrintModuleStructure() {
    var modules = ApplicationModules.of(StonksJavaApplication.class);
    modules.verify();
    log.info("\n{}", modules);
  }
}
