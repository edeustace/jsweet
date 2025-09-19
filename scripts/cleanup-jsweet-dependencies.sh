#!/bin/bash

# JSweet Dependencies Cleanup Script  
# Removes JSweet JAR files from local Maven repository for fresh setup

echo "🧹 JSweet Dependencies Cleanup"
echo "================================"
echo

# Check if ~/.m2/repository exists
MAVEN_REPO="$HOME/.m2/repository"
if [ ! -d "$MAVEN_REPO" ]; then
    echo "ℹ️  No Maven repository found at $MAVEN_REPO"
    echo "   Nothing to clean up."
    exit 0
fi

JSWEET_DIR="$MAVEN_REPO/org/jsweet"

if [ ! -d "$JSWEET_DIR" ]; then
    echo "ℹ️  No JSweet dependencies found in Maven repository"
    echo "   Nothing to clean up."
    exit 0
fi

echo "🗑️  Found JSweet dependencies at: $JSWEET_DIR"
echo

# Show what will be removed
echo "The following will be removed:"
ls -la "$JSWEET_DIR" 2>/dev/null || echo "   (directory listing failed)"
echo

# Prompt for confirmation
read -p "❓ Are you sure you want to remove all JSweet dependencies? (y/N): " -r
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Cleanup cancelled."
    exit 0
fi

echo "🧹 Removing JSweet dependencies..."

# Remove the entire org/jsweet directory
if rm -rf "$JSWEET_DIR"; then
    echo "✅ JSweet dependencies removed successfully!"
    echo
    echo "🚀 Next steps:"
    echo "   1. Run './scripts/setup-jsweet-dependencies.sh' to reinstall"
    echo "   2. Run 'mvn clean' to clean the project build"
else
    echo "❌ ERROR: Failed to remove JSweet dependencies"
    echo "   You may need to check permissions on: $JSWEET_DIR"
    exit 1
fi

echo