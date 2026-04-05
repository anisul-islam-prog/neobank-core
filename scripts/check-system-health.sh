#!/usr/bin/env bash
# =============================================================================
# NeoBank Production System Health Check
# =============================================================================
# Pings health endpoints of the Frontend, Reactive Gateway, and all downstream
# microservice modules. Outputs a color-coded status report and sends a mock
# JSON payload to a simulated Slack webhook if any service is DOWN.
#
# Usage:
#   ./scripts/check-system-health.sh                  # single check
#   ./scripts/check-system-health.sh --watch 30      # check every 30s
#   ./scripts/check-system-health.sh --json           # output JSON only
# =============================================================================

set -euo pipefail

# ── Configuration ────────────────────────────────────────────────────────────
FRONTEND_URL="${NEOBANK_FRONTEND_URL:-http://localhost:3000}"
GATEWAY_URL="${NEOBANK_GATEWAY_URL:-http://localhost:8080}"

# Core entry-point services (direct health pings)
CORE_SERVICES=(
  "Frontend       |${FRONTEND_URL}/api/health"
  "ReactiveGateway|${GATEWAY_URL}/actuator/health"
)

# Downstream microservices (reachable via their own actuator endpoints)
# In a production Kubernetes / ECS deployment these would resolve via
# internal DNS. Here we default to localhost with per-service ports.
DOWNSTREAM_SERVICES=(
  "Auth           |http://localhost:8081/actuator/health"
  "Onboarding     |http://localhost:8082/actuator/health"
  "CoreBanking    |http://localhost:8083/actuator/health"
  "Lending        |http://localhost:8084/actuator/health"
  "Cards          |http://localhost:8085/actuator/health"
  "Fraud          |http://localhost:8086/actuator/health"
  "Batch          |http://localhost:8087/actuator/health"
  "Analytics      |http://localhost:8088/actuator/health"
)

# Observability stack health endpoints
OBSERVABILITY_SERVICES=(
  "Prometheus     |http://localhost:9090/-/healthy"
  "Grafana        |http://localhost:3003/api/health"
  "Tempo          |http://localhost:3200/status"
  "Loki           |http://localhost:3100/ready"
  "OTelCollector  |http://localhost:13133/"
)

# Slack webhook (simulated — payload printed to STDOUT)
SLACK_WEBHOOK_URL="${SLACK_WEBHOOK_URL:-https://hooks.slack.com/services/XXX/YYY/ZZZ}"

# Timeouts (seconds)
CONNECT_TIMEOUT=3
MAX_TIME=5

# ── ANSI Colors ──────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# ── State ────────────────────────────────────────────────────────────────────
declare -a DOWN_SERVICES=()
JSON_MODE=false

# ── Functions ────────────────────────────────────────────────────────────────

timestamp() { date '+%Y-%m-%dT%H:%M:%S%z'; }

now_human() { date '+%Y-%m-%d %H:%M:%S %Z'; }

log_section() { echo -e "\n${BOLD}${CYAN}── $* ──${NC}"; }

# probe URL → sets HTTP_CODE and BODY in caller scope
probe() {
  local url="$1"
  HTTP_CODE=$(curl -s -o /tmp/_neobank_health_body -w '%{http_code}' \
    --max-time "$MAX_TIME" --connect-timeout "$CONNECT_TIMEOUT" \
    "$url" 2>/dev/null) || HTTP_CODE="000"
  BODY=$(cat /tmp/_neobank_health_body 2>/dev/null | head -c 500) || BODY="(empty)"
  rm -f /tmp/_neobank_health_body
}

# is_up HTTP_CODE BODY → returns 0 if healthy
is_up() {
  local code="$1" body="$2"
  [[ "$code" == "200" ]] && echo "$body" | grep -qi '"status"\s*:\s*"UP"' 2>/dev/null
}

# check_service NAME URL → prints result, appends to DOWN_SERVICES if failed
check_service() {
  local name="$1" url="$2"
  probe "$url"

  if is_up "$HTTP_CODE" "$BODY"; then
    if [[ "$JSON_MODE" == false ]]; then
      echo -e "  ${GREEN}✔${NC}  ${name}  UP  (${HTTP_CODE})"
    fi
    return 0
  else
    DOWN_SERVICES+=("$name")
    if [[ "$JSON_MODE" == false ]]; then
      echo -e "  ${RED}✘${NC}  ${name}  DOWN  (HTTP ${HTTP_CODE})"
    fi
    return 1
  fi
}

# send_slack_alert JSON payload
send_slack_alert() {
  local payload="$1"

  if [[ "$JSON_MODE" == false ]]; then
    echo ""
    echo -e "${RED}${BOLD}┌─────────────────────────────────────────────────────────────┐${NC}"
    echo -e "${RED}│  ⚠️  NEOBANK SLACK ALERT SIMULATION                               │${NC}"
    echo -e "${RED}├─────────────────────────────────────────────────────────────┤${NC}"
    echo -e "${RED}│  Webhook : ${SLACK_WEBHOOK_URL}${NC}"
    echo -e "${RED}│  Payload :${NC}"
    echo -e "${RED}│  ${payload}${NC}"
    echo -e "${RED}│  Time    : $(now_human)${NC}"
    echo -e "${RED}├─────────────────────────────────────────────────────────────┤${NC}"
    echo -e "${RED}│  curl -X POST \"\${SLACK_WEBHOOK_URL}\" \\${NC}"
    echo -e "${RED}│    -H 'Content-Type: application/json' \\${NC}"
    echo -e "${RED}│    -d '${payload}'${NC}"
    echo -e "${RED}└─────────────────────────────────────────────────────────────┘${NC}"
    echo ""
  fi

  # Simulated send — uncomment the curl below for real Slack integration:
  # curl -s -X POST "$SLACK_WEBHOOK_URL" \
  #   -H 'Content-Type: application/json' \
  #   -d "$payload" >/dev/null 2>&1
}

# ── Main Check Routine ───────────────────────────────────────────────────────

run_check() {
  DOWN_SERVICES=()

  if [[ "$JSON_MODE" == false ]]; then
    echo ""
    log_section "NeoBank System Health Check — $(now_human)"
  fi

  # 1. Core entry-point services
  if [[ "$JSON_MODE" == false ]]; then log_section "Core Services"; fi
  for entry in "${CORE_SERVICES[@]}"; do
    local name url
    name=$(echo "$entry" | cut -d'|' -f1 | xargs)
    url=$(echo "$entry" | cut -d'|' -f2 | xargs)
    check_service "$name" "$url" || true
  done

  # 2. Downstream microservices
  if [[ "$JSON_MODE" == false ]]; then log_section "Downstream Services"; fi
  for entry in "${DOWNSTREAM_SERVICES[@]}"; do
    local name url
    name=$(echo "$entry" | cut -d'|' -f1 | xargs)
    url=$(echo "$entry" | cut -d'|' -f2 | xargs)
    check_service "$name" "$url" || true
  done

  # 3. Observability stack
  if [[ "$JSON_MODE" == false ]]; then log_section "Observability Stack (LGT + OTel)"; fi
  for entry in "${OBSERVABILITY_SERVICES[@]}"; do
    local name url
    name=$(echo "$entry" | cut -d'|' -f1 | xargs)
    url=$(echo "$entry" | cut -d'|' -f2 | xargs)
    check_service "$name" "$url" || true
  done

  # ── Summary ──────────────────────────────────────────────────────────────
  local total=$(( ${#CORE_SERVICES[@]} + ${#DOWNSTREAM_SERVICES[@]} + ${#OBSERVABILITY_SERVICES[@]} ))
  local healthy=$(( total - ${#DOWN_SERVICES[@]} ))

  if [[ "$JSON_MODE" == false ]]; then
    echo ""
    echo -e "${BOLD}Summary:${NC}  ${GREEN}${healthy} UP${NC}  /  ${RED}${#DOWN_SERVICES[@]} DOWN${NC}  /  ${total} total"
  fi

  # Alert if any service is DOWN
  if [[ ${#DOWN_SERVICES[@]} -gt 0 ]]; then
    local alert_payload
    alert_payload=$(printf '{"text":"🚨 NeoBank Alert","blocks":[{"type":"header","text":{"type":"plain_text","text":"🚨 NeoBank Health Alert"}},{"type":"section","fields":[{"type":"mrkdwn","text":"*Down Services:*"},{"type":"mrkdwn","text":"%s"}]},{"type":"section","fields":[{"type":"mrkdwn","text":"*Timestamp:*"},{"type":"mrkdwn","text":"%s"}]}]}' \
      "$(IFS=', '; echo "${DOWN_SERVICES[*]}")" \
      "$(timestamp)")

    send_slack_alert "$alert_payload"
    return 1
  fi

  if [[ "$JSON_MODE" == false ]]; then
    echo -e "\n${GREEN}${BOLD}All systems operational.${NC}"
  fi
  return 0
}

# ── Entry Point ──────────────────────────────────────────────────────────────

WATCH_INTERVAL=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --watch)
      WATCH_INTERVAL="${2:-60}"
      shift; [[ $# -gt 0 ]] && shift || true
      ;;
    --json)
      JSON_MODE=true
      shift
      ;;
    -h|--help)
      echo "Usage: $0 [--watch <seconds>] [--json]"
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      exit 1
      ;;
  esac
done

if [[ -n "$WATCH_INTERVAL" ]]; then
  if [[ "$JSON_MODE" == false ]]; then
    echo -e "${CYAN}Watch mode: checking every ${WATCH_INTERVAL}s. Ctrl+C to stop.${NC}"
  fi
  while true; do
    run_check || true
    sleep "$WATCH_INTERVAL"
  done
else
  run_check
  exit $?
fi
