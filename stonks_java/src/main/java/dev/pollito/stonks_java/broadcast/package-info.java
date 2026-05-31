@ApplicationModule(
    allowedDependencies = {
      "stock",
      "stock :: domain",
      "trade :: domain",
      "chaosevent",
      "chaosevent :: domain",
      "portfolio",
      "portfolio :: domain"
    })
package dev.pollito.stonks_java.broadcast;

import org.springframework.modulith.ApplicationModule;
