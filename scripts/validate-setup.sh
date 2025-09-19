#!/bin/bash

# JSweet Setup Validation Script
# Verifies JSweet dependencies are correctly installed and Maven build works

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🔍 JSweet Setup Validation"
echo "================================"
echo

# Track validation status
VALIDATION_PASSED=true

# Function to check and report status
check_status() {
    local description="$1"
    local command="$2"
    
    echo -n "Checking $description... "
    
    if eval "$command" &>/dev/null; then
        echo "✅ PASS"
    else
        echo "❌ FAIL"
        VALIDATION_PASSED=false
    fi
}

# Function to check JAR files specifically
check_jar_files() {
    echo -n "Checking JSweet JAR files... "
    
    if [ -d "$PROJECT_ROOT/jsweet-jars" ]; then
        JAR_COUNT=$(find "$PROJECT_ROOT/jsweet-jars" -name '*.jar' -type f | wc -l)
        if [ "$JAR_COUNT" -ge 2 ]; then
            echo "✅ PASS"
        else
            echo "❌ FAIL"
            VALIDATION_PASSED=false
        fi
    else
        echo "❌ FAIL"
        VALIDATION_PASSED=false
    fi
}

# Check prerequisites
echo "📋 Prerequisites Check"
echo "----------------------"

check_status "Java installation" "java -version"
check_status "Maven installation" "mvn -version"
check_status "Project structure" "[ -f '$PROJECT_ROOT/pom.xml' ]"
check_jar_files

echo
echo "📦 Maven Repository Check"  
echo "-------------------------"

MAVEN_REPO="$HOME/.m2/repository/org/jsweet"

check_status "JSweet transpiler in Maven repo" "[ -d '$MAVEN_REPO/jsweet-transpiler' ]"
check_status "JSweet core in Maven repo" "[ -d '$MAVEN_REPO/jsweet-core' ]"

echo
echo "🏗️  Build System Check"
echo "----------------------"

cd "$PROJECT_ROOT" || exit 1

# Check if Maven can resolve dependencies
echo -n "Checking Maven dependency resolution... "
if mvn dependency:resolve-sources -q; then
    echo "✅ PASS"
else
    echo "❌ FAIL"
    VALIDATION_PASSED=false
fi

# Check if project compiles
echo -n "Checking project compilation... "
if mvn compile -q; then
    echo "✅ PASS"
else
    echo "❌ FAIL"
    VALIDATION_PASSED=false
fi

# Check if JAR builds successfully
echo -n "Checking JAR build... "
if mvn package -q; then
    echo "✅ PASS"
    
    # Check if JAR file was created
    JAR_FILE="target/jsweet-cli-0.1.0.jar"
    echo -n "Checking JAR file creation... "
    if [ -f "$JAR_FILE" ]; then
        echo "✅ PASS"
    else
        echo "❌ FAIL"
        VALIDATION_PASSED=false
    fi
else
    echo "❌ FAIL"
    VALIDATION_PASSED=false
fi

echo
echo "🧪 Functional Testing"
echo "---------------------"

# Check if CLI shows help
if [ -f "target/jsweet-cli-0.1.0.jar" ]; then
    echo -n "Checking CLI help functionality... "
    if java -jar target/jsweet-cli-0.1.0.jar --help | grep -q "Usage:"; then
        echo "✅ PASS"
    else
        echo "❌ FAIL"
        VALIDATION_PASSED=false
    fi
    
    # Check if CLI shows version
    echo -n "Checking CLI version functionality... "
    if java -jar target/jsweet-cli-0.1.0.jar --version | grep -q "0.1.0"; then
        echo "✅ PASS"
    else
        echo "❌ FAIL"
        VALIDATION_PASSED=false
    fi
    
    # Check if test compilation works
    echo -n "Checking basic compilation... "
    if [ -f "src/test/resources/HelloWorld.java" ]; then
        # Clean previous output
        rm -rf test-output 2>/dev/null
        
        if java -jar target/jsweet-cli-0.1.0.jar src/test/resources/HelloWorld.java -o test-output 2>/dev/null && \
           [ -f "test-output/HelloWorld.ts" ]; then
            echo "✅ PASS"
            rm -rf test-output  # Clean up
        else
            echo "❌ FAIL"
            VALIDATION_PASSED=false
        fi
    else
        echo "⚠️  SKIP (no test file found)"
    fi
fi

echo
echo "📊 Validation Results"
echo "===================="

if [ "$VALIDATION_PASSED" = true ]; then
    echo "🎉 ALL CHECKS PASSED!"
    echo
    echo "✅ Your JSweet setup is working correctly!"
    echo "🚀 You can now use:"
    echo "   • mvn clean package  (build the project)"
    echo "   • java -jar target/jsweet-cli-0.1.0.jar --help  (see CLI options)"
    echo "   • java -jar target/jsweet-cli-0.1.0.jar HelloWorld.java -o output/  (compile Java to TypeScript)"
    echo
    exit 0
else
    echo "❌ SOME CHECKS FAILED!"
    echo
    echo "🔧 Troubleshooting:"
    echo "   1. Run './scripts/setup-jsweet-dependencies.sh' to reinstall JSweet dependencies"
    echo "   2. Check that Java 11+ and Maven 3.6+ are installed"
    echo "   3. Check TROUBLESHOOTING.md for common issues"
    echo "   4. Run 'mvn clean' and try validation again"
    echo
    exit 1
fi