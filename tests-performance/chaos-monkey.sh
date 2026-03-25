#!/bin/bash
# =============================================================================
# NeoBank Chaos Monkey
# =============================================================================
# Randomly stops backend service containers for 30 seconds to test resilience.
# Runs in a loop every 2 minutes.
#
# Usage:
#   ./chaos-monkey.sh              # Run continuously
#   ./chaos-monkey.sh --once       # Run once and exit
#   ./chaos-monkey.sh --dry-run    # Show what would happen without stopping
#
# Excluded services (never stopped):
#   - PostgreSQL containers
#   - Redis containers
#   - Observability stack (Prometheus, Grafana, Loki, Tempo)
#   - Docker infrastructure (Ryuk)
# =============================================================================

set -e

# Configuration
CHAOS_INTERVAL=${CHAOS_INTERVAL:-120}  # 2 minutes between chaos events
CHAOS_DURATION=${CHAOS_DURATION:-30}   # 30 seconds downtime
LOG_FILE="${LOG_FILE:-chaos-monkey.log}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Services that should NEVER be stopped
EXCLUDED_SERVICES=(
    "postgres"
    "redis"
    "prometheus"
    "grafana"
    "loki"
    "tempo"
    "promtail"
    "ollama"
    "ryuk"
)

# Backend services that CAN be stopped for chaos testing
CHAOS_TARGET_SERVICES=(
    "neobank-auth"
    "neobank-onboarding"
    "neobank-core-banking"
    "neobank-lending"
    "neobank-cards"
    "neobank-batch"
    "neobank-analytics"
    "neobank-gateway"
)

# =============================================================================
# Utility Functions
# =============================================================================

log() {
    local level=$1
    shift
    local message="$*"
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo -e "${timestamp} [${level}] ${message}" | tee -a "$LOG_FILE"
}

log_info() {
    log "${BLUE}INFO${NC}" "$@"
}

log_warn() {
    log "${YELLOW}WARN${NC}" "$@"
}

log_error() {
    log "${RED}ERROR${NC}" "$@"
}

log_success() {
    log "${GREEN}SUCCESS${NC}" "$@"
}

# =============================================================================
# Core Functions
# =============================================================================

# Check if a service is in the excluded list
is_excluded() {
    local service=$1
    for excluded in "${EXCLUDED_SERVICES[@]}"; do
        if [[ "$service" == *"$excluded"* ]]; then
            return 0
        fi
    done
    return 1
}

# Check if a service is a valid chaos target
is_chaos_target() {
    local service=$1
    for target in "${CHAOS_TARGET_SERVICES[@]}"; do
        if [[ "$service" == *"$target"* ]]; then
            return 0
        fi
    done
    return 1
}

# Get list of running NeoBank service containers
get_running_services() {
    docker-compose ps --format json 2>/dev/null | \
        jq -r '.[] | select(.Running == true) | .Service' 2>/dev/null || \
    docker ps --format '{{.Names}}' | grep -E "neobank-" | grep -v "postgres" | grep -v "redis"
}

# Pick a random service to stop
pick_random_service() {
    local services=("$@")
    if [ ${#services[@]} -eq 0 ]; then
        return 1
    fi
    local index=$((RANDOM % ${#services[@]}))
    echo "${services[$index]}"
}

# Stop a service
stop_service() {
    local service=$1
    log_info "Stopping service: ${service}"
    
    # Try docker-compose first, then docker stop
    if docker-compose ps | grep -q "$service"; then
        docker-compose stop "$service" 2>/dev/null || \
        docker stop "$service" 2>/dev/null
    else
        docker stop "$service" 2>/dev/null
    fi
    
    if [ $? -eq 0 ]; then
        log_success "Service ${service} stopped"
        return 0
    else
        log_error "Failed to stop service ${service}"
        return 1
    fi
}

# Start a service
start_service() {
    local service=$1
    log_info "Starting service: ${service}"
    
    # Try docker-compose first, then docker start
    if docker-compose ps | grep -q "$service"; then
        docker-compose start "$service" 2>/dev/null || \
        docker start "$service" 2>/dev/null
    else
        docker start "$service" 2>/dev/null
    fi
    
    if [ $? -eq 0 ]; then
        log_success "Service ${service} started"
        return 0
    else
        log_error "Failed to start service ${service}"
        return 1
    fi
}

# Wait for service to be healthy
wait_for_healthy() {
    local service=$1
    local timeout=${2:-60}
    local elapsed=0
    
    log_info "Waiting for ${service} to become healthy (timeout: ${timeout}s)"
    
    while [ $elapsed -lt $timeout ]; do
        if docker-compose ps "$service" 2>/dev/null | grep -q "(healthy)"; then
            log_success "Service ${service} is healthy"
            return 0
        fi
        
        # Also check if container is running (some services don't have health checks)
        if docker ps --format '{{.Names}}' | grep -q "$service"; then
            if docker inspect --format='{{.State.Health.Status}}' "$service" 2>/dev/null | grep -q "healthy"; then
                log_success "Service ${service} is healthy"
                return 0
            fi
            # If no health check defined, consider running as healthy
            if docker inspect --format='{{.State.Health.Status}}' "$service" 2>/dev/null | grep -q "none"; then
                sleep 5  # Give it a moment to initialize
                log_success "Service ${service} is running (no health check defined)"
                return 0
            fi
        fi
        
        sleep 2
        elapsed=$((elapsed + 2))
    done
    
    log_warn "Service ${service} did not become healthy within ${timeout}s"
    return 1
}

# =============================================================================
# Chaos Functions
# =============================================================================

# Run a single chaos event
run_chaos_event() {
    log_info "=========================================="
    log_info "🐵 CHAOS MONKEY EVENT"
    log_info "=========================================="
    
    # Get running services
    local running_services=($(get_running_services))
    
    if [ ${#running_services[@]} -eq 0 ]; then
        log_error "No running services found. Is the stack running?"
        return 1
    fi
    
    # Filter to only chaos targets
    local chaos_targets=()
    for service in "${running_services[@]}"; do
        if is_chaos_target "$service"; then
            chaos_targets+=("$service")
        fi
    done
    
    if [ ${#chaos_targets[@]} -eq 0 ]; then
        log_error "No valid chaos targets found"
        return 1
    fi
    
    # Pick random service
    local target=$(pick_random_service "${chaos_targets[@]}")
    log_info "🎯 Selected target: ${target}"
    
    # Record start time
    local start_time=$(date +%s)
    
    # Stop the service
    if ! stop_service "$target"; then
        return 1
    fi
    
    # Log chaos event
    log_warn "⚠️  CHAOS: ${target} is DOWN (started at $(date '+%H:%M:%S'))"
    echo "{\"event\":\"chaos_start\",\"service\":\"${target}\",\"timestamp\":\"$(date -Iseconds)\"}" >> chaos-events.jsonl
    
    # Wait for chaos duration
    log_info "⏱️  Waiting ${CHAOS_DURATION} seconds..."
    sleep "$CHAOS_DURATION"
    
    # Start the service
    if ! start_service "$target"; then
        return 1
    fi
    
    # Wait for healthy
    wait_for_healthy "$target" 60
    
    # Record end time
    local end_time=$(date +%s)
    local downtime=$((end_time - start_time))
    
    # Log recovery
    log_success "✅ RECOVERY: ${target} is back online (downtime: ${downtime}s)"
    echo "{\"event\":\"chaos_end\",\"service\":\"${target}\",\"downtime\":${downtime},\"timestamp\":\"$(date -Iseconds)\"}" >> chaos-events.jsonl
    
    log_info "=========================================="
    log_info "Chaos event complete. Next event in ${CHAOS_INTERVAL} seconds."
    log_info "=========================================="
    
    return 0
}

# Dry run - show what would happen
dry_run() {
    log_info "🔍 DRY RUN MODE"
    log_info "=========================================="
    
    local running_services=($(get_running_services))
    
    log_info "Running services:"
    for service in "${running_services[@]}"; do
        if is_excluded "$service"; then
            log_info "  🛡️  ${service} (EXCLUDED)"
        elif is_chaos_target "$service"; then
            log_info "  🎯 ${service} (CHAOS TARGET)"
        else
            log_info "  ℹ️  ${service} (UNKNOWN)"
        fi
    done
    
    log_info ""
    log_info "Configuration:"
    log_info "  Chaos interval: ${CHAOS_INTERVAL}s"
    log_info "  Chaos duration: ${CHAOS_DURATION}s"
    log_info "  Log file: ${LOG_FILE}"
    log_info ""
    log_info "To run chaos monkey, remove --dry-run flag"
}

# =============================================================================
# Main
# =============================================================================

main() {
    # Parse arguments
    local once=false
    local dry_run_mode=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --once)
                once=true
                shift
                ;;
            --dry-run)
                dry_run_mode=true
                shift
                ;;
            *)
                echo "Unknown option: $1"
                echo "Usage: $0 [--once] [--dry-run]"
                exit 1
                ;;
        esac
    done
    
    # Check for docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed or not in PATH"
        exit 1
    fi
    
    # Check for docker-compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed or not in PATH"
        exit 1
    fi
    
    # Initialize chaos events log
    echo "# NeoBank Chaos Events Log" > chaos-events.jsonl
    echo "# Started at $(date -Iseconds)" >> chaos-events.jsonl
    
    log_info "🐵 NeoBank Chaos Monkey initialized"
    log_info "Log file: ${LOG_FILE}"
    
    if [ "$dry_run_mode" = true ]; then
        dry_run
        exit 0
    fi
    
    if [ "$once" = true ]; then
        run_chaos_event
        exit $?
    fi
    
    # Main loop
    log_info "Starting chaos loop (interval: ${CHAOS_INTERVAL}s, duration: ${CHAOS_DURATION}s)"
    log_info "Press Ctrl+C to stop"
    
    while true; do
        run_chaos_event
        log_info "⏱️  Sleeping for ${CHAOS_INTERVAL} seconds until next chaos event..."
        sleep "$CHAOS_INTERVAL"
    done
}

# Handle interrupts
trap 'log_info "Chaos Monkey interrupted. Exiting."; exit 0' INT TERM

# Run main
main "$@"
