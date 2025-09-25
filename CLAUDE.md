# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This project is an exploration of porting GWT (Google Web Toolkit) from Java to TypeScript.

It so happens that this project is a fork of JSweet (http://www.jsweet.org), a Java to TypeScript/JavaScript transpiler. 
It is like this because JSweet was getting us pretty close to what we wanted. That said, other tooling can be used as part of the pipeline (for example we could look at add vite plugins on the TS side etc).

## The goal

Convert the GWT User runtime to TypeScript. Compile that TypeScript using vite/esbuild and run it in a browser.

Success is being able to render a simple Hello World app using the ported GWT Typescript runtime.

### How 

Currently we're focusing on porting as much of gwt-user as we can - 1:1. 
I dont think this is 100% possible nor necessary, so we will need to do some analysis and see if there are any parts of the porting that we can stub out.

* An example - There seems to be some code that is only there for the old GWT compiler. Maybe that can be stubbed out?
* 


# Current Pipeline

1. Run jsweet on gwt user codebase (add adapters as needed) - convert to ts and place in gwt-to-ts-test/gwt-ts/user-ts.
2. Run `pnpm vite build` in sample-ts (which depents on user-ts).
3. Look for ts compile errors - or runtime errors in the browser.

We have a few places where we can fix things, we can add adapters in jsweet, we can fix the jsweet transpiler itself, or we can start looking at post-processing the ts (aka in vite).

As a rule - jsweet fixes should be considered framework fixes, and should be applicable to typescript conversion in general.
Adapters can be used to hook into the jsweet flow, and is best suited for custom gwt specific tweaks (eg JSNI, or skipping bits of code)
Post processing could be an option too, say we want to stub out some class, we could get that copied in before running vite etc? Or the circular dependencies issue could be fixed like that?

# What works - what's todo..
- [x] JSNI - in jsweet we add the method body as a comment, then in our adapter we strip out the comment tags and set the raw javascript.
- [x] Static inner classes - we've updated the transpiler to render these correctly.
- [ ] Circular dependencies - I had another repo where we just merged the 2 dependees into 1 module, may do that post processing? 
- [ ] Namespace ]
Below is the JSweet info .. build etc. 

## Mvn run cli 

You can run the cli like so:
```bash 
mvn exec:java -pl cli -Dexec.args="--output gwt-to-ts-test/gwt-ts/user-ts --target ES5 --excludes *bindery* --excludes *webgl* --excludes *websocket* --excludes *hibernate* --excludes *javax/validation* --excludes *validation* --excludes *logging --excludes *i18n* --excludes *rpc* --excludes *requestfactory* --excludes *autobean* --excludes *editor* --excludes *safehtml* --excludes *aria* --excludes dom/builder --excludes *junit* --excludes **/server/** --excludes **/vm/** gwt-to-ts-test/gwt-2.11.0/user/src"
```
Note that you can exclude pacakges etc.



## Build System

The project uses Maven as its primary build system with a multi-module structure:

- **Root**: Parent POM (`pom.xml`) that manages common dependencies and plugin versions
- **Main modules**: 
  - `transpiler/`: Core Java to TypeScript/JavaScript compiler
  - `core-lib/es5/` & `core-lib/es6/`: Core JavaScript APIs for different ECMAScript versions
  - `candy-generator/`: Tool to generate Java APIs from TypeScript definition files
  - `candy-generator-util/`: Utilities for candy generation
  - `typescript.java-ts.core/`: TypeScript integration core

### Common Build Commands

```bash
# Build entire project
mvn clean install

# Build specific module (from root)
mvn clean install -pl transpiler

# Run tests for transpiler module
cd transpiler && mvn test

# Run specific test
cd transpiler && mvn test -Dtest=StructuralTests

# Create transpiler JAR with dependencies
cd transpiler && mvn clean package

# Clean build artifacts
mvn clean
```

### Test Commands

Tests are primarily located in `transpiler/src/test/java/` and use JUnit 4. The test framework extends `AbstractTest` class which provides utilities for compilation and evaluation tests.

```bash
# Run all transpiler tests
cd transpiler && mvn test

# Run tests with specific pattern
cd transpiler && mvn test -Dtest="*Structural*"

# Run single test method
cd transpiler && mvn test -Dtest=StructuralTests#testPrivateFieldNameClashes
```

## Architecture

### Core Components

1. **JSweetTranspiler** (`transpiler/src/main/java/org/jsweet/transpiler/`): Main transpilation engine
2. **JSweetCommandLineLauncher** (`transpiler/src/main/java/org/jsweet/JSweetCommandLineLauncher.java`): CLI interface
3. **Candy System**: Java API definitions for JavaScript libraries (similar to C header files)
4. **TypeScript Integration**: Uses Microsoft TypeScript compiler for final JavaScript generation

### Module Structure

- `transpiler/src/main/java/org/jsweet/transpiler/`: Core transpilation logic
  - `TypeScriptAdapter.java`: Bridges to TypeScript compiler  
  - `Java2TypeScriptTranslator.java`: Main translation logic
  - `JSweetContext.java`: Transpilation context management
- `transpiler/src/test/java/`: Test framework and test cases
  - `org/jsweet/test/transpiler/AbstractTest.java`: Base test class
  - `source/`: Test source files organized by test category

### Key Concepts

- **Candies**: Java API definitions that bridge JavaScript libraries (similar to TypeScript .d.ts files)
- **Source-to-source compilation**: Java → TypeScript → JavaScript pipeline
- **Bridge pattern**: Uses type definitions to allow Java code to call JavaScript APIs
- **Module support**: Supports various module systems (CommonJS, AMD, UMD)

## Development Workflow

### Testing Framework

JSweet uses a custom testing framework built on JUnit that supports both compilation and evaluation tests:

1. **Compilation tests**: Verify that source code compiles with expected errors/warnings
2. **Evaluation tests**: Compile and run code, then verify execution results using `$export` macro

Example test structure:
```java
@Test
public void testFeature() {
    eval((logHandler, r) -> {
        logHandler.assertNoProblems();
        assertEquals(expectedValue, r.get("exportedVariable"));
    }, getSourceFile(TestClass.class));
}
```

### Code Organization

- Place test classes in `transpiler/src/test/java/source/` following package structure
- Test files should match the test method name for automatic discovery
- Use `$export("name", value)` in test Java code to export values for assertion
- Tests automatically run with and without modules unless specified otherwise

## Important Files

- `pom.xml`: Root Maven configuration with dependency management
- `transpiler/pom.xml`: Core transpiler module configuration  
- `CONTRIBUTING.md`: Contributor guidelines and test writing instructions
- `doc/jsweet-language-specifications.md`: Comprehensive language documentation
- `JSweet4.md`: Future development plans and roadmap

## Development Requirements

- Java 11+ (configured in transpiler/pom.xml with release version 11)
- Maven 3.3.9+ (enforced by maven-enforcer-plugin)
- Node.js and TypeScript (for final JavaScript generation)

## Repository Branches

- **develop**: Default development branch (uses Git Flow)
- **master/main**: Production releases
- Use `git flow feature start <name>` for new features
- if i show an issue w/ generated ts - don't fix the generated ts. we need to look at the source java and the transpiler to see how to fix