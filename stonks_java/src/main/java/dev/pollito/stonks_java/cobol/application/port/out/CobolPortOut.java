package dev.pollito.stonks_java.cobol.application.port.out;

public interface CobolPortOut {
  <REQ, RES> RES execute(String programName, REQ request, Class<RES> responseType);
}
