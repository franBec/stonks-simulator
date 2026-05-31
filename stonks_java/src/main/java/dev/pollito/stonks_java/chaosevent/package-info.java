@ApplicationModule(
    allowedDependencies = {
      "intensity :: in", "intensity :: domain",
      "stock :: in", "stock :: domain",
      "news :: in", "news :: domain"
    })
package dev.pollito.stonks_java.chaosevent;

import org.springframework.modulith.ApplicationModule;
