#!/usr/bin/env python3

"""
GWT to TypeScript Compilation Script (Java 8 Version)

Compiles GWT source code to TypeScript using JSweet CLI with Java 8.
Uses uv for Python dependency management.

Usage:
    uv run gen-gwt-java8.py [--verbose] [--target TARGET]

Requirements:
    - Maven project built (target/jsweet-cli-0.1.0.jar exists)
    - Java 8 installed via mise (corretto-8.422.05.1)
    - GWT source at gwt-to-ts-test/gwt-2.12.0/
"""

import argparse
import os
import subprocess
import sys
from pathlib import Path


GWT_USER_SRC_DIR = "gwt-to-ts-test/gwt-2.12.2/user/src"
JSWEET_JAR = Path("target/jsweet-cli-0.1.0.jar")

def check_requirements():
    """Check that required files and directories exist."""
    # Check for JSweet CLI jar
    if not JSWEET_JAR.exists():
        print("❌ JSweet CLI jar not found. Run 'mvn clean package' first.")
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
    output_dir = Path("gwt-ts/user-ts")
    output_dir.mkdir(parents=True, exist_ok=True)
    print(f"📁 Output directory: {output_dir.absolute()}")
    return output_dir


def build_exclude_patterns(exclude_patterns):
    """Convert exclude patterns to glob patterns for JSweet CLI."""
    if not exclude_patterns:
        return []

    # Convert simple patterns to glob patterns
    glob_patterns = []
    for pattern in exclude_patterns:
        # Add wildcard patterns to match anywhere in path
        glob_patterns.append(f"**/*{pattern}*/**/*.java")
        glob_patterns.append(f"**/*{pattern}*.java")

    return glob_patterns


def run_jsweet_compilation(source_root, output_dir, target="ES5", verbose=False, exclude_patterns=None):
    """Run JSweet CLI compilation using Java 8."""

    # Build JSweet CLI command using Java 8 via mise
    cmd = [
        "mise", "exec", "java@corretto-8.422.05.1", "--",
        "java", "-jar", str(JSWEET_JAR),
        "--output", str(output_dir),
        "--target", target
    ]

    if verbose:
        cmd.append("--verbose")

    # Add exclusion patterns if provided
    if exclude_patterns:
        glob_patterns = build_exclude_patterns(exclude_patterns)
        if glob_patterns:
            cmd.extend(["--excludes", ",".join(glob_patterns)])

    # Add source root as last argument
    cmd.append(str(source_root))

    print(f"🔧 Running JSweet compilation with Java 8...")
    print(f"   Source: {source_root}")
    print(f"   Output: {output_dir}")
    print(f"   Target: {target}")

    if exclude_patterns:
        print(f"   Excluding: {', '.join(exclude_patterns)}")

    if verbose:
        print(f"   Command: {' '.join(cmd)}")

    # Run compilation
    try:
        result = subprocess.run(
            cmd,
            cwd=Path.cwd(),
            capture_output=not verbose,  # Show output in real-time if verbose
            text=True,
            timeout=300  # 5 minute timeout
        )

        if result.returncode == 0:
            print("✅ JSweet compilation completed successfully")
            if not verbose and result.stdout:
                # Show summary even in non-verbose mode
                lines = result.stdout.strip().split('\n')
                for line in lines[-5:]:  # Show last 5 lines
                    if "Generated:" in line or "Compilation completed" in line:
                        print(f"   {line}")
            return True
        else:
            print(f"❌ JSweet compilation failed (exit code: {result.returncode})")
            if result.stderr:
                print("Error output:")
                print(result.stderr)
            return False

    except subprocess.TimeoutExpired:
        print("❌ JSweet compilation timed out (5 minutes)")
        return False
    except Exception as e:
        print(f"❌ Error running JSweet compilation: {e}")
        return False


def main():
    """Main script execution."""
    parser = argparse.ArgumentParser(
        description="Compile GWT source to TypeScript using JSweet CLI with Java 8",
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
        default=["org/hibernate", "javax/validation", "com/google/web/bindery", "com/google/gwt/core/ext"],
        help="Patterns to exclude from compilation"
    )

    args = parser.parse_args()

    print("🔄 GWT to TypeScript Compilation (Java 8)")
    print("=" * 50)

    # Check prerequisites
    if not check_requirements():
        sys.exit(1)

    # Set up paths
    source_root = Path(GWT_USER_SRC_DIR)
    output_dir = create_output_directory()

    # Count total Java files for information
    total_java_files = len(list(source_root.rglob("*.java")))

    print(f"📊 Source Analysis:")
    print(f"   Total Java files: {total_java_files}")
    print(f"   Exclude patterns: {', '.join(args.exclude) if args.exclude else 'None'}")

    # Run compilation with exclusion patterns passed to JSweet CLI
    success = run_jsweet_compilation(
        source_root=source_root,
        output_dir=output_dir,
        target=args.target,
        verbose=args.verbose,
        exclude_patterns=args.exclude
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
