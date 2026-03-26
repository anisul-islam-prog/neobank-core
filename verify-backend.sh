#!/bin/bash

# =============================================================================
# NeoBank Backend Test Verification Script
# =============================================================================
# This script runs all backend tests and provides a summary of which modules
# passed and which failed.
#
# Usage: ./verify-backend.sh [--module <module-name>] [--verbose]
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Parse arguments
SPECIFIC_MODULE=""
VERBOSE=false

for arg in "$@"; do
    case $arg in
        --module)
            SPECIFIC_MODULE="$2"
            shift 2
            ;;
        --verbose)
            VERBOSE=true
            shift
            ;;
        --help)
            echo "NeoBank Backend Test Verification Script"
            echo ""
            echo "Usage: ./verify-backend.sh [options]"
            echo ""
            echo "Options:"
            echo "  --module <name>    Run tests for a specific module only"
            echo "  --verbose          Show detailed test output"
            echo "  --help             Show this help message"
            exit 0
            ;;
    esac
done

# Print header
echo -e "${BLUE}"
echo "╔═══════════════════════════════════════════════════════════╗"
echo "║        NeoBank Backend Test Verification Suite            ║"
echo "╚═══════════════════════════════════════════════════════════╝"
echo -e "${NC}"
echo ""
echo "Started at: $(date)"
echo "Working directory: $(pwd)"
echo ""

# Define modules to test
MODULES=(
    "neobank-gateway"
    "neobank-auth"
    "neobank-onboarding"
    "neobank-core-banking"
    "neobank-lending"
    "neobank-cards"
    "neobank-batch"
    "neobank-analytics"
    "neobank-fraud"
)

# Track test results using files for compatibility
MODULE_RESULTS_DIR="/tmp/neobank-test-results-$$"
mkdir -p "$MODULE_RESULTS_DIR"
TESTS_PASSED=0
TESTS_FAILED=0
TESTS_SKIPPED=0

# Function to set module result
set_module_result() {
    local module=$1
    local result=$2
    echo "$result" > "$MODULE_RESULTS_DIR/$module"
}

# Function to get module result
get_module_result() {
    local module=$1
    if [ -f "$MODULE_RESULTS_DIR/$module" ]; then
        cat "$MODULE_RESULTS_DIR/$module"
    else
        echo "UNKNOWN"
    fi
}

# Cleanup on exit
cleanup() {
    rm -rf "$MODULE_RESULTS_DIR"
}
trap cleanup EXIT

# Function to run tests for a module
run_module_tests() {
    local module=$1
    local start_time=$(date +%s)
    
    echo -e "\n${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${CYAN}Testing: ${module}${NC}"
    echo -e "${CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
    
    # Check if module directory exists
    if [ ! -d "$module" ]; then
        echo -e "${YELLOW}⚠ Module directory not found: $module${NC}"
        set_module_result "$module" "SKIPPED"
        TESTS_SKIPPED=$((TESTS_SKIPPED + 1))
        return 0
    fi

    # Check if module has test files
    local test_count=$(find "$module/src/test" -name "*.java" 2>/dev/null | wc -l)
    if [ "$test_count" -eq 0 ]; then
        echo -e "${YELLOW}⊘ No test files found in $module${NC}"
        set_module_result "$module" "NO_TESTS"
        TESTS_SKIPPED=$((TESTS_SKIPPED + 1))
        return 0
    fi

    echo "Found $test_count test file(s)"
    echo ""

    # Run Maven tests for this module
    local mvn_output
    local mvn_exit_code

    if [ "$VERBOSE" = true ]; then
        mvn clean test -pl "$module" -am -DfailIfNoTests=false 2>&1 | tee /tmp/test-output.log
        mvn_exit_code=${PIPESTATUS[0]}
    else
        mvn_output=$(mvn clean test -pl "$module" -am -DfailIfNoTests=false 2>&1)
        mvn_exit_code=$?
    fi

    local end_time=$(date +%s)
    local duration=$((end_time - start_time))

    # Check result
    if [ $mvn_exit_code -eq 0 ]; then
        echo -e "${GREEN}✓ ${module} tests PASSED (${duration}s)${NC}"
        set_module_result "$module" "PASSED"
        TESTS_PASSED=$((TESTS_PASSED + 1))
        return 0
    else
        echo -e "${RED}✗ ${module} tests FAILED (${duration}s)${NC}"
        set_module_result "$module" "FAILED"
        TESTS_FAILED=$((TESTS_FAILED + 1))
        
        # Show error summary
        if [ "$VERBOSE" = false ]; then
            echo -e "${YELLOW}Error summary:${NC}"
            echo "$mvn_output" | grep -A 5 "ERROR\|FAILURE" | head -20
        fi
        return 1
    fi
}

# =============================================================================
# Main Execution
# =============================================================================

# Check if Docker is running (required for Testcontainers)
if ! docker info > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠ WARNING: Docker is not running. Testcontainers tests may fail.${NC}"
    echo "Consider starting Docker before running integration tests."
    echo ""
fi

# Run tests for specific module or all modules
if [ -n "$SPECIFIC_MODULE" ]; then
    run_module_tests "$SPECIFIC_MODULE" || true
else
    for module in "${MODULES[@]}"; do
        run_module_tests "$module" || true
    done
fi

# =============================================================================
# Summary
# =============================================================================
echo -e "\n${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}                    TEST SUMMARY                             ${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${CYAN}Module Results:${NC}"
echo ""

# Print results table
printf "%-25s | %-10s\n" "Module" "Status"
printf "%-25s-+-%-10s\n" "-------------------------" "----------"

for module in "${MODULES[@]}"; do
    status=$(get_module_result "$module")
    case $status in
        "PASSED")
            printf "%-25s | ${GREEN}%-10s${NC}\n" "$module" "$status"
            ;;
        "FAILED")
            printf "%-25s | ${RED}%-10s${NC}\n" "$module" "$status"
            ;;
        "SKIPPED")
            printf "%-25s | ${YELLOW}%-10s${NC}\n" "$module" "$status"
            ;;
        "NO_TESTS")
            printf "%-25s | ${CYAN}%-10s${NC}\n" "$module" "$status"
            ;;
        *)
            printf "%-25s | ${YELLOW}%-10s${NC}\n" "$module" "UNKNOWN"
            ;;
    esac
done

echo ""
echo -e "${CYAN}Summary:${NC}"
echo -e "  ${GREEN}Passed:  ${TESTS_PASSED}${NC}"
echo -e "  ${RED}Failed:  ${TESTS_FAILED}${NC}"
echo -e "  ${YELLOW}Skipped: ${TESTS_SKIPPED}${NC}"
echo ""
echo "Completed at: $(date)"

# Final verdict
if [ ${TESTS_FAILED} -gt 0 ]; then
    echo -e "\n${RED}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║                    TESTS FAILED                             ║${NC}"
    echo -e "${RED}╚═══════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${YELLOW}Failed modules:${NC}"
    for module in "${MODULES[@]}"; do
        if [ "$(get_module_result "$module")" = "FAILED" ]; then
            echo "  - $module"
        fi
    done
    echo ""
    echo "To re-run failed modules:"
    echo "  ./verify-backend.sh --module <module-name>"
    echo ""
    exit 1
else
    echo -e "\n${GREEN}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║                    ALL TESTS PASSED                         ║${NC}"
    echo -e "${GREEN}╚═══════════════════════════════════════════════════════════╝${NC}"
    exit 0
fi
