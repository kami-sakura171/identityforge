#!/bin/bash
# IdentityForge - Full test execution script
# Runs unit tests (Maven) and API tests (curl) sequentially,
# producing a final pass/fail summary.

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

UNIT_EXIT=0
API_EXIT=0

echo "========================================="
echo "  IdentityForge Test Suite"
echo "  $(date)"
echo "========================================="

# Phase 1: Unit Tests
echo ""
echo ">>> Phase 1: Running Unit Tests <<<"
echo "-----------------------------------------"
if [ -f "unit_tests/test_suite.sh" ]; then
    bash unit_tests/test_suite.sh
    UNIT_EXIT=$?
else
    echo "WARNING: unit_tests/test_suite.sh not found"
fi

# Phase 2: API Tests
echo ""
echo ">>> Phase 2: Running API Tests <<<"
echo "-----------------------------------------"
if [ -f "API_tests/test_suite.sh" ]; then
    bash API_tests/test_suite.sh
    API_EXIT=$?
else
    echo "WARNING: API_tests/test_suite.sh not found"
fi

# Summary
echo ""
echo "========================================="
echo "  TEST SUMMARY"
echo "========================================="
echo "  Unit Tests exit code: $UNIT_EXIT"
echo "  API Tests exit code:  $API_EXIT"
echo "========================================="

if [ "$UNIT_EXIT" -eq 0 ] && [ "$API_EXIT" -eq 0 ]; then
    echo "  ALL TESTS PASSED ✓"
    exit 0
else
    echo "  SOME TESTS FAILED ✗"
    exit 1
fi
