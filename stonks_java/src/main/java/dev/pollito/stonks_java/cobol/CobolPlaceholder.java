package dev.pollito.stonks_java.cobol;

/**
 * The {@code cobol} module is the bridge to the GnuCOBOL legacy trading engine.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Spawn GnuCOBOL binaries via stdin/stdout process execution
 *   <li>Provide Java wrappers for COBOL programs: TRADE-VALIDATOR, PORTFOLIO-MGR, COMPLIANCE-MGR
 *   <li>Translate COBOL output into Java-friendly data structures
 *   <li>Convert errors into retro COBOL-style messages (e.g. "JOB ABEND S222 - INSUFFICIENT FUNDS")
 * </ul>
 *
 * <p>This is a leaf / infrastructure module — it encapsulates an external system and should not
 * depend on any business modules.
 */
public class CobolPlaceholder {
  public CobolPlaceholder() {
    throw new RuntimeException("boundary placeholder: cobol module");
  }
}
