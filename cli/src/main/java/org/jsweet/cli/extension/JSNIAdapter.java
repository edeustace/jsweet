package org.jsweet.cli.extension;

import org.jsweet.cli.service.JSNIProcessor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import standalone.com.sun.source.tree.CompilationUnitTree;
import org.jsweet.transpiler.extension.PrinterAdapter;

/**
 * Custom JSweet adapter to support JSNI (JavaScript Native Interface) syntax.
 *
 * Processes JSNI comment blocks in generated TypeScript files and replaces them
 * with actual JavaScript code on a per-type basis.
 */
public class JSNIAdapter extends PrinterAdapter {

    // Pattern to match JSNI comment blocks in generated TypeScript
    private static final Pattern JSNI_COMMENT_BLOCK_PATTERN = Pattern.compile(
        "/\\*\\s*JSNI_METHOD_BEGIN\\s*\\n([\\s\\S]*?)\\n\\s*JSNI_METHOD_END\\s*\\*/",
        Pattern.MULTILINE | Pattern.DOTALL
    );

    private final JSNIProcessor jsniProcessor;

    public JSNIAdapter(PrinterAdapter parentAdapter, String sourceRootPath) {
        super(parentAdapter);
        this.jsniProcessor = new JSNIProcessor();
        logger.debug("🔧 JSNIAdapter: Initialized for JSNI post-processing");
    }

    @Override
    public boolean substituteMethodBody(TypeElement parentTypeElement, ExecutableElement method) {
        // Let the transpiler handle JSNI method conversion at AST level
        if (method.getModifiers().contains(Modifier.NATIVE)) {
            logger.debug("🔄 JSNIAdapter: Found native method, letting transpiler handle conversion: " + method.getSimpleName());
        }
        return super.substituteMethodBody(parentTypeElement, method);
    }

    @Override
    public boolean substituteExecutable(ExecutableElement executable) {
        // Let the transpiler handle JSNI method conversion at AST level
        if (executable.getModifiers().contains(Modifier.NATIVE)) {
            logger.debug("🔄 JSNIAdapter: Found native method, letting transpiler handle conversion: " + executable.getSimpleName());
        }
        return super.substituteExecutable(executable);
    }

    @Override
    public String onBeforeWriteType(CompilationUnitTree compilationUnit, String tsContent) {
        // Process JSNI comment blocks before writing to file
        if (tsContent.contains("JSNI_METHOD_BEGIN")) {
            logger.debug("🔍 JSNIAdapter: Processing JSNI comment blocks for compilation unit");
            String processedContent = processJsniCommentBlocks(tsContent);
            if (!tsContent.equals(processedContent)) {
                logger.debug("✅ JSNIAdapter: Processed JSNI methods");
                return processedContent;
            }
        }

        return super.onBeforeWriteType(compilationUnit, tsContent);
    }

    /**
     * Process JSNI comment blocks in TypeScript content.
     */
    private String processJsniCommentBlocks(String content) {
        Matcher jsniBlockMatcher = JSNI_COMMENT_BLOCK_PATTERN.matcher(content);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;

        while (jsniBlockMatcher.find()) {
            String jsniBody = jsniBlockMatcher.group(1).trim();

            // Add everything before this JSNI block
            result.append(content, lastEnd, jsniBlockMatcher.start());

            // Replace with processed JavaScript
            String processedJs = processJsniBodyForTypeScript(jsniBody);
            result.append(processedJs);

            lastEnd = jsniBlockMatcher.end();
        }

        // Add remaining content
        result.append(content, lastEnd, content.length());
        return result.toString();
    }

    /**
     * Process JSNI body for direct insertion into TypeScript method.
     */
    private String processJsniBodyForTypeScript(String jsniBody) {
        // Process the JSNI body to convert GWT-style references to TypeScript
        String processedJs = jsniProcessor.processJSNI(jsniBody);

        // Return the processed JavaScript as-is (multiline formatting is preserved)
        return processedJs;
    }

    @Override
    public void onTranspilationStarted() {
        System.out.println("🚀 JSNIAdapter: Transpilation started - JSNI processing enabled");
        super.onTranspilationStarted();
    }

}
