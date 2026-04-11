#!/usr/bin/env bash
# =============================================================================
# NeoBank — Deployment Verification & Smoke Test Script
# =============================================================================
# Verifies that all NeoBank services are running correctly after deployment.
#
# Tests performed:
#   1. Gateway /actuator/health — confirms the entry point is UP
#   2. Gateway /actuator/health per downstream service (circuit breaker state)
#   3. Fallback routes — verifies correct JSON when a service is scaled to 0
#   4. Pod readiness — all pods in Running + Ready state
#   5. Service endpoint validation — ClusterIP services are reachable
#
# Usage:
#   ./scripts/verify-deployment.sh                    # full verification
#   ./scripts/verify-deployment.sh --quick            # gateway health only
#   ./scripts/verify-deployment.sh --scale-to-zero    # test fallback routes
#   ./scripts/verify-deployment.sh --namespace dev    # override namespace
# =============================================================================

set -euo pipefail

# ── Configuration ────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

NAMESPACE="neobank"
QUICK_MODE=false
SCALE_TO_ZERO_MODE=false
GATEWAY_HOST=""            # Auto-detected or set via env
GATEWAY_PORT=8080
CONNECT_TIMEOUT=5
MAX_TIME=10

# ── Service Registry ─────────────────────────────────────────────────────────
declare -A SERVICE_PORTS=(
  [neobank-gateway]=8080
  [neobank-auth]=8081
  [neobank-onboarding]=8082
  [neobank-core-banking]=8083
  [neobank-lending]=8084
  [neobank-cards]=8085
  [neobank-fraud]=8086
  [neobank-batch]=8087
  [neobank-analytics]=8088
)

ALL_SERVICES=(
  "neobank-gateway"
  "neobank-auth"
  "neobank-onboarding"
  "neobank-core-banking"
  "neobank-lending"
  "neobank-cards"
  "neobank-fraud"
  "neobank-batch"
  "neobank-analytics"
)

# ── Colors ───────────────────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ── State ────────────────────────────────────────────────────────────────────
declare -a FAILURES=()
declare -a PASSES=()

# ── Logging ──────────────────────────────────────────────────────────────────
log_info()    { echo -e "${CYAN}[INFO]${NC}  $*"; }
log_success() { echo -e "${GREEN}[PASS]${NC}  $*"; PASSES+=("$*"); }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_fail()    { echo -e "${RED}[FAIL]${NC}  $*"; FAILURES+=("$*"); }
log_step()    { echo -e "\n${BOLD}${CYAN}══ $* ══${NC}"; }

# ── Detect Gateway URL ───────────────────────────────────────────────────────
detect_gateway() {
  if [[ -n "${NEOBANK_GATEWAY_URL:-}" ]]; then
    GATEWAY_HOST="$NEOBANK_GATEWAY_URL"
    log_info "Using gateway URL from env: $GATEWAY_HOST"
    return
  fi

  # Try to get from Kubernetes Ingress
  if command -v kubectl &>/dev/null && kubectl cluster-info &>/dev/null 2>&1; then
    local ingress_ip
    ingress_ip=$(kubectl get ingress neobank-ingress -n "$NAMESPACE" \
      -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>/dev/null || echo "")

    if [[ -n "$ingress_ip" ]]; then
      GATEWAY_HOST="http://${ingress_ip}"
      log_info "Detected gateway via Ingress: $GATEWAY_HOST"
      return
    fi

    # Fallback: use port-forward
    log_warn "No Ingress IP found. Use NEOBANK_GATEWAY_URL env or port-forward:"
    log_info "  kubectl port-forward -n $NAMESPACE svc/neobank-gateway 8080:8080 &"
    GATEWAY_HOST="http://localhost:${GATEWAY_PORT}"
    log_info "Defaulting to: $GATEWAY_HOST"
    return
  fi

  # No kubectl — assume localhost
  GATEWAY_HOST="http://localhost:${GATEWAY_PORT}"
  log_info "No kubectl available. Defaulting to: $GATEWAY_HOST"
}

# ── Test 1: Gateway Health ───────────────────────────────────────────────────
test_gateway_health() {
  log_step "Test 1: Gateway /actuator/health"

  local url="${GATEWAY_HOST}/actuator/health"
  log_info "GET $url"

  local http_code body
  http_code=$(curl -s -o /tmp/_neobank_verify_body -w '%{http_code}' \
    --max-time "$MAX_TIME" --connect-timeout "$CONNECT_TIMEOUT" \
    "$url" 2>/dev/null) || http_code="000"
  body=$(cat /tmp/_neobank_verify_body 2>/dev/null | head -c 1000) || body="(empty)"
  rm -f /tmp/_neobank_verify_body

  if [[ "$http_code" == "200" ]]; then
    if echo "$body" | grep -qi '"status"\s*:\s*"UP"' 2>/dev/null; then
      log_success "Gateway is UP (HTTP $http_code)"
      echo -e "  Response: ${GREEN}${body}${NC}"
    else
      log_fail "Gateway returned unexpected body: $body"
    fi
  else
    log_fail "Gateway health returned HTTP $http_code"
  fi
}

# ── Test 2: Gateway Info ─────────────────────────────────────────────────────
test_gateway_info() {
  log_step "Test 2: Gateway /actuator/info"

  local url="${GATEWAY_HOST}/actuator/info"
  log_info "GET $url"

  local http_code
  http_code=$(curl -s -o /dev/null -w '%{http_code}' \
    --max-time "$MAX_TIME" --connect-timeout "$CONNECT_TIMEOUT" \
    "$url" 2>/dev/null) || http_code="000"

  if [[ "$http_code" == "200" ]]; then
    log_success "Gateway /actuator/info accessible (HTTP $http_code)"
  else
    log_warn "Gateway /actuator/info returned HTTP $http_code (may be disabled)"
  fi
}

# ── Test 3: Fallback Routes (Circuit Breaker Fallback) ───────────────────────
test_fallback_routes() {
  log_step "Test 3: Fallback Routes (Circuit Breaker Behavior)"

  # These routes should return a fallback response when downstream
  # services are unavailable. The Gateway has Resilience4j circuit
  # breakers configured for each downstream service.

  local fallback_paths=(
    "/api/auth/health"
    "/api/onboarding/health"
    "/api/core-banking/health"
    "/api/lending/health"
    "/api/cards/health"
    "/api/fraud/health"
    "/api/batch/health"
    "/api/analytics/health"
  )

  local fallback_count=0
  local unreachable_count=0

  for path in "${fallback_paths[@]}"; do
    local url="${GATEWAY_HOST}${path}"
    local http_code body
    http_code=$(curl -s -o /tmp/_neobank_fallback_body -w '%{http_code}' \
      --max-time "$MAX_TIME" --connect-timeout "$CONNECT_TIMEOUT" \
      "$url" 2>/dev/null) || http_code="000"
    body=$(cat /tmp/_neobank_fallback_body 2>/dev/null | head -c 500) || body="(empty)"
    rm -f /tmp/_neobank_fallback_body

    # A fallback response typically contains "fallback", "unavailable", or
    # a circuit breaker open message. Gateway returns 503 with fallback JSON.
    if [[ "$http_code" == "503" || "$http_code" == "200" ]]; then
      if echo "$body" | grep -qi 'fallback\|unavailable\|circuit\|degraded' 2>/dev/null; then
        log_success "Fallback active for $path (HTTP $http_code)"
        fallback_count=$((fallback_count + 1))
      else
        log_success "Route reachable: $path (HTTP $http_code)"
      fi
    elif [[ "$http_code" == "000" ]]; then
      log_fail "Route unreachable: $path (connection failed)"
      unreachable_count=$((unreachable_count + 1))
    else
      log_warn "Unexpected response for $path: HTTP $http_code"
    fi
  done

  echo ""
  log_info "Fallback Summary: $fallback_count fallbacks active, $unreachable_count unreachable"
}

# ── Test 4: Pod Readiness (Kubernetes) ──────────────────────────────────────
test_pod_readiness() {
  log_step "Test 4: Pod Readiness (Kubernetes)"

  if ! command -v kubectl &>/dev/null; then
    log_warn "kubectl not available — skipping pod readiness check"
    return
  fi

  if ! kubectl cluster-info &>/dev/null 2>&1; then
    log_warn "Cannot connect to Kubernetes cluster — skipping pod readiness"
    return
  fi

  local total_pods=0
  local ready_pods=0
  local not_ready=0

  for svc in "${ALL_SERVICES[@]}"; do
    local pods
    pods=$(kubectl get pods -n "$NAMESPACE" \
      -l "app.kubernetes.io/name=$svc" \
      -o jsonpath='{range .items[*]}{.metadata.name}{"\t"}{.status.phase}{"\t"}{.status.containerStatuses[0].ready}{"\n"}{end}' 2>/dev/null || echo "")

    if [[ -z "$pods" ]]; then
      log_warn "No pods found for $svc"
      continue
    fi

    while IFS=$'\t' read -r pod_name phase ready; do
      [[ -z "$pod_name" ]] && continue
      total_pods=$((total_pods + 1))

      if [[ "$phase" == "Running" && "$ready" == "true" ]]; then
        ready_pods=$((ready_pods + 1))
        log_success "Pod: $pod_name ($phase, ready=$ready)"
      else
        not_ready=$((not_ready + 1))
        log_fail "Pod: $pod_name ($phase, ready=$ready)"
      fi
    done <<< "$pods"
  done

  echo ""
  log_info "Pod Summary: $ready_pods/$total_pods ready, $not_ready not ready"

  if [[ $not_ready -gt 0 ]]; then
    log_fail "$not_ready pods are not ready"
  else
    log_success "All pods are healthy"
  fi
}

# ── Test 5: Service Endpoints ────────────────────────────────────────────────
test_service_endpoints() {
  log_step "Test 5: Service Endpoints (Kubernetes)"

  if ! command -v kubectl &>/dev/null; then
    log_warn "kubectl not available — skipping service endpoint check"
    return
  fi

  if ! kubectl cluster-info &>/dev/null 2>&1; then
    log_warn "Cannot connect to Kubernetes cluster — skipping service check"
    return
  fi

  for svc in "${ALL_SERVICES[@]}"; do
    local endpoints
    endpoints=$(kubectl get endpoints "$svc" -n "$NAMESPACE" \
      -o jsonpath='{.subsets[*].addresses[*].ip}' 2>/dev/null || echo "")

    if [[ -n "$endpoints" ]]; then
      log_success "$svc has endpoints: $endpoints"
    else
      log_fail "$svc has NO endpoints"
    fi
  done
}

# ── Test 6: Scale-to-Zero Fallback Verification ─────────────────────────────
test_scale_to_zero() {
  log_step "Test 6: Scale-to-Zero Fallback Verification"

  # Scale Onboarding to 0 replicas, verify fallback response
  local test_service="neobank-onboarding"
  local original_replicas

  log_info "Scaling $test_service to 0 replicas..."

  if ! command -v kubectl &>/dev/null || ! kubectl cluster-info &>/dev/null 2>&1; then
    log_warn "kubectl not available — skipping scale-to-zero test"
    return
  fi

  # Save original replica count
  original_replicas=$(kubectl get deployment "$test_service" -n "$NAMESPACE" \
    -o jsonpath='{.spec.replicas}' 2>/dev/null || echo "1")

  log_info "Original replicas: $original_replicas"

  # Scale to 0
  kubectl scale deployment "$test_service" -n "$NAMESPACE" --replicas=0 2>/dev/null || {
    log_fail "Failed to scale $test_service to 0"
    return
  }

  # Wait for pods to terminate
  log_info "Waiting for pods to terminate..."
  sleep 15

  # Test fallback
  local url="${GATEWAY_HOST}/api/onboarding/health"
  log_info "GET $url (service scaled to 0)"

  local http_code body
  http_code=$(curl -s -o /tmp/_neobank_scale_body -w '%{http_code}' \
    --max-time "$MAX_TIME" --connect-timeout "$CONNECT_TIMEOUT" \
    "$url" 2>/dev/null) || http_code="000"
  body=$(cat /tmp/_neobank_scale_body 2>/dev/null | head -c 500) || body="(empty)"
  rm -f /tmp/_neobank_scale_body

  if [[ "$http_code" == "503" ]]; then
    if echo "$body" | grep -qi 'fallback\|unavailable\|circuit' 2>/dev/null; then
      log_success "Fallback response correctly for scaled-to-zero service"
      echo -e "  Response: ${YELLOW}${body}${NC}"
    else
      log_warn "Got 503 but body doesn't look like a fallback: $body"
    fi
  elif [[ "$http_code" == "000" ]]; then
    log_fail "Gateway unreachable when service scaled to 0"
  else
    log_warn "Unexpected HTTP $http_code when service scaled to 0"
  fi

  # Restore original replica count
  log_info "Restoring $test_service to $original_replicas replicas..."
  kubectl scale deployment "$test_service" -n "$NAMESPACE" --replicas="$original_replicas" 2>/dev/null || true

  log_info "Waiting for $test_service to recover..."
  kubectl rollout status deployment/"$test_service" -n "$NAMESPACE" --timeout=120s 2>/dev/null || {
    log_warn "$test_service recovery timed out — check manually"
  }
}

# ── Quick Mode ───────────────────────────────────────────────────────────────
run_quick() {
  detect_gateway
  test_gateway_health
}

# ── Full Verification ────────────────────────────────────────────────────────
run_full() {
  detect_gateway
  test_gateway_health
  test_gateway_info
  test_fallback_routes
  test_pod_readiness
  test_service_endpoints
}

# ── Scale-to-Zero Mode ───────────────────────────────────────────────────────
run_scale_to_zero() {
  detect_gateway
  test_scale_to_zero
}

# ── Final Report ─────────────────────────────────────────────────────────────
report() {
  log_step "Verification Report"

  local total=$(( ${#PASSES[@]} + ${#FAILURES[@]} ))

  echo ""
  echo -e "${BOLD}Results:${NC}"
  echo -e "  ${GREEN}Passed: ${#PASSES[@]}${NC}"
  echo -e "  ${RED}Failed: ${#FAILURES[@]}${NC}"
  echo -e "  Total:  $total"
  echo ""

  if [[ ${#FAILURES[@]} -gt 0 ]]; then
    echo -e "${RED}${BOLD}Failures:${NC}"
    for f in "${FAILURES[@]}"; do
      echo -e "  ${RED}✗${NC} $f"
    done
    echo ""
  fi

  if [[ ${#FAILURES[@]} -eq 0 ]]; then
    echo -e "${GREEN}${BOLD}✅ All verification checks passed!${NC}"
  else
    echo -e "${RED}${BOLD}⚠️  Some checks failed — review the output above.${NC}"
    exit 1
  fi
}

# ── Main ─────────────────────────────────────────────────────────────────────
main() {
  echo ""
  echo -e "${BOLD}╔══════════════════════════════════════════════════════════════╗${NC}"
  echo -e "${BOLD}║   NeoBank — Deployment Verification & Smoke Tests          ║${NC}"
  echo -e "${BOLD}╚══════════════════════════════════════════════════════════════╝${NC}"
  echo ""

  # Parse arguments
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --quick|-q)
        QUICK_MODE=true
        shift
        ;;
      --scale-to-zero|-s)
        SCALE_TO_ZERO_MODE=true
        shift
        ;;
      --namespace|-n)
        NAMESPACE="$2"
        shift 2
        ;;
      --gateway-url)
        NEOBANK_GATEWAY_URL="$2"
        shift 2
        ;;
      -h|--help)
        echo "Usage: $0 [--quick] [--scale-to-zero] [--namespace <ns>] [--gateway-url <url>]"
        exit 0
        ;;
      *)
        log_fail "Unknown option: $1"
        exit 1
        ;;
    esac
  done

  if [[ "$QUICK_MODE" == true ]]; then
    run_quick
  elif [[ "$SCALE_TO_ZERO_MODE" == true ]]; then
    run_scale_to_zero
  else
    run_full
  fi

  report
}

main "$@"
