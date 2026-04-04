#!/usr/bin/env bash
# =============================================================================
# NeoBank Production Service Health Monitor
# =============================================================================
# Checks /actuator/health of all backend modules and /api/health of the
# frontend. If any service returns a non-200 status, logs the failure and
# simulates a webhook alert to STDOUT.
#
# Usage:
#   ./scripts/monitor-services.sh                  # single check
#   ./scripts/monitor-services.sh --watch 30       # check every 30 seconds
# =============================================================================

set -euo pipefail

# ── Configuration ────────────────────────────────────────────────────────────
GATEWAY_URL="${NEOBANK_GATEWAY_URL:-http://localhost:8080}"

# Backend modules (all reachable through the gateway on port 8080)
# Each entry: "Service Name|Health Endpoint"
SERVICES=(
  "Gateway        |${GATEWAY_URL}/actuator/health"
  "Auth           |http://localhost:8081/actuator/health"
  "Onboarding     |http://localhost:8082/actuator/health"
  "Core Banking   |http://localhost:8083/actuator/health"
  "Lending        |http://localhost:8084/actuator/health"
  "Cards          |http://localhost:8085/actuator/health"
  "Analytics      |http://localhost:8086/actuator/health"
  "Batch          |http://localhost:8087/actuator/health"
  "Fraud          |http://localhost:8088/actuator/health"
)

# Frontend
FRONTEND_URL="${NEOBANK_FRONTEND_URL:-http://localhost:3000}"
FRONTEND_HEALTH="${FRONTEND_URL}/api/health"

# Webhook URL (simulated — printed to STDOUT)
WEBHOOK_URL="${HEALTH_ALERT_WEBHOOK_URL:-https://hooks.example.com/neobank-alerts}"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# ── Functions ────────────────────────────────────────────────────────────────

timestamp() {
  date '+%Y-%m-%d %H:%M:%S %Z'
}

log_info() {
  echo -e "${CYAN}[$(timestamp)] INFO${NC}  $*"
}

log_ok() {
  echo -e "${GREEN}[$(timestamp)] OK${NC}    $*"
}

log_fail() {
  echo -e "${RED}[$(timestamp)] FAIL${NC}  $*"
}

log_warn() {
  echo -e "${YELLOW}[$(timestamp)] WARN${NC}  $*"
}

send_webhook_alert() {
  local service="$1"
  local endpoint="$2"
  local http_code="$3"
  local body="$4"

  # Simulated webhook — in production, replace with actual curl to your
  # Slack / PagerDuty / Opsgenie webhook URL.
  cat <<EOF
┌─────────────────────────────────────────────────────────────────┐
│  ⚠️  NEOBANK HEALTH ALERT                                      │
├─────────────────────────────────────────────────────────────────┤
│  Service   : ${service}
│  Endpoint  : ${endpoint}
│  HTTP Code : ${http_code}
│  Response  : ${body}
│  Webhook   : ${WEBHOOK_URL}
│  Time      : $(timestamp)
├─────────────────────────────────────────────────────────────────┤
│  curl -X POST "${WEBHOOK_URL}" \\
│    -H "Content-Type: application/json" \\
│    -d '{"service":"${service}","status":"DOWN","code":${http_code}}'
└─────────────────────────────────────────────────────────────────┘
EOF
}

check_health() {
  local name="$1"
  local url="$2"
  local http_code body

  # curl: -s silent, -o /dev/null discard body, -w write out HTTP code
  # Use a 5-second timeout so the script doesn't hang on dead services.
  http_code=$(curl -s -o /tmp/health-body -w '%{http_code}' \
    --max-time 5 --connect-timeout 3 "$url" 2>/dev/null) || http_code="000"

  body=$(cat /tmp/health-body 2>/dev/null | head -c 200) || body="(empty)"
  rm -f /tmp/health-body

  if [[ "$http_code" == "200" ]]; then
    # Check if status field in JSON body is "UP"
    if echo "$body" | grep -q '"status":"UP"' 2>/dev/null; then
      log_ok "${name}  UP  (${url})"
    else
      log_warn "${name}  HTTP 200 but status not UP: ${body}"
    fi
  else
    log_fail "${name}  DOWN  HTTP ${http_code}  (${url})"
    send_webhook_alert "$name" "$url" "$http_code" "$body"
    return 1
  fi
}

run_check() {
  local failures=0

  echo ""
  log_info "═══════════════════════════════════════════════════════"
  log_info "  NeoBank Service Health Check"
  log_info "═══════════════════════════════════════════════════════"

  # Check backend services
  for entry in "${SERVICES[@]}"; do
    local name url
    name=$(echo "$entry" | cut -d'|' -f1 | xargs)
    url=$(echo "$entry" | cut -d'|' -f2 | xargs)
    check_health "$name" "$url" || failures=$((failures + 1))
  done

  # Check frontend
  check_health "Frontend" "$FRONTEND_HEALTH" || failures=$((failures + 1))

  echo ""
  if [[ $failures -gt 0 ]]; then
    log_fail "${failures} service(s) unhealthy"
    return 1
  else
    log_ok "All services healthy"
    return 0
  fi
}

# ── Main ─────────────────────────────────────────────────────────────────────

WATCH_INTERVAL=""
if [[ "${1:-}" == "--watch" ]]; then
  WATCH_INTERVAL="${2:-60}"
fi

if [[ -n "$WATCH_INTERVAL" ]]; then
  log_info "Running in watch mode (every ${WATCH_INTERVAL}s). Ctrl+C to stop."
  while true; do
    run_check || true
    sleep "$WATCH_INTERVAL"
  done
else
  run_check
  exit $?
fi
