#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

LOG_DIR="logs"
mkdir -p "$LOG_DIR"
LOG_FILE="$LOG_DIR/smoke-$(date +%Y%m%d-%H%M%S).log"

# ── helpers ──────────────────────────────────────────────
log()  { echo "[$(date '+%H:%M:%S')] $*" | tee -a "$LOG_FILE"; }
die()  { log "FATAL: $*"; exit 1; }

# ── trap: clean up app on any exit ───────────────────────
cleanup() {
  if [ -n "${APP_PID:-}" ]; then
    log "Shutting down application (PID $APP_PID) …"
    kill "$APP_PID" 2>/dev/null || true
    wait "$APP_PID" 2>/dev/null || true
    log "Application stopped"
  fi
}
trap cleanup EXIT

# ── grab the port from the launch config args ────────────
APP_ARGS=(
  --stonks.adapters.cobol=real
  --stonks.adapters.ai=real
  --stonks.adapters.news=real
  --spring.profiles.active=local
)

log "===== Stonks Smoke Test Suite ====="
log "Log file: $(pwd)/$LOG_FILE"

# ── 1. Compile (bootRun handles classpath/DB deps) ──────
log "--- Compiling application ---"
./gradlew classes 2>&1 | tee -a "$LOG_FILE"
if [ ${PIPESTATUS[0]} -ne 0 ]; then
  die "Gradle compilation failed"
fi

# ── 2. Start app in background (via bootRun for H2) ────
BOOT_RUN_ARGS=$(printf " %s" "${APP_ARGS[@]}")
BOOT_RUN_ARGS=${BOOT_RUN_ARGS:1}

log "Starting application via bootRun …"
./gradlew bootRun --args="$BOOT_RUN_ARGS" >> "$LOG_FILE" 2>&1 &
APP_PID=$!
log "Gradle bootRun PID: $APP_PID"

# ── 3. Wait for health ──────────────────────────────────
log "Waiting for actuator/health to return UP …"
for i in $(seq 1 30); do
  if curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1; then
    log "Application ready after ${i}s"
    break
  fi
  if [ "$i" -eq 30 ]; then
    die "Application did not become ready within 60s"
  fi
  sleep 2
done

# ── 4. Run Karate smoke tests ───────────────────────────
log "--- Running Karate smoke tests ---"
./gradlew karateTest -Dkarate.env=local 2>&1 | tee -a "$LOG_FILE"
KARATE_EXIT=${PIPESTATUS[0]}

# ── 5. Report ───────────────────────────────────────────
if [ "$KARATE_EXIT" -eq 0 ]; then
  log "SUCCESS: All smoke tests passed"
else
  log "FAILURE: Smoke tests had failures (exit code $KARATE_EXIT)"
fi

log "===== Smoke test run complete ====="
log "Log file: $(pwd)/$LOG_FILE"
exit "$KARATE_EXIT"
