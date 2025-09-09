package org.jsweet.cli;

import org.jsweet.cli.model.JavaSourceFile;
import org.jsweet.cli.service.CompilerService;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.concurrent.Callable;

@Command(
    name = "jsweet-cli",
    mixinStandardHelpOptions = true,
    version = "0.1.0",
    description = "Convert Java source files to TypeScript using JSweet transpiler"
)
public class Main implements Callable<Integer> {
    
    @Parameters(
        index = "0",
        description = "Java source file or directory to compile"
    )
    private File inputPath;
    
    @Option(
        names = {"-o", "--output"},
        description = "Output directory for generated TypeScript files (default: ./typescript)",
        defaultValue = "./typescript"
    )
    private File outputDirectory;
    
    @Option(
        names = {"--target"},
        description = "TypeScript target version (ES5, ES6, ES2017)",
        defaultValue = "ES5"
    )
    private String target;
    
    @Option(
        names = {"--verbose"},
        description = "Enable verbose output"
    )
    private boolean verbose;
    
    @Option(
        names = {"--excludes"},
        description = "Comma-separated list of glob patterns to exclude from compilation",
        split = ","
    )
    private String[] excludePatterns;
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
    
    @Override
    public Integer call() throws Exception {
        System.out.println("JSweet CLI v0.1.0 - Java to TypeScript Compiler");
        System.out.println("Input path: " + inputPath.getAbsolutePath());
        System.out.println("Output directory: " + outputDirectory.getAbsolutePath());
        System.out.println("Target: " + target);
        
        if (verbose) {
            System.out.println("Verbose mode enabled");
        }
        
        // Validate input path exists
        if (!inputPath.exists()) {
            System.err.println("Error: Input path does not exist: " + inputPath.getAbsolutePath());
            return 1;
        }
        
        // Create output directory if it doesn't exist
        if (!outputDirectory.exists()) {
            if (!outputDirectory.mkdirs()) {
                System.err.println("Error: Could not create output directory: " + outputDirectory.getAbsolutePath());
                return 1;
            }
            System.out.println("Created output directory: " + outputDirectory.getAbsolutePath());
        }
        
        System.out.println("Starting compilation...");
        
        try {
            CompilerService compilerService = new CompilerService();
            boolean success;
            
            if (inputPath.isDirectory()) {
                // Multi-file compilation
                System.out.println("Detected directory - using multi-file compilation");
                success = compilerService.compileDirectory(inputPath.getAbsolutePath(), outputDirectory, target, verbose, excludePatterns);
            } else {
                // Single file compilation
                if (!inputPath.getName().endsWith(".java")) {
                    System.err.println("Error: Input file must have .java extension");
                    return 1;
                }
                
                System.out.println("Detected file - using single-file compilation");
                JavaSourceFile sourceFile = new JavaSourceFile(inputPath.getAbsolutePath());
                
                if (verbose) {
                    System.out.println("Loaded source file: " + sourceFile);
                }
                
                success = compilerService.compileJavaFile(sourceFile, outputDirectory, target, verbose);
            }
            
            if (success) {
                System.out.println("Compilation completed successfully!");
                return 0;
            } else {
                System.err.println("Compilation failed!");
                return 1;
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            if (verbose) {
                e.printStackTrace();
            }
            return 1;
        }
    }
}