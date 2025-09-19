#!/usr/bin/env python3
"""
Generate TypeScript samples from Java source using JSweet CLI.

This script processes test-samples/ directories, each containing:
- java/ - Java source files
- ts/ - Generated TypeScript output (created by this script)

Usage:
  uv run scripts/gen-samples.py                    # Process all samples
  uv run scripts/gen-samples.py --path jsni        # Process samples matching 'jsni'
  uv run scripts/gen-samples.py --path hello       # Process samples matching 'hello'
"""
# /// script
# requires-python = ">=3.8"
# dependencies = []
# ///

import argparse
import subprocess
import sys
from pathlib import Path
import shutil

def run_command(cmd: list[str], cwd: Path = None) -> tuple[bool, str]:
    """Run a command and return (success, output)."""
    try:
        result = subprocess.run(
            cmd,
            cwd=cwd,
            capture_output=True,
            text=True,
            check=True
        )
        return True, result.stdout
    except subprocess.CalledProcessError as e:
        return False, f"Command failed: {' '.join(cmd)}\nError: {e.stderr}"
    except FileNotFoundError:
        return False, f"Command not found: {cmd[0]}"

def is_jar_outdated(jar_path: Path, project_root: Path) -> bool:
    """Check if JAR file is older than source files."""
    if not jar_path.exists():
        return True

    jar_mtime = jar_path.stat().st_mtime

    # Check if any Java source file is newer than the JAR
    src_dirs = [
        project_root / "src" / "main" / "java",
        project_root / "src" / "test" / "java"
    ]

    for src_dir in src_dirs:
        if src_dir.exists():
            for java_file in src_dir.rglob("*.java"):
                if java_file.stat().st_mtime > jar_mtime:
                    return True

    # Check if pom.xml is newer than JAR
    pom_path = project_root / "pom.xml"
    if pom_path.exists() and pom_path.stat().st_mtime > jar_mtime:
        return True

    return False

def find_jar_file(project_root: Path) -> Path:
    """Find the JSweet CLI JAR file, rebuilding if necessary."""
    target_dir = project_root / "target"
    jar_files = list(target_dir.glob("jsweet-cli-*.jar"))

    jar_path = jar_files[0] if jar_files else None

    # Check if we need to build/rebuild
    should_rebuild = (
        not jar_path or  # No JAR exists
        is_jar_outdated(jar_path, project_root)  # JAR is outdated
    )

    if should_rebuild:
        if jar_path:
            print(f"🔄 JAR is outdated. Rebuilding project...")
        else:
            print("❌ No JAR file found. Building project...")

        success, output = run_command(["mvn", "clean", "package", "-q"], cwd=project_root)
        if not success:
            raise RuntimeError(f"Failed to build JAR: {output}")

        # Re-find JAR files after build
        jar_files = list(target_dir.glob("jsweet-cli-*.jar"))
        if not jar_files:
            raise RuntimeError("JAR file not found after build")

        jar_path = jar_files[0]
        print(f"✅ Built JAR: {jar_path.name}")

    return jar_path

def process_sample(sample_dir: Path, jar_path: Path) -> bool:
    """Process a single sample directory."""
    java_dir = sample_dir / "java"
    ts_dir = sample_dir / "ts"

    if not java_dir.exists():
        print(f"⚠️  Skipping {sample_dir.name} - no java/ directory")
        return True

    # Clean and create TypeScript output directory
    if ts_dir.exists():
        shutil.rmtree(ts_dir)
    ts_dir.mkdir()

    print(f"🔄 Processing {sample_dir.name}...")

    # Determine if single file or directory
    java_files = list(java_dir.rglob("*.java"))
    if not java_files:
        print(f"⚠️  No Java files found in {java_dir}")
        return True

    # Use JSweet CLI with auto-detection
    cmd = [
        "java", "-jar", str(jar_path),
        str(java_dir),
        "-o", str(ts_dir),
        "--target", "ES6",
        "--verbose"
    ]

    print(f"   Running: {' '.join(cmd[-4:])}")  # Show key args
    success, output = run_command(cmd)

    if success:
        print(f"✅ {sample_dir.name} - Generated TypeScript successfully")

        # Count generated files
        ts_files = list(ts_dir.rglob("*.ts"))
        js_files = list(ts_dir.rglob("*.js"))
        print(f"   📁 Generated: {len(ts_files)} .ts files, {len(js_files)} .js files")

        return True
    else:
        print(f"❌ {sample_dir.name} - Compilation failed")
        print(f"   Error: {output}")
        return False

def parse_args():
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description="Generate TypeScript samples from Java source using JSweet CLI",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  uv run scripts/gen-samples.py                # Process all samples
  uv run scripts/gen-samples.py --path jsni    # Process samples containing 'jsni'
  uv run scripts/gen-samples.py --path hello   # Process samples containing 'hello'
        """.strip()
    )
    parser.add_argument(
        "--path",
        type=str,
        help="Filter samples by path containing this string (loose match)"
    )
    return parser.parse_args()

def filter_samples(sample_dirs: list[Path], path_filter: str = None) -> list[Path]:
    """Filter sample directories based on path matching."""
    if not path_filter:
        return sample_dirs

    # Case-insensitive loose matching
    filtered = [d for d in sample_dirs if path_filter.lower() in d.name.lower()]
    return filtered

def main():
    """Main entry point."""
    args = parse_args()

    # Find project root (directory containing pom.xml)
    script_dir = Path(__file__).parent
    project_root = script_dir.parent

    if not (project_root / "pom.xml").exists():
        print("❌ pom.xml not found. Run from project root.")
        sys.exit(1)

    # Find test-samples directory
    samples_dir = project_root / "test-samples"
    if not samples_dir.exists():
        print("❌ test-samples/ directory not found")
        sys.exit(1)

    # Find or build JAR file
    try:
        jar_path = find_jar_file(project_root)
        print(f"📦 Using JAR: {jar_path.name}")
    except RuntimeError as e:
        print(f"❌ {e}")
        sys.exit(1)

    # Find sample directories (those containing java/ subdirectory)
    all_sample_dirs = [d for d in samples_dir.iterdir()
                      if d.is_dir() and (d / "java").exists()]

    # Apply path filtering
    sample_dirs = filter_samples(all_sample_dirs, args.path)

    if not sample_dirs:
        if args.path:
            print(f"❌ No sample directories matching '{args.path}' found")
            print(f"Available samples: {', '.join(d.name for d in all_sample_dirs)}")
        else:
            print("❌ No sample directories with java/ subdirectories found")
        sys.exit(1)

    sample_dirs.sort()  # Process in alphabetical order

    if args.path:
        print(f"🔍 Filtering for samples matching: '{args.path}'")
    print(f"🚀 Processing {len(sample_dirs)} sample directories...")
    print()

    success_count = 0
    for sample_dir in sample_dirs:
        if process_sample(sample_dir, jar_path):
            success_count += 1
        print()  # Blank line between samples

    print(f"📊 Summary: {success_count}/{len(sample_dirs)} samples processed successfully")

    if success_count < len(sample_dirs):
        print("⚠️  Some samples failed. Check output above for details.")
        sys.exit(1)
    else:
        print("🎉 All samples processed successfully!")

if __name__ == "__main__":
    main()
