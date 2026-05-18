@ApplicationModule(
    allowedDependencies = {"stock", "stock :: domain", "trade", "trade :: domain", "trade :: in"
      // TODO: add "chaos", "chaos :: domain" and "portfolio", "portfolio :: domain" when those
      // modules are implemented (see architecture diagram in README)
    })
package dev.pollito.stonks_java.broadcast;

import org.springframework.modulith.ApplicationModule;
