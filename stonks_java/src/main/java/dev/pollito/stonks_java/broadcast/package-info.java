@ApplicationModule(
    allowedDependencies = {
      "stock",
      "stock :: domain",
      "trade",
      "trade :: domain",
      "trade :: in",
      "chaos",
      "chaos :: domain",
      "portfolio",
      "portfolio :: domain"
    })
package dev.pollito.stonks_java.broadcast;

import org.springframework.modulith.ApplicationModule;
