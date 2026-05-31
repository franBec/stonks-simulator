package dev.pollito.stonks_java.karate;

import com.intuit.karate.junit5.Karate;

/**
 * JUnit 5 runner for Karate smoke tests.
 *
 * <p>These tests are <b>NOT</b> part of the standard Gradle {@code test} / {@code check} lifecycle.
 * They live in a separate source set ({@code src/karate}) and are invoked explicitly via:
 *
 * <pre>{@code
 * ./gradlew karateTest
 * }</pre>
 *
 * <p>They are intended for manual smoke testing against a <b>live</b> server running on {@code
 * localhost:8080}. They do not start an embedded Spring context and do not affect JaCoCo coverage
 * reports.
 */
class KarateSmokeTestRunner {

  @Karate.Test
  Karate runAllFeatures() {
    return Karate.run("classpath:features").relativeTo(getClass());
  }
}
