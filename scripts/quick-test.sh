#!/bin/bash

# JSweet Quick Test Script
# Runs a basic compilation test to verify the setup is working

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🧪 JSweet Quick Test"
echo "==================="
echo

# Function to print status
print_status() {
    local status="$1"
    local message="$2"
    
    case "$status" in
        "info")    echo "ℹ️  $message" ;;
        "success") echo "✅ $message" ;;
        "error")   echo "❌ $message" ;;
        "test")    echo "🔍 $message" ;;
    esac
}

cd "$PROJECT_ROOT" || exit 1

# Check if JAR exists
JAR_FILE="target/jsweet-cli-0.1.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    print_status "error" "JAR file not found: $JAR_FILE"
    echo
    print_status "info" "Run 'mvn clean package' to build the project first"
    exit 1
fi

# Check if test input exists
TEST_INPUT="src/test/resources/HelloWorld.java"
if [ ! -f "$TEST_INPUT" ]; then
    print_status "error" "Test input file not found: $TEST_INPUT"
    exit 1
fi

print_status "test" "Running basic compilation test..."
echo

# Clean any previous test output
rm -rf quick-test-output 2>/dev/null

# Run the compilation
print_status "info" "Compiling: $TEST_INPUT → quick-test-output/"
if java -jar "$JAR_FILE" "$TEST_INPUT" -o quick-test-output; then
    print_status "success" "Compilation completed successfully!"
    
    # Check if output file was created
    EXPECTED_OUTPUT="quick-test-output/HelloWorld.ts"
    if [ -f "$EXPECTED_OUTPUT" ]; then
        print_status "success" "Output file created: $EXPECTED_OUTPUT"
        
        # Show a snippet of the generated TypeScript
        echo
        print_status "info" "Generated TypeScript (first 10 lines):"
        echo "---"
        head -10 "$EXPECTED_OUTPUT"
        echo "---"
        
        # Check if it's valid TypeScript (basic check)
        if grep -q "class HelloWorld" "$EXPECTED_OUTPUT" && grep -q "constructor(" "$EXPECTED_OUTPUT"; then
            print_status "success" "Generated TypeScript looks valid!"
        else
            print_status "error" "Generated TypeScript might be malformed"
            exit 1
        fi
        
    else
        print_status "error" "Expected output file not found: $EXPECTED_OUTPUT"
        exit 1
    fi
    
else
    print_status "error" "Compilation failed!"
    exit 1
fi

# Clean up test output
rm -rf quick-test-output

echo
print_status "success" "Quick test completed successfully!"
echo
print_status "info" "Your JSweet CLI is working correctly."
print_status "info" "Run './scripts/validate-setup.sh' for comprehensive testing."
echo