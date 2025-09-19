#!/bin/bash
set -e  # Exit on any error

# JSweet Dependencies Setup Script
# Installs JSweet JAR files from repository to local Maven repository

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
JARS_DIR="$PROJECT_ROOT/jsweet-jars"

echo "🔧 JSweet Dependencies Setup"
echo "================================"
echo

# Check if jsweet-jars directory exists
if [ ! -d "$JARS_DIR" ]; then
    echo "❌ ERROR: jsweet-jars directory not found at: $JARS_DIR"
    echo "   Make sure you're running this from the project root or scripts/ directory"
    exit 1
fi

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ ERROR: Maven (mvn) not found in PATH"
    echo "   Please install Maven 3.6+ or run 'mise install maven' if using mise"
    exit 1
fi

echo "📦 Installing JSweet JAR files to local Maven repository..."
echo

# Core JSweet dependencies (required)
echo "Installing core JSweet transpiler..."
mvn install:install-file \
    -Dfile="$JARS_DIR/jsweet-transpiler-3.1.0-maven.jar" \
    -DgroupId=org.jsweet \
    -DartifactId=jsweet-transpiler \
    -Dversion=3.2.0-SNAPSHOT \
    -Dclassifier=jar-with-dependencies \
    -Dpackaging=jar \
    -DgeneratePom=true

echo "Installing core JSweet library..."
mvn install:install-file \
    -Dfile="$JARS_DIR/jsweet-core-6.3.0-maven.jar" \
    -DgroupId=org.jsweet \
    -DartifactId=jsweet-core \
    -Dversion=6.3.1 \
    -Dpackaging=jar \
    -DgeneratePom=true

# Optional support libraries (install but don't fail if missing)
echo "Installing optional support libraries..."

if [ -f "$JARS_DIR/bigjs-3.1.0-20170726.jar" ]; then
    mvn install:install-file \
        -Dfile="$JARS_DIR/bigjs-3.1.0-20170726.jar" \
        -DgroupId=org.jsweet \
        -DartifactId=bigjs \
        -Dversion=3.1.0-20170726 \
        -Dpackaging=jar \
        -DgeneratePom=true || echo "⚠️  Warning: bigjs installation failed"
fi

if [ -f "$JARS_DIR/j4ts-awtgeom-1.8.132-20200519.jar" ]; then
    mvn install:install-file \
        -Dfile="$JARS_DIR/j4ts-awtgeom-1.8.132-20200519.jar" \
        -DgroupId=org.jsweet \
        -DartifactId=j4ts-awtgeom \
        -Dversion=1.8.132-20200519 \
        -Dpackaging=jar \
        -DgeneratePom=true || echo "⚠️  Warning: j4ts-awtgeom installation failed"
fi

if [ -f "$JARS_DIR/j4ts-batik-svgpathparser-1.10.0-20170726.jar" ]; then
    mvn install:install-file \
        -Dfile="$JARS_DIR/j4ts-batik-svgpathparser-1.10.0-20170726.jar" \
        -DgroupId=org.jsweet \
        -DartifactId=j4ts-batik-svgpathparser \
        -Dversion=1.10.0-20170726 \
        -Dpackaging=jar \
        -DgeneratePom=true || echo "⚠️  Warning: j4ts-batik-svgpathparser installation failed"
fi

if [ -f "$JARS_DIR/j4ts-swingundo-1.8.132-20170726.jar" ]; then
    mvn install:install-file \
        -Dfile="$JARS_DIR/j4ts-swingundo-1.8.132-20170726.jar" \
        -DgroupId=org.jsweet \
        -DartifactId=j4ts-swingundo \
        -Dversion=1.8.132-20170726 \
        -Dpackaging=jar \
        -DgeneratePom=true || echo "⚠️  Warning: j4ts-swingundo installation failed"
fi

echo
echo "✅ JSweet dependencies installation completed!"
echo
echo "📍 Dependencies installed to: ~/.m2/repository/org/jsweet/"
echo "🚀 Next steps:"
echo "   1. Run 'mvn clean package' to build the project"
echo "   2. Run './scripts/validate-setup.sh' to verify installation"
echo "   3. Test compilation: java -jar target/jsweet-cli-0.1.0.jar src/test/resources/HelloWorld.java -o output/"
echo