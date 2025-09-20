package org.jsweet.cli.extension;

import org.jsweet.cli.service.JSNIProcessor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.jsweet.transpiler.JSweetContext;
import org.jsweet.transpiler.extension.PrinterAdapter;

/**
 * Custom JSweet adapter to support JSNI (JavaScript Native Interface) syntax.
 *
 * This adapter intercepts native method compilation and injects JavaScript
 * code from JSNI blocks into the generated TypeScript.
 *
 * Based on the SweetHome3D adapter pattern and JSweet PrinterAdapter framework.
 */
public class JSNIAdapter extends PrinterAdapter {

    // Pattern to match JSNI blocks in Java source
    private static final Pattern JSNI_PATTERN = Pattern.compile(
        "/\\*-\\{([\\s\\S]*?)\\}-\\*/",
        Pattern.DOTALL
    );

    // Pattern to match method signatures with JSNI blocks
    private static final Pattern METHOD_JSNI_PATTERN = Pattern.compile(
        "(?:public|private|protected)?\\s+(?:static\\s+)?(?:native\\s+)(?:\\w+(?:<[^>]*>)?\\s+)(\\w+)\\s*\\(([^)]*)\\)\\s*/\\*-\\{([\\s\\S]*?)\\}-\\*/",
        Pattern.DOTALL | Pattern.MULTILINE
    );

    // Pattern to match JSNI placeholders in generated TypeScript
    private static final Pattern JSNI_PLACEHOLDER_PATTERN = Pattern.compile(
        "//\\s*JSNI_METHOD:(\\w+):(-?\\d+)",
        Pattern.MULTILINE
    );

    private String currentSourceContent = null;
    private String currentSourceClassName = null;
    private final JSNIProcessor jsniProcessor;
    private final String sourceRootPath;

    // Map to cache JSNI methods: className -> {methodName:paramHash -> jsniBody}
    private final Map<String, Map<String, String>> jsniMethodCache = new HashMap<>();

    public JSNIAdapter(PrinterAdapter parentAdapter, String sourceRootPath) {
        super(parentAdapter);
        this.jsniProcessor = new JSNIProcessor();
        this.sourceRootPath = sourceRootPath;
        System.out.println(
            "🔧 JSNIAdapter: Initialized for JSNI support with JSNIProcessor"
        );
        System.out.println("   Source root path: " + sourceRootPath);
    }

    @Override
    public boolean substituteMethodBody(
        TypeElement parentTypeElement,
        ExecutableElement method
    ) {
        System.out.println(
            "🔍 JSNIAdapter: substituteMethodBody - Checking method: " +
            method.getSimpleName()
        );

        // Check if this is a native method
        if (method.getModifiers().contains(Modifier.NATIVE)) {
            System.out.println(
                "✅ JSNIAdapter: Found native method in substituteMethodBody: " +
                method.getSimpleName()
            );

            // Try to find JSNI block in the source
            String jsniCode = extractJSNICode(method);
            if (jsniCode != null) {
                System.out.println(
                    "🎯 JSNIAdapter: Found JSNI code for " +
                    method.getSimpleName()
                );
                System.out.println("   Raw JavaScript: " + jsniCode.trim());

                // Process the JSNI code to clean up GWT-style references
                String processedJs = jsniProcessor.processJSNI(jsniCode);
                System.out.println(
                    "   Processed JavaScript: " + processedJs.trim()
                );

                // Inject the processed JavaScript code
                injectJavaScriptCode(processedJs);
                return true;
            } else {
                System.out.println(
                    "⚠️ JSNIAdapter: No JSNI block found for native method: " +
                    method.getSimpleName()
                );
            }
        }

        // Delegate to parent adapter if not handled
        return super.substituteMethodBody(parentTypeElement, method);
    }

    @Override
    public boolean substituteExecutable(ExecutableElement executable) {
        System.out.println(
            "🔍 JSNIAdapter: substituteExecutable - Checking executable: " +
            executable.getSimpleName()
        );

        // Check if this is a native method
        if (executable.getModifiers().contains(Modifier.NATIVE)) {
            System.out.println(
                "✅ JSNIAdapter: Found native executable: " +
                executable.getSimpleName()
            );

            // Try to find JSNI block in the source
            String jsniCode = extractJSNICode(executable);
            if (jsniCode != null) {
                System.out.println(
                    "🎯 JSNIAdapter: Found JSNI code for executable " +
                    executable.getSimpleName()
                );
                System.out.println("   Raw JavaScript: " + jsniCode.trim());

                // Process the JSNI code to clean up GWT-style references
                String processedJs = jsniProcessor.processJSNI(jsniCode);
                System.out.println(
                    "   Processed JavaScript: " + processedJs.trim()
                );

                // Print the method signature and body
                printMethodWithJSNI(executable, processedJs);
                return true;
            }
        }

        // Delegate to parent adapter if not handled
        return super.substituteExecutable(executable);
    }

    /**
     * Load the source file content for JSNI parsing.
     */
    private void loadSourceContent(ExecutableElement method) {
        // Try to get the source file from the enclosing type
        TypeElement enclosingType = (TypeElement) method.getEnclosingElement();
        String className = enclosingType.getQualifiedName().toString();

        // Check if we already loaded this class
        if (
            currentSourceContent != null &&
            className.equals(currentSourceClassName)
        ) {
            return; // Already loaded for this class
        }

        try {
            System.out.println(
                "🔍 JSNIAdapter: Loading source for class: " + className
            );

            // Try different ways to get the source file path
            String sourceFilePath = getSourceFilePath(className);
            if (sourceFilePath != null) {
                Path sourcePath = Paths.get(sourceFilePath);
                if (Files.exists(sourcePath)) {
                    currentSourceContent = Files.readString(sourcePath);
                    currentSourceClassName = className; // Remember which class this content is for
                    System.out.println(
                        "✅ JSNIAdapter: Loaded source file: " + sourceFilePath
                    );
                    System.out.println(
                        "   File size: " +
                        currentSourceContent.length() +
                        " characters"
                    );
                } else {
                    System.out.println(
                        "⚠️ JSNIAdapter: Source file not found: " +
                        sourceFilePath
                    );
                }
            }
        } catch (IOException e) {
            System.out.println(
                "❌ JSNIAdapter: Error loading source file: " + e.getMessage()
            );
        }
    }

    /**
     * Determine the source file path from the class name using the source root directory.
     * The file should be located at sourceRoot + packagePath + ClassName.java
     */
    private String getSourceFilePath(String className) {
        if (sourceRootPath == null || sourceRootPath.isEmpty()) {
            System.out.println(
                "❌ JSNIAdapter: No source root path configured"
            );
            return null;
        }

        // Convert class name to path (e.g., com.example.test.JsniTest -> com/example/test/JsniTest.java)
        String classPath = className.replace('.', '/') + ".java";

        // Construct the expected path using source root + package path
        Path expectedPath = Paths.get(sourceRootPath, classPath);

        System.out.println(
            "🔍 JSNIAdapter: Looking for source file at: " +
            expectedPath.toAbsolutePath()
        );

        if (Files.exists(expectedPath)) {
            System.out.println(
                "✅ JSNIAdapter: Found source at: " + expectedPath
            );
            return expectedPath.toAbsolutePath().toString();
        }

        System.out.println(
            "❌ JSNIAdapter: Source file not found for class: " +
            className +
            " at expected path: " +
            expectedPath.toAbsolutePath()
        );
        return null;
    }

    /**
     * Extract JavaScript code from JSNI block in method source.
     * This implementation actually parses the Java source file.
     */
    private String extractJSNICode(ExecutableElement method) {
        String methodName = method.getSimpleName().toString();
        System.out.println(
            "🔍 JSNIAdapter: Extracting JSNI for method: " + methodName
        );

        // Load source content if not already loaded
        loadSourceContent(method);

        if (currentSourceContent == null) {
            System.out.println(
                "❌ JSNIAdapter: No source content available for JSNI extraction"
            );
            return null;
        }

        // Use regex to find method with JSNI block
        Matcher matcher = METHOD_JSNI_PATTERN.matcher(currentSourceContent);

        while (matcher.find()) {
            String foundMethodName = matcher.group(1);
            String jsniCode = matcher.group(2);

            System.out.println(
                "🔍 JSNIAdapter: Found JSNI method: " + foundMethodName
            );

            if (foundMethodName.equals(methodName)) {
                System.out.println(
                    "✅ JSNIAdapter: Matched method " +
                    methodName +
                    " with JSNI block"
                );
                System.out.println(
                    "   JSNI code length: " + jsniCode.length() + " characters"
                );

                // Clean up the JSNI code (remove extra whitespace, preserve structure)
                String cleanCode = jsniCode.trim();
                return cleanCode;
            }
        }

        System.out.println(
            "❌ JSNIAdapter: No JSNI block found for method: " + methodName
        );
        return null;
    }

    /**
     * Inject JavaScript code into the generated TypeScript method.
     */
    private void injectJavaScriptCode(String jsCode) {
        System.out.println(
            "💉 JSNIAdapter: Injecting processed JavaScript code: " + jsCode
        );

        // Print opening brace
        print(" {").println();

        // Increase indentation and print the JavaScript code
        printIndent().print(jsCode);

        // If the code doesn't end with semicolon, add one
        if (!jsCode.trim().endsWith(";")) {
            print(";");
        }

        println();

        // Print closing brace
        printIndent().print("}");

        System.out.println("✅ JSNIAdapter: JavaScript injection complete");
    }

    /**
     * Print a complete method with JSNI JavaScript code.
     */
    private void printMethodWithJSNI(
        ExecutableElement method,
        String processedJs
    ) {
        System.out.println(
            "🖨️ JSNIAdapter: Printing method with processed JSNI: " +
            method.getSimpleName()
        );

        // Build the method signature with proper modifiers and parameters
        StringBuilder signature = new StringBuilder();

        // Add visibility modifier (assume public if not specified)
        signature.append("public ");

        // Add static modifier if present
        if (method.getModifiers().contains(Modifier.STATIC)) {
            signature.append("static ");
        }

        // Add method name
        signature.append(method.getSimpleName().toString());

        // Add parameters
        signature.append("(");
        boolean firstParam = true;
        for (var param : method.getParameters()) {
            if (!firstParam) {
                signature.append(", ");
            }
            firstParam = false;

            // Convert Java type to TypeScript type
            String tsType = convertJavaTypeToTypeScript(
                param.asType().toString()
            );
            signature.append(param.getSimpleName()).append(": ").append(tsType);
        }
        signature.append(")");

        // Add return type
        String returnType = convertJavaTypeToTypeScript(
            method.getReturnType().toString()
        );
        if (!"void".equals(returnType)) {
            signature.append(": ").append(returnType);
        }

        System.out.println(
            "🔧 JSNIAdapter: Generated signature: " + signature.toString()
        );

        // Print the complete method signature
        printIndent().print(signature.toString());

        // Inject the processed JavaScript code as the method body
        injectJavaScriptCode(processedJs);

        System.out.println("✅ JSNIAdapter: Method printing complete");
    }

    /**
     * Convert Java types to TypeScript types for method signatures.
     */
    private String convertJavaTypeToTypeScript(String javaType) {
        // Handle basic type conversions
        switch (javaType) {
            case "java.lang.String":
            case "String":
                return "string";
            case "int":
            case "java.lang.Integer":
            case "Integer":
            case "double":
            case "java.lang.Double":
            case "Double":
            case "float":
            case "java.lang.Float":
            case "Float":
            case "long":
            case "java.lang.Long":
            case "Long":
                return "number";
            case "boolean":
            case "java.lang.Boolean":
            case "Boolean":
                return "boolean";
            case "void":
                return "void";
            default:
                // Handle generic types like HashMap<String, String>
                if (javaType.contains("<") && javaType.contains(">")) {
                    int startIndex = javaType.indexOf('<');
                    int endIndex = javaType.lastIndexOf('>');

                    // Ensure we have valid indices
                    if (startIndex < endIndex) {
                        // Extract the base type and generic parameters
                        String baseType = javaType.substring(0, startIndex);
                        String genericPart = javaType.substring(
                            startIndex + 1,
                            endIndex
                        );

                        // Convert base type
                        String convertedBase = convertBaseTypeToTypeScript(
                            baseType
                        );

                        // Convert generic parameters
                        String[] genericTypes = genericPart.split(",");
                        StringBuilder convertedGenerics = new StringBuilder();
                        for (int i = 0; i < genericTypes.length; i++) {
                            if (i > 0) convertedGenerics.append(", ");
                            convertedGenerics.append(
                                convertJavaTypeToTypeScript(
                                    genericTypes[i].trim()
                                )
                            );
                        }

                        return (
                            convertedBase +
                            "<" +
                            convertedGenerics.toString() +
                            ">"
                        );
                    }
                }

                // For custom classes, use the simple class name
                String[] parts = javaType.split("\\.");
                return parts[parts.length - 1];
        }
    }

    /**
     * Convert Java base types (without generics) to TypeScript types.
     */
    private String convertBaseTypeToTypeScript(String baseType) {
        switch (baseType) {
            case "java.util.HashMap":
            case "HashMap":
                return "Map";
            case "java.util.ArrayList":
            case "ArrayList":
            case "java.util.List":
            case "List":
                return "Array";
            case "java.util.Set":
            case "Set":
                return "Set";
            default:
                // For custom classes, use the simple class name
                String[] parts = baseType.split("\\.");
                return parts[parts.length - 1];
        }
    }

    @Override
    public void onTranspilationStarted() {
        System.out.println(
            "🚀 JSNIAdapter: Transpilation started - JSNI processing enabled"
        );
        super.onTranspilationStarted();
    }

    @Override
    public void onTranspilationFinished() {
        System.out.println(
            "🏁 JSNIAdapter: Transpilation finished - Starting JSNI post-processing"
        );

        // Post-process generated TypeScript files to replace JSNI placeholders
        processGeneratedTypeScriptFiles();

        super.onTranspilationFinished();

        System.out.println(
            "✅ JSNIAdapter: JSNI post-processing complete"
        );
    }

    /**
     * Post-process generated TypeScript files to replace JSNI placeholders with actual JavaScript code.
     */
    private void processGeneratedTypeScriptFiles() {
        System.out.println("🔄 JSNIAdapter: Starting TypeScript post-processing for JSNI placeholders");

        try {
            // Get the current transpiler context to find the output directory
            String outputPath = getTranspilerOutputPath();
            if (outputPath == null) {
                System.out.println("❌ JSNIAdapter: Cannot determine TypeScript output directory");
                return;
            }

            Path outputDir = Paths.get(outputPath);
            System.out.println("📁 JSNIAdapter: Scanning output directory: " + outputDir);

            // Find all .ts files in the output directory and subdirectories
            try (Stream<Path> tsFiles = Files.walk(outputDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".ts"))) {

                tsFiles.forEach(this::processTypeScriptFile);
            }

        } catch (IOException e) {
            System.out.println("❌ JSNIAdapter: Error during post-processing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Process a single TypeScript file to replace JSNI placeholders.
     */
    private void processTypeScriptFile(Path tsFile) {
        try {
            System.out.println("🔍 JSNIAdapter: Processing TypeScript file: " + tsFile);

            String content = Files.readString(tsFile);
            Matcher placeholderMatcher = JSNI_PLACEHOLDER_PATTERN.matcher(content);

            StringBuilder modifiedContent = new StringBuilder();
            int lastEnd = 0;
            boolean hasChanges = false;

            while (placeholderMatcher.find()) {
                String methodName = placeholderMatcher.group(1);
                String paramHash = placeholderMatcher.group(2);

                System.out.println("🎯 JSNIAdapter: Found JSNI placeholder: " + methodName + ":" + paramHash);

                // Find the corresponding JSNI body
                String jsniBody = findJsniBody(tsFile, methodName, paramHash);

                if (jsniBody != null) {
                    System.out.println("✅ JSNIAdapter: Found JSNI body for " + methodName);

                    // Add everything before this placeholder
                    modifiedContent.append(content, lastEnd, placeholderMatcher.start());

                    // Replace the placeholder with the actual JSNI body
                    modifiedContent.append(processJsniBodyForTypeScript(jsniBody));

                    lastEnd = placeholderMatcher.end();
                    hasChanges = true;
                } else {
                    System.out.println("⚠️ JSNIAdapter: No JSNI body found for " + methodName + ":" + paramHash);
                }
            }

            if (hasChanges) {
                // Add the remaining content after the last replacement
                modifiedContent.append(content, lastEnd, content.length());

                // Write the modified content back to the file
                Files.writeString(tsFile, modifiedContent.toString());
                System.out.println("✅ JSNIAdapter: Updated TypeScript file with JSNI bodies: " + tsFile);
            } else {
                System.out.println("ℹ️ JSNIAdapter: No JSNI placeholders found in: " + tsFile);
            }

        } catch (IOException e) {
            System.out.println("❌ JSNIAdapter: Error processing TypeScript file " + tsFile + ": " + e.getMessage());
        }
    }

    /**
     * Find the JSNI body for a given method name and parameter hash.
     */
    private String findJsniBody(Path tsFile, String methodName, String paramHash) {
        // Determine the Java source file corresponding to this TypeScript file
        String className = getClassNameFromTsFile(tsFile);
        if (className == null) {
            System.out.println("❌ JSNIAdapter: Cannot determine class name for: " + tsFile);
            return null;
        }

        // Load JSNI methods from the source if not already cached
        if (!jsniMethodCache.containsKey(className)) {
            loadJsniMethodsFromSource(className);
        }

        Map<String, String> classMethods = jsniMethodCache.get(className);
        if (classMethods == null) {
            return null;
        }

        String methodKey = methodName + ":" + paramHash;
        return classMethods.get(methodKey);
    }

    /**
     * Load all JSNI methods from a Java source file and cache them.
     */
    private void loadJsniMethodsFromSource(String className) {
        System.out.println("📥 JSNIAdapter: Loading JSNI methods from source for: " + className);

        try {
            String sourceFilePath = getSourceFilePath(className);
            if (sourceFilePath == null || !Files.exists(Paths.get(sourceFilePath))) {
                System.out.println("❌ JSNIAdapter: Source file not found for: " + className);
                return;
            }

            String sourceContent = Files.readString(Paths.get(sourceFilePath));
            Map<String, String> methods = new HashMap<>();

            Matcher methodMatcher = METHOD_JSNI_PATTERN.matcher(sourceContent);
            while (methodMatcher.find()) {
                String methodName = methodMatcher.group(1);
                String parameters = methodMatcher.group(2);
                String jsniBody = methodMatcher.group(3);

                // Generate the same hash as the transpiler
                String paramHash = generateParameterHash(parameters);
                String methodKey = methodName + ":" + paramHash;

                methods.put(methodKey, jsniBody.trim());
                System.out.println("📝 JSNIAdapter: Cached JSNI method: " + methodKey);
            }

            jsniMethodCache.put(className, methods);
            System.out.println("✅ JSNIAdapter: Cached " + methods.size() + " JSNI methods for " + className);

        } catch (IOException e) {
            System.out.println("❌ JSNIAdapter: Error loading JSNI methods for " + className + ": " + e.getMessage());
        }
    }

    /**
     * Generate parameter hash using the same logic as the transpiler.
     */
    private String generateParameterHash(String parameters) {
        StringBuilder paramSignature = new StringBuilder();
        if (parameters != null && !parameters.trim().isEmpty()) {
            String[] params = parameters.split(",");
            for (String param : params) {
                String[] parts = param.trim().split("\\s+");
                if (parts.length >= 2) {
                    // Use the type (first part) for the signature
                    paramSignature.append(parts[0]).append(";");
                }
            }
        }
        return String.valueOf(paramSignature.toString().hashCode());
    }

    /**
     * Extract class name from TypeScript file path.
     * Converts path like "com/example/MyClass.ts" to "com.example.MyClass"
     */
    private String getClassNameFromTsFile(Path tsFile) {
        try {
            String fileName = tsFile.getFileName().toString();
            if (!fileName.endsWith(".ts")) {
                return null;
            }

            String className = fileName.substring(0, fileName.length() - 3);

            // Get the package path
            Path parent = tsFile.getParent();
            if (parent != null) {
                String packagePath = parent.toString().replace('/', '.').replace('\\', '.');
                // Remove any leading path components that aren't part of the package
                if (packagePath.contains("source.")) {
                    int sourceIndex = packagePath.indexOf("source.");
                    packagePath = packagePath.substring(sourceIndex);
                }
                return packagePath + "." + className;
            }

            return className;
        } catch (Exception e) {
            System.out.println("❌ JSNIAdapter: Error extracting class name from: " + tsFile + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Process JSNI body for insertion into TypeScript.
     */
    private String processJsniBodyForTypeScript(String jsniBody) {
        // Clean up and process the JSNI body using the existing processor
        String processedJs = jsniProcessor.processJSNI(jsniBody);

        // Format for TypeScript insertion
        StringBuilder result = new StringBuilder();
        String[] lines = processedJs.split("\n");

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                result.append(line.trim());
                if (!line.trim().endsWith(";")) {
                    result.append(";");
                }
                result.append("\n        ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Get the TypeScript output path from the transpiler context.
     */
    private String getTranspilerOutputPath() {
        // TODO: Find the correct way to access output directory from JSweetContext
        System.out.println("🔍 JSNIAdapter: Searching for TypeScript output directory...");

        // Fallback: try common output directories based on working directory
        String[] possiblePaths = {
            "tempOut",
            "target/ts",
            "out",
            "build/ts"
        };

        for (String path : possiblePaths) {
            Path outputPath = Paths.get(path);
            if (Files.exists(outputPath) && Files.isDirectory(outputPath)) {
                System.out.println("✅ JSNIAdapter: Using fallback output directory: " + outputPath.toAbsolutePath());
                return outputPath.toAbsolutePath().toString();
            }
        }

        System.out.println("❌ JSNIAdapter: Cannot determine TypeScript output directory");
        return null;
    }
}
