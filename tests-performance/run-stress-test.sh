#!/bin/bash
# =============================================================================
# NeoBank War Room Stress Test
# =============================================================================
# Automated stress testing with chaos engineering.
# 
# This script:
# 1. Starts the full stack with observability
# 2. Starts the Chaos Monkey
# 3. Runs the k6 load test
# 4. Outputs a summary of resilience during service outages
#
# Usage:
#   ./run-stress-test.sh [--duration 5m] [--chaos-interval 120]
#
# Prerequisites:
#   - Docker and Docker Compose
#   - k6 installed (https://k6.io/docs/getting-started/installation/)
#   - jq for JSON parsing
# =============================================================================

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
DURATION=${DURATION:-5m}
CHAOS_INTERVAL=${CHAOS_INTERVAL:-120}
CHAOS_DURATION=${CHAOS_DURATION:-30}
RESULTS_DIR="${SCRIPT_DIR}/results"
LOG_FILE="${RESULTS_DIR}/war-room-$(date +%Y%m%d-%H%M%S).log"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

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

log_info() { log "${BLUE}INFO${NC}" "$@"; }
log_warn() { log "${YELLOW}WARN${NC}" "$@"; }
log_error() { log "${RED}ERROR${NC}" "$@"; }
log_success() { log "${GREEN}SUCCESS${NC}" "$@"; }
log_step() { log "${MAGENTA}STEP${NC}" "$@"; }

check_prerequisites() {
    log_info "Checking prerequisites..."
    
    local missing=()
    
    if ! command -v docker &> /dev/null; then
        missing+=("docker")
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        missing+=("docker-compose")
    fi
    
    if ! command -v k6 &> /dev/null; then
        missing+=("k6")
    fi
    
    if ! command -v jq &> /dev/null; then
        missing+=("jq")
    fi
    
    if [ ${#missing[@]} -gt 0 ]; then
        log_error "Missing prerequisites: ${missing[*]}"
        echo ""
        echo "Install missing tools:"
        echo "  - Docker: https://docs.docker.com/get-docker/"
        echo "  - k6: https://k6.io/docs/getting-started/installation/"
        echo "  - jq: https://jqlang.github.io/jq/download/"
        exit 1
    fi
    
    log_success "All prerequisites met"
}

setup_results_dir() {
    log_info "Setting up results directory..."
    mkdir -p "$RESULTS_DIR"
    mkdir -p "$PROJECT_ROOT/tests-performance/results"
    log_success "Results directory: $RESULTS_DIR"
}

# =============================================================================
# Stack Management
# =============================================================================

start_stack() {
    log_step "Starting NeoBank stack with observability..."
    
    cd "$PROJECT_ROOT"
    
    # Start with observability profile
    docker-compose --profile observability up -d
    
    log_info "Waiting for services to be healthy..."
    sleep 30
    
    # Check health
    local health_url="http://localhost:8080/actuator/health"
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$health_url" | jq -e '.status == "UP"' > /dev/null 2>&1; then
            log_success "Backend is healthy!"
            return 0
        fi
        
        log_info "Waiting for backend... (attempt $attempt/$max_attempts)"
        sleep 5
        attempt=$((attempt + 1))
    done
    
    log_error "Backend failed to become healthy"
    return 1
}

stop_stack() {
    log_step "Stopping NeoBank stack..."
    
    cd "$PROJECT_ROOT"
    docker-compose --profile observability down
    
    log_success "Stack stopped"
}

# =============================================================================
# Chaos Monkey
# =============================================================================

start_chaos_monkey() {
    log_step "Starting Chaos Monkey..."
    
    cd "$SCRIPT_DIR"
    
    # Start chaos monkey in background
    ./chaos-monkey.sh --chaos-interval "$CHAOS_INTERVAL" --chaos-duration "$CHAOS_DURATION" &
    CHAOS_PID=$!
    
    log_success "Chaos Monkey started (PID: $CHAOS_PID)"
    log_info "Chaos interval: ${CHAOS_INTERVAL}s, Duration: ${CHAOS_DURATION}s"
}

stop_chaos_monkey() {
    if [ -n "$CHAOS_PID" ] && kill -0 "$CHAOS_PID" 2>/dev/null; then
        log_info "Stopping Chaos Monkey..."
        kill "$CHAOS_PID" 2>/dev/null || true
        wait "$CHAOS_PID" 2>/dev/null || true
        log_success "Chaos Monkey stopped"
    fi
}

# =============================================================================
# Load Testing
# =============================================================================

run_load_test() {
    log_step "Running k6 load test..."
    
    cd "$SCRIPT_DIR"
    
    export BASE_URL="http://localhost:8080"
    
    # Run load test
    k6 run --out json="${RESULTS_DIR}/load-test-results.json" load-test.js 2>&1 | tee "${RESULTS_DIR}/load-test-output.log"
    
    local exit_code=${PIPESTATUS[0]}
    
    if [ $exit_code -eq 0 ]; then
        log_success "Load test completed successfully"
    else
        log_warn "Load test completed with threshold failures (exit code: $exit_code)"
    fi
    
    return $exit_code
}

run_rate_limit_test() {
    log_step "Running rate limit validation test..."
    
    cd "$SCRIPT_DIR"
    
    export BASE_URL="http://localhost:8080"
    
    k6 run rate-limit-test.js 2>&1 | tee "${RESULTS_DIR}/rate-limit-output.log"
    
    local exit_code=${PIPESTATUS[0]}
    
    if [ $exit_code -eq 0 ]; then
        log_success "Rate limit test completed successfully"
    else
        log_warn "Rate limit test completed with threshold failures (exit code: $exit_code)"
    fi
    
    return $exit_code
}

# =============================================================================
# Reporting
# =============================================================================

generate_report() {
    log_step "Generating War Room Report..."
    
    local report_file="${RESULTS_DIR}/war-room-report.md"
    local chaos_events_file="${SCRIPT_DIR}/chaos-events.jsonl"
    local load_results_file="${RESULTS_DIR}/load-test-results.json"
    
    cat > "$report_file" << 'EOF'
# NeoBank War Room Stress Test Report

## Executive Summary

EOF
    
    # Add timestamp
    echo "**Generated:** $(date '+%Y-%m-%d %H:%M:%S')" >> "$report_file"
    echo "**Duration:** ${DURATION}" >> "$report_file"
    echo "**Chaos Interval:** ${CHAOS_INTERVAL}s" >> "$report_file"
    echo "**Chaos Duration:** ${CHAOS_DURATION}s" >> "$report_file"
    echo "" >> "$report_file"
    
    # Chaos Events Summary
    echo "## Chaos Events" >> "$report_file"
    echo "" >> "$report_file"
    
    if [ -f "$chaos_events_file" ]; then
        local chaos_count=$(grep -c '"event":"chaos_start"' "$chaos_events_file" 2>/dev/null || echo "0")
        local recovery_count=$(grep -c '"event":"chaos_end"' "$chaos_events_file" 2>/dev/null || echo "0")
        
        echo "| Metric | Value |" >> "$report_file"
        echo "|--------|-------|" >> "$report_file"
        echo "| Chaos Events Triggered | ${chaos_count} |" >> "$report_file"
        echo "| Recoveries | ${recovery_count} |" >> "$report_file"
        echo "" >> "$report_file"
        
        echo "### Event Timeline" >> "$report_file"
        echo "" >> "$report_file"
        echo '```' >> "$report_file"
        grep -v "^#" "$chaos_events_file" | jq -r 'select(.event != null) | "\(.timestamp) - \(.event | ascii_upcase): \(.service)"' 2>/dev/null >> "$report_file" || echo "No events recorded" >> "$report_file"
        echo '```' >> "$report_file"
        echo "" >> "$report_file"
    else
        echo "No chaos events recorded." >> "$report_file"
        echo "" >> "$report_file"
    fi
    
    # Load Test Results
    echo "## Load Test Results" >> "$report_file"
    echo "" >> "$report_file"
    
    if [ -f "${RESULTS_DIR}/load-test-output.log" ]; then
        echo '```' >> "$report_file"
        tail -50 "${RESULTS_DIR}/load-test-output.log" >> "$report_file"
        echo '```' >> "$report_file"
    fi
    echo "" >> "$report_file"
    
    # Resilience Assessment
    echo "## Resilience Assessment" >> "$report_file"
    echo "" >> "$report_file"
    
    # Check if money flows succeeded despite chaos
    local money_flow_success=true
    
    if [ -f "${RESULTS_DIR}/load-test-output.log" ]; then
        if grep -q "Money Flow Test: ❌ FAILED" "${RESULTS_DIR}/load-test-output.log"; then
            money_flow_success=false
        fi
    fi
    
    if [ "$money_flow_success" = true ]; then
        echo "✅ **Money flows remained successful despite service outages**" >> "$report_file"
    else
        echo "❌ **Some money flows failed during chaos events**" >> "$report_file"
    fi
    
    echo "" >> "$report_file"
    echo "### Recommendations" >> "$report_file"
    echo "" >> "$report_file"
    
    if [ "$money_flow_success" = true ]; then
        echo "1. ✅ Circuit breakers are working correctly" >> "$report_file"
        echo "2. ✅ Fallback mechanisms are effective" >> "$report_file"
        echo "3. ✅ System is resilient to single-service failures" >> "$report_file"
    else
        echo "1. ⚠️ Review circuit breaker thresholds" >> "$report_file"
        echo "2. ⚠️ Check fallback mechanism implementation" >> "$report_file"
        echo "3. ⚠️ Consider increasing retry attempts" >> "$report_file"
    fi
    
    echo "" >> "$report_file"
    echo "---" >> "$report_file"
    echo "*Report generated by NeoBank War Room Stress Test*" >> "$report_file"
    
    log_success "Report generated: $report_file"
    
    # Also output to console
    echo ""
    echo "============================================"
    echo "         WAR ROOM REPORT SUMMARY"
    echo "============================================"
    cat "$report_file"
    echo "============================================"
}

# =============================================================================
# Cleanup
# =============================================================================

cleanup() {
    log_info "Cleaning up..."
    
    stop_chaos_monkey
    
    # Optionally stop the stack (comment out to keep running)
    # stop_stack
    
    log_success "Cleanup complete"
}

# =============================================================================
# Main
# =============================================================================

main() {
    # Parse arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --duration)
                DURATION="$2"
                shift 2
                ;;
            --chaos-interval)
                CHAOS_INTERVAL="$2"
                shift 2
                ;;
            --chaos-duration)
                CHAOS_DURATION="$2"
                shift 2
                ;;
            --no-cleanup)
                NO_CLEANUP=true
                shift
                ;;
            *)
                echo "Unknown option: $1"
                echo "Usage: $0 [--duration 5m] [--chaos-interval 120] [--chaos-duration 30] [--no-cleanup]"
                exit 1
                ;;
        esac
    done
    
    # Setup trap for cleanup
    if [ -z "$NO_CLEANUP" ]; then
        trap cleanup EXIT INT TERM
    fi
    
    echo ""
    echo "============================================"
    echo "    NeoBank War Room Stress Test"
    echo "============================================"
    echo ""
    echo "Configuration:"
    echo "  Duration: ${DURATION}"
    echo "  Chaos Interval: ${CHAOS_INTERVAL}s"
    echo "  Chaos Duration: ${CHAOS_DURATION}s"
    echo "  Results: ${RESULTS_DIR}"
    echo ""
    
    # Run the test suite
    check_prerequisites
    setup_results_dir
    
    log_info "Starting War Room Stress Test..."
    
    start_stack
    start_chaos_monkey
    
    # Give chaos monkey time to start
    sleep 5
    
    # Run tests
    run_load_test
    local load_exit=$?
    
    # Run rate limit test
    run_rate_limit_test
    
    # Generate report
    generate_report
    
    log_success "War Room Stress Test complete!"
    log_info "Results saved to: ${RESULTS_DIR}"
    
    return $load_exit
}

# Run main
main "$@"
