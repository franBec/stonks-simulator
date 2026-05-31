---
description: Analyze a smoke test log file from stonks_java/logs
---

Analyze the smoke test log file provided and determine if the smoke test run succeeded or failed.

## Log File to Analyze

The user provided: `$1`

Resolve the path:
- If `$1` is just a filename (no `/`), look in `stonks_java/logs/$1`
- If `$1` is a relative path, resolve relative to `stonks_java/`
- If `$1` is an absolute path, use it directly

If the file does not exist, report the error and list available `.log` files in `stonks_java/logs/`.

## Log File Content

```
!`if [ -f "$1" ]; then cat "$1"; elif [ -f "stonks_java/logs/$1" ]; then cat "stonks_java/logs/$1"; elif [ -f "stonks_java/$1" ]; then cat "stonks_java/$1"; else echo "FILE_NOT_FOUND"; fi`
```

If the file was not found, stop here and list available logs: `` !`ls -1 stonks_java/logs/*.log 2>/dev/null || echo "No log files found in stonks_java/logs/"` ``

## Analysis Instructions

Read the log file and check for these 4 phases in order. Look for the exact markers emitted by `@stonks_java/run-smoke-tests.sh`:

### Phase 1: Compilation
- **Success:** `BUILD SUCCESSFUL` appears in the Gradle output
- **Failure:** `BUILD FAILED` or `FATAL: Gradle compilation failed`

### Phase 2: Application Startup
- **Success:** `Application ready after Ns` appears
- **Failure:** `FATAL: Application did not become ready within 60s`

### Phase 3: Health Check
- **Success:** Implicitly passed if Phase 2 succeeded (the script polls `actuator/health`)
- **Failure:** Connection refused, timeout, or HTTP errors before the ready message

### Phase 4: Karate Smoke Tests
- **Success:** `SUCCESS: All smoke tests passed`
- **Failure:** `FAILURE: Smoke tests had failures (exit code N)`

Also scan for these common issues anywhere in the log:
- Java stack traces (`Exception`, `ERROR`, `FATAL` in application output)
- Port binding errors (`Port 8080 was already in use`, `BindException`)
- COBOL adapter errors (if `stonks.adapters.cobol=real`)
- Gradle test failure lines in the Karate output
- `OutOfMemoryError`

## Output Format

Produce a concise markdown report:

```markdown
## Smoke Test Log Analysis

**Log file:** `<resolved-path>`

### Summary
- **Overall Status:** PASS / FAIL / PARTIAL
- **Exit Code:** <N or unknown>
- **Phases Passed:** X/4

### Phase Breakdown
1. **Compilation:** <emoji> <status> (<brief note>)
2. **Application Startup:** <emoji> <status> (<brief note>)
3. **Health Check:** <emoji> <status> (<brief note>)
4. **Karate Smoke Tests:** <emoji> <status> (<brief note>)

### Errors Found
- <List specific errors with context, or "None" if clean>

### Verdict
- <One-sentence summary of whether everything went OK>
- <If failures found, suggest the most likely next step>
```

### Emoji Guide
- Use ✅ for passed phases
- Use ❌ for failed phases
- Use ⚠️ for phases with warnings but not outright failure
- Use ⏭️ for phases that were skipped due to earlier failure

## Rules
- If the log is empty or truncated, say so and suggest re-running the smoke tests.
- If the log contains multiple runs (e.g., appended output), analyze the **last** run only.
- If no recognizable smoke test markers are found, report that the file does not appear to be a valid smoke test log.
- Be specific: quote the exact log lines that indicate success or failure.
