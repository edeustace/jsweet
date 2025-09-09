package org.jsweet.cli.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes JSNI (JavaScript Native Interface) code blocks to convert GWT-style
 * Java references into clean TypeScript/JavaScript code.
 *
 * Transforms patterns like:
 * - @JSNIExample::new(Ljava/lang/String;)(value) → new JSNIExample(value)
 * - this.@JSNIExample::instanceFoo(Ljava/lang/String;)(s) → this.instanceFoo(s)
 * - @JSNIExample::staticFoo(Ljava/lang/String;)(s) → JSNIExample.staticFoo(s)
 * - this.@JSNIExample::myInstanceField → this.myInstanceField
 * - @JSNIExample::myStaticField → JSNIExample.myStaticField
 */
public class JSNIProcessor {

    // Pattern for instance method calls: this.@package.ClassName::methodName(signature)(args)
    private static final Pattern INSTANCE_METHOD_PATTERN = Pattern.compile(
        "(\\w+)\\.@([\\w.]+)::(\\w+)\\([^)]*\\)\\(([^)]*)\\)",
        Pattern.MULTILINE
    );

    // Pattern for static method calls: @package.ClassName::methodName(signature)(args)
    private static final Pattern STATIC_METHOD_PATTERN = Pattern.compile(
        "@([\\w.]+)::(\\w+)\\([^)]*\\)\\(([^)]*)\\)",
        Pattern.MULTILINE
    );

    // Pattern for instance field access: obj.@package.ClassName::fieldName
    private static final Pattern INSTANCE_FIELD_PATTERN = Pattern.compile(
        "(\\w+)\\.@([\\w.]+)::(\\w+)",
        Pattern.MULTILINE
    );

    // Pattern for static field access: @package.ClassName::fieldName
    private static final Pattern STATIC_FIELD_PATTERN = Pattern.compile(
        "@([\\w.]+)::(\\w+)",
        Pattern.MULTILINE
    );

    // Pattern for constructor calls: @package.ClassName::new(signature)(args)
    private static final Pattern CONSTRUCTOR_PATTERN = Pattern.compile(
        "@([\\w.]+)::new\\([^)]*\\)\\(([^)]*)\\)",
        Pattern.MULTILINE
    );

    /**
     * Process JSNI code block and convert GWT-style references to clean JavaScript.
     *
     * @param jsniCode Raw JSNI JavaScript code with GWT references
     * @return Clean JavaScript code suitable for TypeScript compilation
     */
    public String processJSNI(String jsniCode) {
        if (jsniCode == null || jsniCode.trim().isEmpty()) {
            return jsniCode;
        }

        String processed = jsniCode;

        // Process in order of specificity (most specific patterns first)

        // 1. Constructor calls: @JSNIExample::new(Ljava/lang/String;)(value)
        processed = processConstructorCalls(processed);

        // 2. Instance method calls: this.@JSNIExample::instanceFoo(Ljava/lang/String;)(s)
        processed = processInstanceMethodCalls(processed);

        // 3. Static method calls: @JSNIExample::staticFoo(Ljava/lang/String;)(s)
        processed = processStaticMethodCalls(processed);

        // 4. Instance field access: this.@JSNIExample::myInstanceField
        processed = processInstanceFieldAccess(processed);

        // 5. Static field access: @JSNIExample::myStaticField
        processed = processStaticFieldAccess(processed);

        return processed;
    }

    /**
     * Transform constructor calls from JSNI to JavaScript.
     * @com.example.JsniTest::new(Ljava/lang/String;)(value) → new JsniTest(value)
     */
    private String processConstructorCalls(String code) {
        Matcher matcher = CONSTRUCTOR_PATTERN.matcher(code);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String fullClassName = matcher.group(1); // "com.example.JsniTest"
            String args = matcher.group(2); // "value"

            // Extract simple class name from full package name
            String className = fullClassName.substring(
                fullClassName.lastIndexOf('.') + 1
            );

            // Transform to: new ClassName(args)
            String replacement = "new " + className + "(" + args + ")";
            matcher.appendReplacement(
                result,
                Matcher.quoteReplacement(replacement)
            );
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Transform instance method calls from JSNI to JavaScript.
     * this.@JSNIExample::instanceFoo(Ljava/lang/String;)(s) → this.instanceFoo(s)
     */
    private String processInstanceMethodCalls(String code) {
        Matcher matcher = INSTANCE_METHOD_PATTERN.matcher(code);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String objectRef = matcher.group(1); // "this" or variable name
            String fullClassName = matcher.group(2); // "com.example.JSNIExample"
            String methodName = matcher.group(3); // "instanceFoo"
            String args = matcher.group(4); // "s"

            // Transform to: objectRef.methodName(args)
            String replacement =
                objectRef + "." + methodName + "(" + args + ")";
            matcher.appendReplacement(
                result,
                Matcher.quoteReplacement(replacement)
            );
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Transform static method calls from JSNI to JavaScript.
     * @JSNIExample::staticFoo(Ljava/lang/String;)(s) → JSNIExample.staticFoo(s)
     */
    private String processStaticMethodCalls(String code) {
        Matcher matcher = STATIC_METHOD_PATTERN.matcher(code);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String fullClassName = matcher.group(1); // "com.example.JSNIExample"
            String methodName = matcher.group(2); // "staticFoo"
            String args = matcher.group(3); // "s"

            // Extract simple class name from full package name
            String className = fullClassName.substring(
                fullClassName.lastIndexOf('.') + 1
            );

            // Transform to: ClassName.methodName(args)
            String replacement =
                className + "." + methodName + "(" + args + ")";
            matcher.appendReplacement(
                result,
                Matcher.quoteReplacement(replacement)
            );
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Transform instance field access from JSNI to JavaScript.
     * this.@JSNIExample::myInstanceField → this.myInstanceField
     */
    private String processInstanceFieldAccess(String code) {
        Matcher matcher = INSTANCE_FIELD_PATTERN.matcher(code);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String objectRef = matcher.group(1); // "this" or variable name
            String fullClassName = matcher.group(2); // "com.example.JSNIExample"
            String fieldName = matcher.group(3); // "myInstanceField"

            // Transform to: objectRef.fieldName
            String replacement = objectRef + "." + fieldName;
            matcher.appendReplacement(
                result,
                Matcher.quoteReplacement(replacement)
            );
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Transform static field access from JSNI to JavaScript.
     * @JSNIExample::myStaticField → JSNIExample.myStaticField
     */
    private String processStaticFieldAccess(String code) {
        Matcher matcher = STATIC_FIELD_PATTERN.matcher(code);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String fullClassName = matcher.group(1); // "com.example.JSNIExample"
            String fieldName = matcher.group(2); // "myStaticField"

            // Extract simple class name from full package name
            String className = fullClassName.substring(
                fullClassName.lastIndexOf('.') + 1
            );

            // Transform to: ClassName.fieldName
            String replacement = className + "." + fieldName;
            matcher.appendReplacement(
                result,
                Matcher.quoteReplacement(replacement)
            );
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Check if a string contains JSNI syntax patterns.
     *
     * @param code JavaScript code to check
     * @return true if JSNI patterns are detected
     */
    public boolean containsJSNI(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        return (
            CONSTRUCTOR_PATTERN.matcher(code).find() ||
            INSTANCE_METHOD_PATTERN.matcher(code).find() ||
            STATIC_METHOD_PATTERN.matcher(code).find() ||
            INSTANCE_FIELD_PATTERN.matcher(code).find() ||
            STATIC_FIELD_PATTERN.matcher(code).find()
        );
    }
}
