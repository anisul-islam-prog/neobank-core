#!/bin/bash

# =============================================================================
# NeoBank Backend Orchestrated Startup Script
# =============================================================================
# Launches all 9 NeoBank services in dependency order.
# Waits for critical services (Auth, Core Banking) to be healthy before
# starting dependent services.
#
# Usage:
#   ./run-all.sh              # Build and run all services
#   ./run-all.sh --no-build   # Skip build, just run
#   ./run-all.sh --stop       # Stop all running services
# =============================================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Configuration
PID_DIR=".pids"
LOG_DIR="logs"
MAX_WAIT_SECONDS=120
HEALTH_CHECK_INTERVAL=3

# Service definitions (order matters)
CRITICAL_SERVICES=("neobank-auth:8081" "neobank-core-banking:8083")
DEPENDENT_SERVICES=("neobank-onboarding:8082" "neobank-lending:8084" "neobank-cards:8085" "neobank-fraud:8086" "neobank-batch:8087" "neobank-analytics:8088")
GATEWAY="neobank-gateway:8080"

ALL_SERVICES=("${CRITICAL_SERVICES[@]}" "${DEPENDENT_SERVICES[@]}" "$GATEWAY")

# Parse arguments
NO_BUILD=false
STOP=false

for arg in "$@"; do
    case $arg in
        --no-build) NO_BUILD=true; shift ;;
        --stop) STOP=true; shift ;;
        --help)
            echo "NeoBank Orchestrated Startup"
            echo ""
            echo "Usage: ./run-all.sh [options]"
            echo ""
            echo "Options:"
            echo "  --no-build   Skip Maven build"
            echo "  --stop       Stop all running services"
            echo "  --help       Show this help"
            exit 0
            ;;
    esac
done

# Stop function
stop_all() {
    echo -e "${YELLOW}Stopping all NeoBank services...${NC}"
    if [ -d "$PID_DIR" ]; then
        for pid_file in "$PID_DIR"/*.pid; do
            [ -f "$pid_file" ] || continue
            module=$(basename "$pid_file" .pid)
            pid=$(cat "$pid_file")
            if kill -0 "$pid" 2>/dev/null; then
                echo -e "${CYAN}  Stopping $module (PID: $pid)${NC}"
                kill "$pid" 2>/dev/null || true
            fi
            rm -f "$pid_file"
        done
    fi
    pkill -f "spring-boot:run" 2>/dev/null || true
    echo -e "${GREEN}All services stopped.${NC}"
    exit 0
}

[ "$STOP" = true ] && stop_all

# Header
echo -e "${BLUE}"
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║          NeoBank Orchestrated Service Startup             ║"
echo "║              Spring Boot 3.5.13 | Java 21                 ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Create directories
mkdir -p "$PID_DIR" "$LOG_DIR"

# Check Docker
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}✗ Docker is not running.${NC}"
    echo "  Required for PostgreSQL. Start Docker first."
    exit 1
fi

# Build phase
if [ "$NO_BUILD" = false ]; then
    echo -e "\n${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}Step 1: Building all modules${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
    
    if mvn clean install -DskipTests -Dmaven.test.skip=true; then
        echo -e "\n${GREEN}✓ Build successful!${NC}"
    else
        echo -e "\n${RED}✗ Build failed!${NC}"
        exit 1
    fi
else
    echo -e "\n${YELLOW}⊘ Skipping build (--no-build)${NC}"
fi

# Function to check service health
wait_for_health() {
    local service=$1
    local port=$2
    local elapsed=0
    
    echo -ne "${CYAN}  Waiting for $service (port $port) to be healthy${NC}"
    
    while [ $elapsed -lt $MAX_WAIT_SECONDS ]; do
        if curl -s "http://localhost:$port/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then
            echo -e " ${GREEN}✓ HEALTHY${NC}"
            return 0
        fi
        
        # Also check if process is still running
        local pid_file="$PID_DIR/$service.pid"
        if [ -f "$pid_file" ]; then
            local pid=$(cat "$pid_file")
            if ! kill -0 "$pid" 2>/dev/null; then
                echo -e " ${RED}✗ FAILED (process exited)${NC}"
                echo -e "${RED}  Check logs/$service.log for details${NC}"
                return 1
            fi
        fi
        
        echo -ne "."
        sleep $HEALTH_CHECK_INTERVAL
        elapsed=$((elapsed + HEALTH_CHECK_INTERVAL))
    done
    
    echo -e " ${YELLOW}⚠ TIMEOUT ($MAX_WAIT_SECONDS s)${NC}"
    return 1
}

# Function to start a service
start_service() {
    local service_port=$1
    local service="${service_port%%:*}"
    local port="${service_port##*:}"
    local log_file="$LOG_DIR/$service.log"
    local pid_file="$PID_DIR/$service.pid"
    
    echo -e "${CYAN}  Starting $service on port $port...${NC}"
    mvn spring-boot:run -pl "$service" > "$log_file" 2>&1 &
    local pid=$!
    echo "$pid" > "$pid_file"
    
    # Wait for port to be bound
    sleep 3
    if lsof -i :$port > /dev/null 2>&1; then
        echo -e "${GREEN}    ✓ $service started (PID: $pid)${NC}"
        return 0
    else
        echo -e "${RED}    ✗ $service failed to bind to port $port${NC}"
        return 1
    fi
}

# =============================================================================
# Phase 1: Start Critical Services (Auth + Core Banking)
# =============================================================================
echo -e "\n${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Phase 1: Starting Critical Services${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

critical_healthy=true
for service_port in "${CRITICAL_SERVICES[@]}"; do
    start_service "$service_port" || critical_healthy=false
done

if [ "$critical_healthy" = true ]; then
    echo -e "\n${CYAN}Verifying critical service health...${NC}"
    for service_port in "${CRITICAL_SERVICES[@]}"; do
        service="${service_port%%:*}"
        port="${service_port##*:}"
        wait_for_health "$service" "$port" || critical_healthy=false
    done
fi

if [ "$critical_healthy" = false ]; then
    echo -e "\n${RED}✗ Critical services failed to start. Aborting.${NC}"
    exit 1
fi

echo -e "\n${GREEN}✓ Critical services are healthy!${NC}"

# =============================================================================
# Phase 2: Start Dependent Services
# =============================================================================
echo -e "\n${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Phase 2: Starting Dependent Services${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

for service_port in "${DEPENDENT_SERVICES[@]}"; do
    start_service "$service_port"
done

echo -e "\n${CYAN}Waiting for dependent services to initialize...${NC}"
sleep 10

# =============================================================================
# Phase 3: Start Gateway (after all services are up)
# =============================================================================
echo -e "\n${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Phase 3: Starting API Gateway${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

start_service "$GATEWAY"
sleep 5

# =============================================================================
# Final Status
# =============================================================================
echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Service Status${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

printf "%-25s | %-8s | %-10s | %s\n" "Service" "Port" "Status" "PID"
printf "%-25s-+-%-8s-+-%-10s-+-%s\n" "-------------------------" "--------" "----------" "--------"

for service_port in "${ALL_SERVICES[@]}"; do
    service="${service_port%%:*}"
    port="${service_port##*:}"
    pid_file="$PID_DIR/$service.pid"
    
    if [ -f "$pid_file" ]; then
        pid=$(cat "$pid_file")
        if kill -0 "$pid" 2>/dev/null; then
            # Check health
            if curl -s "http://localhost:$port/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then
                status="${GREEN}UP${NC}"
            else
                status="${YELLOW}STARTING${NC}"
            fi
            printf "%-25s | %-8s | ${GREEN}%-10s${NC} | %s\n" "$service" "$port" "RUNNING" "$pid"
        else
            printf "%-25s | %-8s | ${RED}%-10s${NC} | %s\n" "$service" "$port" "FAILED" "$pid"
        fi
    else
        printf "%-25s | %-8s | ${RED}%-10s${NC} | %s\n" "$service" "$port" "NOT STARTED" "-"
    fi
done

echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Access Points${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}"
echo "  API Gateway:    http://localhost:8080"
echo "  Auth:           http://localhost:8081"
echo "  Onboarding:     http://localhost:8082"
echo "  Core Banking:   http://localhost:8083"
echo "  Lending:        http://localhost:8084"
echo "  Cards:          http://localhost:8085"
echo "  Fraud:          http://localhost:8086"
echo "  Batch:          http://localhost:8087"
echo "  Analytics:      http://localhost:8088"
echo ""
echo "  Swagger UI:     http://localhost:8080/swagger-ui.html"
echo "  Actuator:       http://localhost:8080/actuator/health"
echo "  Grafana:        http://localhost:3003 (admin/admin123)"
echo "  Prometheus:     http://localhost:9090"
echo -e "${NC}"

echo -e "\n${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Management${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}"
echo "  View logs:      tail -f logs/<service>.log"
echo "  Stop all:       ./run-all.sh --stop"
echo "  Check health:   curl http://localhost:8080/actuator/health"
echo -e "${NC}"

echo -e "\n${GREEN}✅ NeoBank backend is running!${NC}\n"

# Keep script alive
trap 'echo -e "\n${YELLOW}Interrupt received. Services continue running in background.${NC}\n${YELLOW}Use ./run-all.sh --stop to stop them.${NC}"; exit 0' INT

wait
