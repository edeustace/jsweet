package org.jsweet.cli.extension;

import org.jsweet.cli.service.JSNIProcessor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        "(?:public|private|protected)?\\s+(?:static\\s+)?(?:native\\s+)(?:\\w+(?:<[^>]*>)?\\s+)(\\w+)\\s*\\([^)]*\\)\\s*/\\*-\\{([\\s\\S]*?)\\}-\\*/",
        Pattern.DOTALL | Pattern.MULTILINE
    );

    private String currentSourceContent = null;
    private String currentSourceClassName = null;
    private final JSNIProcessor jsniProcessor;
    private final String sourceRootPath;

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
            "🏁 JSNIAdapter: Transpilation finished - JSNI processing complete"
        );
        super.onTranspilationFinished();
    }
}
