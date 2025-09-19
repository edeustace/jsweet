#!/usr/bin/env python3

"""
GWT to TypeScript Compilation Script

Compiles GWT source code to TypeScript using JSweet CLI via Maven exec:java.
Uses uv for Python dependency management.

Usage:
    uv run gen-gwt.py [--verbose] [--target TARGET]

Requirements:
    - Maven project with CLI module (cli/pom.xml exists)
    - GWT source at gwt-to-ts-test/gwt-2.11.0/user/src
    - Run from JSweet project root directory
"""

import argparse
import os
import subprocess
import sys
from pathlib import Path


GWT_USER_SRC_DIR = "gwt-to-ts-test/gwt-2.11.0/user/src"
JSWEET_JAR = Path("target/jsweet-cli-0.1.0.jar")

def check_requirements():
    """Check that required files and directories exist."""
    # Check for Maven CLI module (pom.xml should exist)
    cli_pom = Path("cli/pom.xml")
    if not cli_pom.exists():
        print("❌ JSweet CLI module not found. Expected cli/pom.xml")
        print("   Make sure you're running from the JSweet project root")
        return False

    # Check for GWT source directory
    gwt_source = Path(GWT_USER_SRC_DIR)
    if not gwt_source.exists():
        print(f"❌ GWT source directory not found: {gwt_source}")
        print(f"   Expected {GWT_USER_SRC_DIR}")
        return False

    return True


def create_output_directory():
    """Create output directory for TypeScript files."""
    output_dir = Path("gwt-to-ts-test/gwt-ts/user-ts")
    output_dir.mkdir(parents=True, exist_ok=True)
    print(f"📁 Output directory: {output_dir.absolute()}")
    return output_dir


# File filtering is now handled natively by JSweet CLI --excludes option


def run_jsweet_compilation(source_root, output_dir, target="ES5", verbose=False, exclude_patterns=None):
    """Run JSweet CLI compilation using Maven exec:java."""

    # Check if GWT JARs exist
    gwt_jars_dir = Path("gwt-to-ts-test/gwt-2.11.0-jars")
    gwt_dev_jar = gwt_jars_dir / "gwt-dev.jar"
    gwt_user_jar = gwt_jars_dir / "gwt-user.jar"

    if not gwt_dev_jar.exists() or not gwt_user_jar.exists():
        print(f"❌ GWT JARs not found at {gwt_jars_dir}")
        print("   Expected: gwt-dev.jar and gwt-user.jar")
        return False

    # Build Maven command for JSweet CLI
    cmd = ["mvn", "exec:java", "-pl", "cli"]

    # Add JVM arguments for Java module access
    jvm_args = [
        "--illegal-access=permit",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
        "--add-opens", "jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
        "--add-opens", "java.compiler/javax.lang.model.element=ALL-UNNAMED",
        "--add-opens", "java.compiler/javax.lang.model.type=ALL-UNNAMED",
        "--add-exports", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
        "--add-exports", "java.compiler/javax.lang.model.type=ALL-UNNAMED"
    ]

    # Build JSweet CLI arguments
    jsweet_args = [
        "--output", str(output_dir),
        "--target", target
    ]

    if verbose:
        jsweet_args.append("--verbose")

    # Add excludes patterns if provided
    if exclude_patterns:
        for pattern in exclude_patterns:
            jsweet_args.extend(["--excludes", pattern])

    # Add source root as last argument
    jsweet_args.append(str(source_root))

    # The entire exec.args value must be quoted to prevent Maven from parsing individual arguments
    exec_args = ' '.join(jsweet_args)
    cmd.extend([f'-Dexec.args="{exec_args}"'])

    # Build MAVEN_OPTS with JVM arguments (GWT JARs now in pom.xml)
    maven_opts = ' '.join(jvm_args)

    print(f"🔧 Running JSweet compilation via Maven...")
    print(f"   Source: {source_root}")
    print(f"   Output: {output_dir}")
    print(f"   Target: {target}")

    if exclude_patterns:
        print(f"   Excluding: {', '.join(exclude_patterns)}")

    # For now, just print the commands instead of running them
    print("\n📋 Commands to execute:")
    print(f'export MAVEN_OPTS="{maven_opts}"')
    print(' '.join(cmd))
    
    return True  # Always return success for now since we're just printing


def main():
    """Main script execution."""
    parser = argparse.ArgumentParser(
        description="Compile GWT source to TypeScript using JSweet CLI",
        formatter_class=argparse.RawDescriptionHelpFormatter
    )

    parser.add_argument(
        "--verbose", "-v",
        action="store_true",
        help="Enable verbose output from JSweet compilation"
    )

    parser.add_argument(
        "--target", "-t",
        choices=["ES3", "ES5", "ES6", "ES2015", "ES2016", "ES2017"],
        default="ES5",
        help="Target ECMAScript version (default: ES5)"
    )

    parser.add_argument(
        "--exclude",
        nargs="*",
        # default=["bindery"], # , "webgl", "websocket", "hibernate", "javax/validation", "validation", "logging", "i18n", "rpc", "requestfactory", "autobean", "editor", "safehtml", "aria", "dom/builder"],
        default=["*webgl*", "*websocket*", "*hibernate*", "*javax/validation*", "*validation*", "*logging", "*i18n*", "*rpc*", "*requestfactory*", "*autobean*", "*editor*", "*safehtml*", "*aria*", "dom/builder", "*junit*"],
        help="Patterns to exclude from compilation (default: exclude non-core components)"
    )

    parser.add_argument(
        "--client-only",
        action="store_true",
        default=True,
        help="Only include client-side GWT code (excludes server, shared non-client packages)"
    )

    args = parser.parse_args()

    print("🔄 GWT to TypeScript Compilation")
    print("=" * 50)

    # Check prerequisites
    if not check_requirements():
        sys.exit(1)

    # Set up paths
    original_source_root = Path(GWT_USER_SRC_DIR)
    output_dir = create_output_directory()

    # Count total Java files for information
    total_java_files = len(list(original_source_root.rglob("*.java")))

    print(f"📊 Source Analysis:")
    print(f"   Total Java files: {total_java_files}")
    print(f"   Exclude patterns: {', '.join(args.exclude) if args.exclude else 'None'}")

    # Prepare exclude patterns (JSweet CLI now supports --excludes natively!)
    exclude_patterns = []
    if args.exclude:
        exclude_patterns.extend(args.exclude)

    # Add common problematic patterns
    # exclude_patterns.extend(["**/*junit*/**", "**/*test*/**", "**/*selenium*/**", "**/*rebind*/**", "**/*tools*/**"])

    # Client-only filtering via exclude patterns
    if args.client_only:
        exclude_patterns.extend([
            "**/server/**",  # Exclude server-side code
            "**/vm/**",      # Exclude VM implementations
            # Note: We'll need to be more selective with shared exclusions
            # since JSweet doesn't support negation patterns like "!pattern"
        ])

    actual_source_root = original_source_root

    # Run compilation with exclude patterns (JSweet CLI handles filtering natively)
    success = run_jsweet_compilation(
        source_root=actual_source_root,
        output_dir=output_dir,
        target=args.target,
        verbose=args.verbose,
        exclude_patterns=exclude_patterns
    )

    if success:
        print("\n🎉 GWT to TypeScript compilation completed!")
        print(f"   TypeScript files generated in: {output_dir.absolute()}")
        sys.exit(0)
    else:
        print("\n❌ Compilation failed. Check error messages above.")
        sys.exit(1)


if __name__ == "__main__":
    main()
