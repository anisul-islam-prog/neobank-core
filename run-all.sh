#!/bin/bash

# =============================================================================
# NeoBank Backend Application Runner
# =============================================================================
# This script builds and runs all NeoBank backend modules in parallel.
# 
# Prerequisites:
# - Docker must be running (for infrastructure services)
# - All modules must be built successfully first
#
# Usage: 
#   ./run-all.sh                    # Build and run all modules
#   ./run-all.sh --no-build         # Skip build, just run
#   ./run-all.sh --module <name>    # Run specific module only
#   ./run-all.sh --stop             # Stop all running modules
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
# NeoBank Architecture: Each module with @SpringBootApplication runs as independent service
# Gateway (WebFlux) routes to downstream WebMvc services
# All 8 services migrated to Spring Boot 3.5.13
RUNNABLE_MODULES=(
    "neobank-gateway:8080"
    "neobank-onboarding:8081"
    "neobank-lending:8082"
    "neobank-core-banking:8083"
    "neobank-cards:8084"
    "neobank-fraud:8085"
    "neobank-batch:8086"
    "neobank-analytics:8087"
)

# All modules in the project
ALL_MODULES=(
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

PID_DIR=".pids"
LOG_DIR="logs"

# Parse arguments
NO_BUILD=false
SPECIFIC_MODULE=""
STOP=false

for arg in "$@"; do
    case $arg in
        --no-build)
            NO_BUILD=true
            shift
            ;;
        --module)
            SPECIFIC_MODULE="$2"
            shift 2
            ;;
        --stop)
            STOP=true
            shift
            ;;
        --help)
            echo "NeoBank Backend Application Runner"
            echo ""
            echo "Usage: ./run-all.sh [options]"
            echo ""
            echo "Options:"
            echo "  --no-build         Skip Maven build, just run applications"
            echo "  --module <name>    Run specific module only"
            echo "  --stop             Stop all running NeoBank modules"
            echo "  --help             Show this help message"
            exit 0
            ;;
    esac
done

# Function to stop all running modules
stop_all() {
    echo -e "${YELLOW}Stopping all NeoBank modules...${NC}"
    
    if [ -d "$PID_DIR" ]; then
        for pid_file in "$PID_DIR"/*.pid; do
            if [ -f "$pid_file" ]; then
                module=$(basename "$pid_file" .pid)
                pid=$(cat "$pid_file")
                
                if kill -0 "$pid" 2>/dev/null; then
                    echo -e "${CYAN}Stopping $module (PID: $pid)...${NC}"
                    kill "$pid"
                else
                    echo -e "${YELLOW}Module $module is not running (stale PID: $pid)${NC}"
                fi
                
                rm "$pid_file"
            fi
        done
    fi
    
    echo -e "${GREEN}All modules stopped.${NC}"
    exit 0
}

# Print header
echo -e "${BLUE}"
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║              NeoBank Backend Application Runner           ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo -e "${NC}"

# Stop if requested
if [ "$STOP" = true ]; then
    stop_all
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}✗ Docker is not running.${NC}"
    echo "Please start Docker first (required for database and services)."
    exit 1
fi

# Create directories
mkdir -p "$PID_DIR"
mkdir -p "$LOG_DIR"

# Step 1: Build all modules
if [ "$NO_BUILD" = false ]; then
    echo -e "\n${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}Step 1: Building all modules${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
    
    echo "Running: mvn clean install -DskipTests"
    echo -e "${CYAN}(This may take a few minutes on first run...)${NC}\n"
    
    if mvn clean install -DskipTests; then
        echo -e "\n${GREEN}✓ Build successful!${NC}"
    else
        echo -e "\n${RED}✗ Build failed! Please fix the errors and try again.${NC}"
        exit 1
    fi
else
    echo -e "\n${YELLOW}⊘ Skipping build (--no-build flag)${NC}"
fi

# Step 2: Determine which modules to run
if [ -n "$SPECIFIC_MODULE" ]; then
    # Check if module exists and has a main class
    module_exists=false
    for module in "${RUNNABLE_MODULES[@]}"; do
        if [ "$module" = "$SPECIFIC_MODULE" ]; then
            module_exists=true
            break
        fi
    done
    
    if [ "$module_exists" = false ]; then
        echo -e "${RED}✗ Unknown module or module has no main class: $SPECIFIC_MODULE${NC}"
        echo "Runnable modules:"
        for module_port in "${RUNNABLE_MODULES[@]}"; do
            module="${module_port%%:*}"
            echo "  - $module"
        done
        echo ""
        echo "Note: Other modules are being migrated to Spring Boot 3.5.13"
        exit 1
    fi

    RUNNABLE_MODULES=("${SPECIFIC_MODULE}")
fi

# Step 3: Start all modules
echo -e "\n${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Step 2: Starting ${#RUNNABLE_MODULES[@]} module(s)${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

echo -e "${CYAN}NeoBank Spring Boot 3.5.13 - All 8 Services${NC}"
echo -e "${CYAN}Starting distributed backend services...${NC}\n"

# Function to start a single module
start_module() {
    local module_port=$1
    local module="${module_port%%:*}"  # Strip port
    local log_file="$LOG_DIR/$module.log"
    local pid_file="$PID_DIR/$module.pid"

    echo -e "${CYAN}Starting $module...${NC}"

    # Start the module in background
    mvn spring-boot:run -pl "$module" > "$log_file" 2>&1 &
    local pid=$!

    # Save PID
    echo "$pid" > "$pid_file"

    echo -e "${GREEN}  ✓ $module started (PID: $pid, log: $log_file)${NC}"
}

# Start modules in parallel
for module_port in "${RUNNABLE_MODULES[@]}"; do
    start_module "$module_port"
    # Small delay to prevent port binding race conditions
    sleep 2
done

# Wait a moment for processes to initialize
sleep 3

# Check if processes are still running
echo -e "\n${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}Startup Status${NC}"
echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

all_running=true
for module_port in "${RUNNABLE_MODULES[@]}"; do
    module="${module_port%%:*}"  # Strip port
    pid_file="$PID_DIR/$module.pid"
    
    if [ -f "$pid_file" ]; then
        pid=$(cat "$pid_file")
        
        if kill -0 "$pid" 2>/dev/null; then
            echo -e "${GREEN}  ✓ $module is running (PID: $pid)${NC}"
        else
            echo -e "${RED}  ✗ $module failed to start (check $LOG_DIR/$module.log)${NC}"
            all_running=false
        fi
    fi
done

echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}Application Access Points${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}"
echo "  API Gateway:    http://localhost:8080"
echo "  Onboarding:     http://localhost:8081"
echo "  Lending:        http://localhost:8082"
echo "  Core Banking:   http://localhost:8083"
echo "  Cards:          http://localhost:8084"
echo "  Fraud:          http://localhost:8085"
echo "  Batch:          http://localhost:8086"
echo "  Analytics:      http://localhost:8087"
echo "  Swagger UI:     http://localhost:8080/swagger-ui.html"
echo "  Actuator:       http://localhost:8080/actuator/health"
echo -e "${NC}"

echo -e "\n${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${YELLOW}Managing Applications${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}"
echo "  View logs:      tail -f logs/<module>.log"
echo "  Stop all:       ./run-all.sh --stop"
echo "  Stop specific:  kill \$(cat .pids/<module>.pid)"
echo "  Restart:        ./run-all.sh --no-build"
echo -e "${NC}"

echo -e "\n${GREEN}All modules started! Press Ctrl+C to view logs (apps continue running).${NC}"
echo -e "${YELLOW}Use './run-all.sh --stop' in another terminal to stop all apps.${NC}\n"

# Keep script running and show combined logs
trap 'echo -e "\n${YELLOW}Caught interrupt. Applications are still running in background.${NC}\n${YELLOW}Use ./run-all.sh --stop to stop them.${NC}"; exit 0' INT

# Wait forever (user will Ctrl+C)
wait
