package dev.pollito.stonks_java.cobol;

public interface CobolProgramExecutor {
  <REQ, RES> RES execute(String programName, REQ request, Class<RES> responseType);
}
