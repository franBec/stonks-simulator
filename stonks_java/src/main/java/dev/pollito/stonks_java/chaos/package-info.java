@ApplicationModule(
    allowedDependencies = {"stock :: in", "stock :: domain", "news :: in", "news :: domain"})
package dev.pollito.stonks_java.chaos;

import org.springframework.modulith.ApplicationModule;
