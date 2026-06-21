#!/bin/bash
# Unit test suite - runs Maven unit tests
echo "Running Maven unit tests..."

cd "$(dirname "$0")/.."

mvn test -pl . -Dtest="com.identityforge.unit.**" --batch-mode 2>&1 | tee unit_tests_output.log
MAVEN_EXIT=${PIPESTATUS[0]}

PASSED=$(grep -c "Tests run:.*Failures: 0, Errors: 0" unit_tests_output.log || echo 0)
FAILED=$(grep -c "Tests run:.*Failures: [1-9]" unit_tests_output.log || echo 0)

echo ""
echo "========================================="
echo "  Unit Test Summary"
echo "========================================="
echo "  Maven exit code: $MAVEN_EXIT"

if [ "$MAVEN_EXIT" -eq 0 ]; then
    echo "  Status: PASSED"
else
    echo "  Status: FAILED"
fi

exit $MAVEN_EXIT
