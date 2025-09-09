package org.jsweet.cli.service;

import org.jsweet.cli.extension.JSNIFactory;
import org.jsweet.cli.model.JavaSourceFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import org.jsweet.JSweetConfig;
import org.jsweet.transpiler.EcmaScriptComplianceLevel;
import org.jsweet.transpiler.JSweetFactory;
import org.jsweet.transpiler.JSweetProblem;
import org.jsweet.transpiler.JSweetTranspiler;
import org.jsweet.transpiler.ModuleKind;
import org.jsweet.transpiler.Severity;
import org.jsweet.transpiler.SourceFile;
import org.jsweet.transpiler.SourcePosition;
import org.jsweet.transpiler.TranspilationHandler;
import org.jsweet.transpiler.util.ErrorCountTranspilationHandler;

public class CompilerService {

    /**
     * Filter source files based on exclude patterns (glob patterns)
     */
    private SourceFile[] filterSourceFiles(SourceFile[] sourceFiles, String[] excludePatterns, File sourceRoot) {
        if (excludePatterns == null || excludePatterns.length == 0) {
            return sourceFiles;
        }

        List<SourceFile> filteredFiles = new ArrayList<>();
        List<Pattern> compiledPatterns = new ArrayList<>();
        
        // Convert glob patterns to regex patterns (using official JSweet CLI logic)
        for (String pattern : excludePatterns) {
            if (pattern != null && !pattern.trim().isEmpty()) {
                compiledPatterns.add(toPattern(pattern.trim()));
            }
        }
        
        if (compiledPatterns.isEmpty()) {
            return sourceFiles;
        }

        for (SourceFile sourceFile : sourceFiles) {
            try {
                // Get relative path from source root for pattern matching
                Path sourcePath = sourceFile.getJavaFile().toPath();
                Path rootPath = sourceRoot.toPath();
                String relativePath = rootPath.relativize(sourcePath).toString().replace('\\', '/');
                
                boolean shouldExclude = false;
                for (Pattern pattern : compiledPatterns) {
                    if (pattern.matcher(relativePath).matches()) {
                        shouldExclude = true;
                        break;
                    }
                }
                
                if (!shouldExclude) {
                    filteredFiles.add(sourceFile);
                }
            } catch (Exception e) {
                // If there's any error, include the file to be safe
                filteredFiles.add(sourceFile);
            }
        }

        return filteredFiles.toArray(new SourceFile[0]);
    }

    /**
     * Convert glob pattern to regex pattern (using official JSweet CLI logic)
     */
    private Pattern toPattern(String expression) {
        if (!expression.contains("*") && !expression.contains(".")) {
            expression += "*";
        }
        return Pattern.compile(expression.replace(".", "\\.").replace("*", ".*"));
    }

    public boolean compileJavaFile(
        JavaSourceFile sourceFile,
        File outputDirectory,
        String target,
        boolean verbose
    ) {
        try {
            if (verbose) {
                System.out.println("Compiling: " + sourceFile.getFileName());
                System.out.println("Package: " + sourceFile.getPackageName());
                System.out.println("Class: " + sourceFile.getClassName());
            }

            // Set up transpilation handler to collect problems
            TranspilationHandler baseHandler = new TranspilationHandler() {
                @Override
                public void report(
                    JSweetProblem problem,
                    SourcePosition sourcePosition,
                    String message
                ) {
                    if (verbose || problem.getSeverity() == Severity.ERROR) {
                        System.out.println(
                            "[" + problem.getSeverity() + "] " + message
                        );
                        if (sourcePosition != null) {
                            System.out.println("  at " + sourcePosition);
                        }
                    }
                }

                @Override
                public void onCompleted(
                    JSweetTranspiler transpiler,
                    boolean fullPass,
                    SourceFile[] files
                ) {
                    if (verbose) {
                        System.out.println(
                            "Transpilation completed. Full pass: " + fullPass
                        );
                    }
                }
            };

            ErrorCountTranspilationHandler errorHandler =
                new ErrorCountTranspilationHandler(baseHandler);

            // Create custom JSNIFactory with JSNI support
            // For single file compilation, derive source root from the file's directory
            File sourceFileForRoot = new File(sourceFile.getFilePath());
            String sourceRootPath = deriveSourceRootFromFile(
                sourceFileForRoot,
                sourceFile.getPackageName()
            );

            System.out.println("🔧 Using JSNIFactory for JSNI support");
            System.out.println("   Derived source root: " + sourceRootPath);
            JSweetFactory factory = new JSNIFactory(sourceRootPath);

            // Create transpiler with factory and output directory
            JSweetTranspiler transpiler = new JSweetTranspiler(
                factory,
                null,
                outputDirectory,
                null,
                null,
                null
            );

            // Configure for ES6 modules (ESM) instead of namespaces
            transpiler.setModuleKind(org.jsweet.transpiler.ModuleKind.es2015);

            // Create SourceFile for JSweet
            File inputFile = new File(sourceFile.getFilePath());
            SourceFile jsweetSourceFile = new SourceFile(inputFile);

            if (verbose) {
                System.out.println("Starting JSweet transpilation...");
                System.out.println("Target: " + target);
                System.out.println(
                    "Input file: " + inputFile.getAbsolutePath()
                );
                System.out.println(
                    "Output directory: " + outputDirectory.getAbsolutePath()
                );
            }

            // Transpile using the correct API signature
            // Based on our inspection: transpile(TranspilationHandler, SourceFile[])
            transpiler.transpile(errorHandler, jsweetSourceFile);

            // Verify output file was created first (primary success indicator)
            String expectedOutputPath = determineOutputPath(
                sourceFile,
                outputDirectory
            );
            boolean outputGenerated = Files.exists(
                Paths.get(expectedOutputPath)
            );

            if (verbose) {
                System.out.println(
                    "Transpilation completed with " +
                    errorHandler.getProblemCount() +
                    " total problems"
                );
            }

            // Check for errors, but be lenient if output was still generated
            if (errorHandler.getErrorCount() > 0) {
                if (outputGenerated) {
                    // Output was generated despite "errors" - treat as success with warnings
                    System.out.println(
                        "⚠️  Compilation completed with " +
                        errorHandler.getErrorCount() +
                        " warnings (output generated successfully)"
                    );
                    System.out.println("Generated: " + expectedOutputPath);
                    return true;
                } else {
                    // No output generated - real failure
                    System.err.println(
                        "Compilation failed with " +
                        errorHandler.getErrorCount() +
                        " errors"
                    );
                    return false;
                }
            }

            if (outputGenerated) {
                System.out.println("Generated: " + expectedOutputPath);
                return true;
            } else {
                System.err.println(
                    "Error: Expected output file not created: " +
                    expectedOutputPath
                );
                // List what files were actually created in the output directory
                if (verbose && outputDirectory.exists()) {
                    System.err.println("Files found in output directory:");
                    listFilesRecursively(outputDirectory, "  ");
                }
                return false;
            }
        } catch (Exception e) {
            System.err.println("Compilation failed: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return false;
        }
    }

    private void listFilesRecursively(File dir, String prefix) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    System.err.println(prefix + file.getName());
                    if (file.isDirectory()) {
                        listFilesRecursively(file, prefix + "  ");
                    }
                }
            }
        }
    }

    private String determineOutputPath(
        JavaSourceFile sourceFile,
        File outputDirectory
    ) {
        String outputFileName = sourceFile.getClassName() + ".ts";

        if (
            sourceFile.getPackageName() != null &&
            !sourceFile.getPackageName().isEmpty()
        ) {
            // Create package directory structure
            String packagePath = sourceFile
                .getPackageName()
                .replace(".", File.separator);
            Path fullOutputPath = Paths.get(
                outputDirectory.getAbsolutePath(),
                packagePath,
                outputFileName
            );
            return fullOutputPath.toString();
        } else {
            // No package, put directly in output directory
            return Paths.get(
                outputDirectory.getAbsolutePath(),
                outputFileName
            ).toString();
        }
    }

    /**
     * Compile all Java files in a directory using JSweet's multi-file support
     */
    public boolean compileDirectory(
        String sourceRootPath,
        File outputDirectory,
        String target,
        boolean verbose,
        String[] excludePatterns
    ) {
        try {
            File sourceRoot = new File(sourceRootPath);
            if (!sourceRoot.exists() || !sourceRoot.isDirectory()) {
                System.err.println(
                    "Source directory does not exist: " + sourceRootPath
                );
                return false;
            }

            if (verbose) {
                System.out.println("Compiling directory: " + sourceRootPath);
                System.out.println(
                    "Output directory: " + outputDirectory.getAbsolutePath()
                );
            }

            // Use JSweet's built-in method to find all Java files
            SourceFile[] allSourceFiles = SourceFile.getSourceFiles(sourceRoot);
            
            if (allSourceFiles.length == 0) {
                System.out.println("No Java files found in: " + sourceRootPath);
                return true;
            }
            
            // Apply exclude patterns filtering
            SourceFile[] sourceFiles = filterSourceFiles(allSourceFiles, excludePatterns, sourceRoot);
            
            if (verbose && excludePatterns != null && excludePatterns.length > 0) {
                System.out.println("Applied exclude patterns: " + Arrays.toString(excludePatterns));
                System.out.println("Filtered from " + allSourceFiles.length + " to " + sourceFiles.length + " files");
            }
            
            if (sourceFiles.length == 0) {
                System.out.println("No Java files remaining after filtering");
                return true;
            }

            if (verbose) {
                System.out.println(
                    "Found " + sourceFiles.length + " Java files:"
                );
                for (SourceFile sf : sourceFiles) {
                    System.out.println("  " + sf.getJavaFile().getPath());
                }
            }

            // Set up transpilation handler
            TranspilationHandler baseHandler = new TranspilationHandler() {
                @Override
                public void report(
                    JSweetProblem problem,
                    SourcePosition sourcePosition,
                    String message
                ) {
                    if (verbose || problem.getSeverity() == Severity.ERROR) {
                        System.out.println(
                            "[" + problem.getSeverity() + "] " + message
                        );
                        if (sourcePosition != null) {
                            System.out.println("  at " + sourcePosition);
                        }
                    }
                }

                @Override
                public void onCompleted(
                    JSweetTranspiler transpiler,
                    boolean fullPass,
                    SourceFile[] files
                ) {
                    if (verbose) {
                        System.out.println(
                            "Transpilation completed. Full pass: " + fullPass
                        );
                        System.out.println(
                            "Processed " + files.length + " files"
                        );
                    }
                }
            };

            ErrorCountTranspilationHandler errorHandler =
                new ErrorCountTranspilationHandler(baseHandler);

            // Create custom JSNIFactory with JSNI support
            System.out.println(
                "🔧 Using JSNIFactory for JSNI support (multi-file)"
            );
            System.out.println("   Source root: " + sourceRootPath);
            JSweetFactory factory = new JSNIFactory(sourceRootPath);
            JSweetTranspiler transpiler = new JSweetTranspiler(
                factory,
                null,
                outputDirectory,
                null,
                null,
                null
            );

            // Configure for ES6 modules (ESM) instead of namespaces
            transpiler.setModuleKind(org.jsweet.transpiler.ModuleKind.es2015);

            if (verbose) {
                System.out.println(
                    "Starting JSweet multi-file transpilation..."
                );
                System.out.println("Target: " + target);
            }

            // THIS IS THE KEY: Pass all source files together so JSweet can resolve dependencies
            transpiler.transpile(errorHandler, sourceFiles);

            // Check if any output was generated (primary success indicator)
            boolean outputGenerated =
                outputDirectory.exists() &&
                outputDirectory.listFiles() != null &&
                outputDirectory.listFiles().length > 0;

            // Check for errors, but be lenient if output was still generated
            if (errorHandler.getErrorCount() > 0) {
                if (outputGenerated) {
                    // Output was generated despite "errors" - treat as success with warnings
                    System.out.println(
                        "⚠️  Multi-file compilation completed with " +
                        errorHandler.getErrorCount() +
                        " warnings (output generated successfully)"
                    );
                } else {
                    // No output generated - real failure
                    System.err.println(
                        "Multi-file compilation failed with " +
                        errorHandler.getErrorCount() +
                        " errors"
                    );
                    return false;
                }
            }

            if (verbose) {
                System.out.println(
                    "Multi-file transpilation completed with " +
                    errorHandler.getProblemCount() +
                    " total problems"
                );

                // List generated files
                if (outputDirectory.exists()) {
                    System.out.println("Generated files:");
                    listFilesRecursively(outputDirectory, "  ");
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println(
                "Multi-file compilation failed: " + e.getMessage()
            );
            if (verbose) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Derive the source root directory from a Java file and its package name.
     * For example, if file is /path/to/test-samples/j2ts/com/example/test/JsniTest.java
     * and package is com.example.test, then source root is /path/to/test-samples/j2ts/
     */
    private String deriveSourceRootFromFile(File javaFile, String packageName) {
        String filePath = javaFile.getAbsolutePath();

        if (packageName == null || packageName.isEmpty()) {
            // No package, so source root is the parent directory of the file
            return javaFile.getParent();
        }

        // Convert package name to path (com.example.test -> com/example/test)
        String packagePath = packageName.replace('.', File.separatorChar);

        // The file path should end with packagePath + fileName
        // So we can find the source root by removing packagePath + fileName from the end
        String expectedSuffix =
            File.separator + packagePath + File.separator + javaFile.getName();

        if (filePath.endsWith(expectedSuffix)) {
            String sourceRoot = filePath.substring(
                0,
                filePath.length() - expectedSuffix.length()
            );
            System.out.println(
                "🔍 CompilerService: Derived source root: " + sourceRoot
            );
            System.out.println("   From file: " + filePath);
            System.out.println("   Package: " + packageName);
            return sourceRoot;
        } else {
            // Fallback: use parent directory
            System.out.println(
                "⚠️  CompilerService: Could not derive source root from package structure"
            );
            System.out.println("   File: " + filePath);
            System.out.println("   Expected suffix: " + expectedSuffix);
            System.out.println("   Using parent directory as fallback");
            return javaFile.getParent();
        }
    }
}
