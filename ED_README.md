need to get the mcp server workign properly.

# JSweet Development Guide


.. todo ..

I don't think the jsni fix is fully working.
Look at button element as an example.
user-ts/com/google/gwt/dom/client/ButtonElement.ts:
or
/Users/ed.eustace/dev/github.com/ed-eustace/jsweet/gwt-to-ts-test/gwt-ts/user-ts/com/google/gwt/user/client/Window.ts

## Building the Project

```bash
# Build all modules (skip tests for faster build)
mvn clean install -DskipTests

# Build only CLI and its dependencies
mvn clean package -pl cli -am -DskipTests
```

## Rebuilding After Transpiler Changes

When you make changes to the transpiler and need the CLI to pick them up:

```bash
# 1. First rebuild the transpiler module with your changes
mvn clean install -pl transpiler -DskipTests -q

# 2. Then rebuild the CLI module to pick up the updated transpiler
cd cli && mvn clean compile -DskipTests -q

# 3. Now run your CLI commands - they will use the updated transpiler
cd .. && mvn exec:java -pl cli -Dexec.args="your-args-here"
```

**Important**: The CLI module depends on the transpiler module, so you must rebuild the transpiler first, then the CLI to pick up the changes.

## Running the CLI via Maven

### Basic Usage
```bash
# Show help
mvn exec:java -pl cli -Dexec.args="--help"

# Show version
mvn exec:java -pl cli -Dexec.args="--version"

# Compile a Java file
mvn exec:java -pl cli -Dexec.args="/path/to/MyClass.java --output ./typescript --verbose"

# Compile a directory
mvn exec:java -pl cli -Dexec.args="/path/to/src --output ./typescript --target ES6"
```

## Debugging the CLI

### Method 1: Using MAVEN_OPTS Environment Variable (Recommended)
```bash
export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
mvn exec:java -pl cli -Dexec.args="--help"
```

### Method 2: Using exec.jvmArgs
```bash
mvn exec:java -pl cli \
  -Dexec.args="--help" \
  -Dexec.jvmArgs="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

## IDE Debug Setup

### IntelliJ IDEA
1. Start the CLI with debug options (using MAVEN_OPTS method above)
2. The process will suspend and wait for debugger
3. In IntelliJ: **Run → Edit Configurations → Add New → Remote JVM Debug**
4. Set **Host**: `localhost`, **Port**: `5005`
5. Click **Debug** to attach
6. Set breakpoints anywhere in the codebase (CLI, transpiler, core libs)

### VS Code
1. Add to your `launch.json`:
```json
{
  "type": "java",
  "name": "Debug JSweet CLI",
  "request": "attach",
  "hostName": "localhost",
  "port": 5005
}
```
2. Start CLI with debug options
3. Use **Run → Start Debugging** and select "Debug JSweet CLI"

## Project Structure

```
jsweet/
├── cli/                    # New CLI module with picocli
├── transpiler/            # Core JSweet transpiler 
├── core-lib/es5/         # ES5 JavaScript APIs
├── core-lib/es6/         # ES6 JavaScript APIs
├── typescript.java-ts.core/  # TypeScript integration
├── candy-generator/      # Tool for generating Java APIs
└── candy-generator-util/ # Utilities for candy generation
```

## CLI Features

The new CLI (`cli/`) provides:
- **picocli** for argument parsing with help/version support
- **JSweet transpiler integration** for Java to TypeScript compilation
- **Single file or directory** compilation
- **Configurable output directory** and target ES version
- **Verbose logging** and **exclude patterns**
- **Full debugging support** across all modules

## Development Workflow

1. Make changes to any module (CLI, transpiler, etc.)
2. Run with debugging enabled
3. Set breakpoints in IDE
4. Debug the full pipeline from CLI input to TypeScript output

## Common Debug Scenarios

### Debug CLI argument parsing
Set breakpoints in `cli/src/main/java/org/jsweet/cli/Main.java`

### Debug transpilation process  
Set breakpoints in `transpiler/src/main/java/org/jsweet/transpiler/JSweetTranspiler.java`

### Debug TypeScript generation
Set breakpoints in `transpiler/src/main/java/org/jsweet/transpiler/Java2TypeScriptTranslator.java`

### Debug custom extensions
Set breakpoints in `cli/src/main/java/org/jsweet/cli/extension/` classes