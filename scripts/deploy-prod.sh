#!/usr/bin/env bash
# =============================================================================
# NeoBank — Zero-Downtime Production Deployment Script
# =============================================================================
# Orchestrates a rolling deployment of all 9 NeoBank microservices to
# Kubernetes using Kustomize production overlays.
#
# Deployment Order:
#   1. Apply all K8s resources (ConfigMaps, Secrets, Services first)
#   2. Wait for critical services (Auth, Core Banking) to reach Ready
#   3. Rolling update for remaining 7 business modules + Gateway
#
# Usage:
#   ./scripts/deploy-prod.sh                  # deploy to production
#   ./scripts/deploy-prod.sh --dry-run        # preview kubectl commands
#   ./scripts/deploy-prod.sh --namespace dev  # override namespace
# =============================================================================

set -euo pipefail

# ── Configuration ────────────────────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
KUSTOMIZE_OVERLAY="${PROJECT_ROOT}/k8s/overlays/prod"

NAMESPACE="neobank"
DRY_RUN=false
TIMEOUT_CRITICAL=300   # 5 minutes for critical services
TIMEOUT_BUSINESS=180   # 3 minutes per business module
POLL_INTERVAL=5        # seconds between readiness checks

# ── Service Definitions ──────────────────────────────────────────────────────
# Critical services that must be ready before proceeding
CRITICAL_SERVICES=("neobank-auth" "neobank-core-banking")

# Remaining business modules (rolling updated after critical services)
BUSINESS_SERVICES=(
  "neobank-onboarding"
  "neobank-lending"
  "neobank-cards"
  "neobank-fraud"
  "neobank-batch"
  "neobank-analytics"
  "neobank-gateway"
)

ALL_SERVICES=("${CRITICAL_SERVICES[@]}" "${BUSINESS_SERVICES[@]}")

# ── Colors & Formatting ──────────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ── Logging Functions ────────────────────────────────────────────────────────
log_info()    { echo -e "${CYAN}[INFO]${NC}  $*"; }
log_success() { echo -e "${GREEN}[OK]${NC}    $*"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $*"; }
log_step()    { echo -e "\n${BOLD}${CYAN}══ $* ══${NC}"; }

# ── Pre-flight Checks ────────────────────────────────────────────────────────
preflight() {
  log_step "Pre-flight Checks"

  # Check kubectl
  if ! command -v kubectl &>/dev/null; then
    log_error "kubectl is not installed or not in PATH"
    exit 1
  fi
  log_success "kubectl found: $(kubectl version --client --short 2>/dev/null || echo 'unknown')"

  # Check cluster connectivity
  if ! kubectl cluster-info &>/dev/null; then
    log_error "Cannot connect to Kubernetes cluster. Check your kubeconfig."
    exit 1
  fi
  log_success "Connected to cluster: $(kubectl config current-context)"

  # Check namespace exists or will be created
  if kubectl get namespace "$NAMESPACE" &>/dev/null; then
    log_success "Namespace '$NAMESPACE' exists"
  else
    log_warn "Namespace '$NAMESPACE' does not exist — will be created by Kustomize"
  fi

  # Check Kustomize overlay directory
  if [[ ! -d "$KUSTOMIZE_OVERLAY" ]]; then
    log_error "Kustomize overlay not found: $KUSTOMIZE_OVERLAY"
    exit 1
  fi
  log_success "Kustomize overlay found: $KUSTOMIZE_OVERLAY"

  # Validate Kustomize can build
  if ! kubectl kustomize "$KUSTOMIZE_OVERLAY" &>/dev/null; then
    log_error "Kustomize validation failed. Run 'kubectl kustomize $KUSTOMIZE_OVERLAY' for details."
    exit 1
  fi
  log_success "Kustomize manifest validates successfully"
}

# ── Dry Run Mode ─────────────────────────────────────────────────────────────
dry_run() {
  log_step "Dry Run — Rendering Kustomize Manifests"
  kubectl kustomize "$KUSTOMIZE_OVERLAY"
  echo ""
  log_info "To apply these manifests, run without --dry-run flag"
}

# ── Deploy All Resources ─────────────────────────────────────────────────────
deploy_all() {
  log_step "Applying Kustomize Production Overlay"

  if kubectl apply -k "$KUSTOMIZE_OVERLAY" --namespace="$NAMESPACE"; then
    log_success "All resources applied successfully"
  else
    log_error "Failed to apply resources"
    exit 1
  fi
}

# ── Wait for a Single Service to be Ready ────────────────────────────────────
wait_for_service() {
  local service_name="$1"
  local timeout="${2:-$TIMEOUT_CRITICAL}"
  local elapsed=0

  log_info "Waiting for $service_name to reach Ready status (timeout: ${timeout}s)..."

  while [[ $elapsed -lt $timeout ]]; do
    # Check if deployment exists
    if ! kubectl get deployment "$service_name" -n "$NAMESPACE" &>/dev/null; then
      log_warn "Deployment $service_name not found yet, retrying..."
      sleep "$POLL_INTERVAL"
      elapsed=$((elapsed + POLL_INTERVAL))
      continue
    fi

    # Check rollout status
    if kubectl rollout status deployment/"$service_name" -n "$NAMESPACE" --timeout="${POLL_INTERVAL}s" 2>/dev/null; then
      # Double-check pod readiness
      local ready_pods
      ready_pods=$(kubectl get deployment "$service_name" -n "$NAMESPACE" \
        -o jsonpath='{.status.readyReplicas}' 2>/dev/null || echo "0")
      local desired_pods
      desired_pods=$(kubectl get deployment "$service_name" -n "$NAMESPACE" \
        -o jsonpath='{.spec.replicas}' 2>/dev/null || echo "0")

      if [[ "$ready_pods" -gt 0 && "$ready_pods" -ge "$desired_pods" ]]; then
        log_success "$service_name is READY ($ready_pods/$desired_pods pods)"
        return 0
      fi
    fi

    sleep "$POLL_INTERVAL"
    elapsed=$((elapsed + POLL_INTERVAL))
  done

  log_error "$service_name did not become ready within ${timeout}s"
  log_info "Current status:"
  kubectl get pods -n "$NAMESPACE" -l "app.kubernetes.io/name=$service_name" --show-labels 2>/dev/null || true
  kubectl describe deployment "$service_name" -n "$NAMESPACE" 2>/dev/null | tail -20 || true
  return 1
}

# ── Wait for Critical Services ───────────────────────────────────────────────
wait_critical_services() {
  log_step "Waiting for Critical Services"

  for svc in "${CRITICAL_SERVICES[@]}"; do
    if ! wait_for_service "$svc" "$TIMEOUT_CRITICAL"; then
      log_error "Critical service '$svc' failed to start. Aborting deployment."
      log_info "Run 'kubectl logs -n $NAMESPACE -l app.kubernetes.io/name=$svc --tail=50' for details"
      exit 1
    fi
  done

  log_success "All critical services are operational"
}

# ── Rolling Update Business Modules ─────────────────────────────────────────
rolling_update_business() {
  log_step "Rolling Update — Business Modules"

  local failed=()

  for svc in "${BUSINESS_SERVICES[@]}"; do
    log_info "Processing: $svc"

    # Trigger a rollout by restarting the deployment (ensures new image is pulled)
    kubectl rollout restart deployment/"$svc" -n "$NAMESPACE" 2>/dev/null || true

    if wait_for_service "$svc" "$TIMEOUT_BUSINESS"; then
      log_success "$svc rolled out successfully"
    else
      log_warn "$svc rollout timed out — continuing with next service"
      failed+=("$svc")
    fi
  done

  if [[ ${#failed[@]} -gt 0 ]]; then
    log_warn "The following services had rollout timeouts (check manually):"
    for svc in "${failed[@]}"; do
      echo "  - $svc"
    done
  else
    log_success "All business modules rolled out successfully"
  fi
}

# ── Post-Deployment Status ───────────────────────────────────────────────────
post_deploy_status() {
  log_step "Post-Deployment Status"

  echo ""
  log_info "Pod Status:"
  kubectl get pods -n "$NAMESPACE" -l "app.kubernetes.io/part-of=neobank" || true

  echo ""
  log_info "Service Endpoints:"
  kubectl get svc -n "$NAMESPACE" -l "app.kubernetes.io/part-of=neobank" || true

  echo ""
  log_info "Deployment Summary:"
  kubectl get deployments -n "$NAMESPACE" -l "app.kubernetes.io/part-of=neobank" || true
}

# ── Main ─────────────────────────────────────────────────────────────────────
main() {
  echo ""
  echo -e "${BOLD}╔══════════════════════════════════════════════════════════════╗${NC}"
  echo -e "${BOLD}║   NeoBank — Zero-Downtime Production Deployment            ║${NC}"
  echo -e "${BOLD}╚══════════════════════════════════════════════════════════════╝${NC}"
  echo ""
  echo -e "  Target Namespace: ${BOLD}$NAMESPACE${NC}"
  echo -e "  Kustomize Overlay: ${BOLD}$KUSTOMIZE_OVERLAY${NC}"
  echo -e "  Total Services: ${BOLD}${#ALL_SERVICES[@]}${NC} (${#CRITICAL_SERVICES[@]} critical, ${#BUSINESS_SERVICES[@]} business)"
  echo ""

  # Parse arguments
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --dry-run)
        DRY_RUN=true
        shift
        ;;
      --namespace|-n)
        NAMESPACE="$2"
        shift 2
        ;;
      --timeout-critical)
        TIMEOUT_CRITICAL="$2"
        shift 2
        ;;
      --timeout-business)
        TIMEOUT_BUSINESS="$2"
        shift 2
        ;;
      -h|--help)
        echo "Usage: $0 [--dry-run] [--namespace <ns>] [--timeout-critical <s>] [--timeout-business <s>]"
        exit 0
        ;;
      *)
        log_error "Unknown option: $1"
        exit 1
        ;;
    esac
  done

  if [[ "$DRY_RUN" == true ]]; then
    dry_run
    exit 0
  fi

  # Execute deployment pipeline
  preflight
  deploy_all
  wait_critical_services
  rolling_update_business
  post_deploy_status

  echo ""
  echo -e "${GREEN}${BOLD}✅ NeoBank production deployment complete!${NC}"
  echo -e "${CYAN}   Run ./scripts/verify-deployment.sh to verify all services.${NC}"
  echo ""
}

main "$@"
