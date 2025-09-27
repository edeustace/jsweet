package org.jsweet.test.transpiler;

import org.junit.Assert;
import org.junit.Test;
import org.jsweet.transpiler.Java2TypeScriptTranslator;

/**
 * Tests for JSNI (JavaScript Native Interface) method detection.
 * Tests multiline method signatures and various JSNI patterns.
 */
public class JsniDetectionTest {

    @Test
    public void testMultilineJsniDetection_attachListeners() {
        // Test case from ScriptInjector.java - multiline method signature
        String sourceCode = 
            "  /**\n" +
            "   * fire in parallel with {@code \"loading\"}.\n" +
            "   * \n" +
            "   * \n" +
            "   * @param scriptElement element to which the event handlers will be attached\n" +
            "   * @param callback callback that runs when the script is loaded and parsed.\n" +
            "   */\n" +
            "  private static native void attachListeners(JavaScriptObject scriptElement,\n" +
            "      Callback<Void, Exception> callback, boolean removeTag) /*-{\n" +
            "    function clearCallbacks() {\n" +
            "      scriptElement.onerror = scriptElement.onreadystatechange = scriptElement.onload = null;\n" +
            "      if (removeTag) {\n" +
            "        @com.google.gwt.core.client.ScriptInjector::nativeRemove(Lcom/google/gwt/core/client/JavaScriptObject;)(scriptElement);\n" +
            "      }\n" +
            "    }\n" +
            "    scriptElement.onload = $entry(function() {\n" +
            "      clearCallbacks();\n" +
            "      if (callback) {\n" +
            "        callback.@com.google.gwt.core.client.Callback::onSuccess(Ljava/lang/Object;)(null);\n" +
            "      }\n" +
            "    });\n" +
            "  }-*/;\n";

        boolean isJsni = Java2TypeScriptTranslator.detectJsniMethodInSource(sourceCode, "attachListeners");
        Assert.assertTrue("Should detect attachListeners as JSNI method", isJsni);
    }

    @Test
    public void testSingleLineJsniDetection() {
        // Test simple single-line JSNI method
        String sourceCode = 
            "  public static native JavaScriptObject createArray() /*-{\n" +
            "    return [];\n" +
            "  }-*/;\n";

        boolean isJsni = Java2TypeScriptTranslator.detectJsniMethodInSource(sourceCode, "createArray");
        Assert.assertTrue("Should detect createArray as JSNI method", isJsni);
    }

    @Test
    public void testMultilineJsniDetection_XMLParser() {
        // Test case from XMLParserImplStandard - method name on different line than native
        String sourceCode = 
            "  @Override\n" +
            "  protected native JavaScriptObject getElementByIdImpl(\n" +
            "      JavaScriptObject document, String id) /*-{\n" +
            "    return document.getElementById(id);\n" +
            "  }-*/;\n";

        boolean isJsni = Java2TypeScriptTranslator.detectJsniMethodInSource(sourceCode, "getElementByIdImpl");
        Assert.assertTrue("Should detect getElementByIdImpl as JSNI method", isJsni);
    }

    @Test
    public void testNonJsniNativeMethod() {
        // Test native method without JSNI (abstract native method)
        String sourceCode = 
            "  public abstract class Test {\n" +
            "    public native void someMethod();\n" +
            "  }\n";

        boolean isJsni = Java2TypeScriptTranslator.detectJsniMethodInSource(sourceCode, "someMethod");
        Assert.assertFalse("Should NOT detect someMethod as JSNI method (no /*-{ marker)", isJsni);
    }

    @Test
    public void testRegularMethod() {
        // Test regular non-native method
        String sourceCode = 
            "  public void regularMethod() {\n" +
            "    System.out.println(\"Hello\");\n" +
            "  }\n";

        boolean isJsni = Java2TypeScriptTranslator.detectJsniMethodInSource(sourceCode, "regularMethod");
        Assert.assertFalse("Should NOT detect regularMethod as JSNI method (not native)", isJsni);
    }

    @Test
    public void testJsniWithComplexSignature() {
        // Test JSNI with complex generic signature spanning multiple lines
        String sourceCode = 
            "  private static native <T extends JavaScriptObject> void complexMethod(\n" +
            "      T element,\n" +
            "      Callback<List<String>, Exception> callback,\n" +
            "      Map<String, Object> options) /*-{\n" +
            "    // JavaScript implementation\n" +
            "    console.log('Complex JSNI');\n" +
            "  }-*/;\n";

        boolean isJsni = Java2TypeScriptTranslator.detectJsniMethodInSource(sourceCode, "complexMethod");
        Assert.assertTrue("Should detect complexMethod as JSNI method", isJsni);
    }

    @Test
    public void testMultipleMethodsInSource() {
        // Test source with multiple methods, only one JSNI
        String sourceCode = 
            "  public void normalMethod() {\n" +
            "    doSomething();\n" +
            "  }\n" +
            "\n" +
            "  private static native void jsniMethod() /*-{\n" +
            "    alert('JSNI');\n" +
            "  }-*/;\n" +
            "\n" +
            "  public void anotherMethod() {\n" +
            "    doSomethingElse();\n" +
            "  }\n";

        Assert.assertFalse("normalMethod should NOT be JSNI", 
            Java2TypeScriptTranslator.detectJsniMethodInSource(sourceCode, "normalMethod"));
        Assert.assertTrue("jsniMethod should be JSNI", 
            Java2TypeScriptTranslator.detectJsniMethodInSource(sourceCode, "jsniMethod"));
        Assert.assertFalse("anotherMethod should NOT be JSNI", 
            Java2TypeScriptTranslator.detectJsniMethodInSource(sourceCode, "anotherMethod"));
    }
}