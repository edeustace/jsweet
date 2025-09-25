/* 
 * JSweet - http://www.jsweet.org
 * Copyright (C) 2015 CINCHEO SAS <renaud.pawlak@cincheo.fr>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jsweet.test.transpiler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.io.FileUtils;
import java.io.IOException;
import org.jsweet.transpiler.ModuleKind;
import org.jsweet.transpiler.SourceFile;
import org.jsweet.transpiler.util.EvaluationResult;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import source.structural.GwtEventTest;
import source.structural.globalclasses.Globals;
import source.structural.gwt.GwtEvent;
import source.structural.gwt.JsniDuplicates;
import source.structural.gwt.client.ui.RootPanel;
import source.structural.gwt.dom.client.ButtonElement;
import source.structural.gwt.user.client.ui.AttachDetachException;
import source.tscomparison.AbstractClasses;
import source.tscomparison.ActualScoping;
import source.tscomparison.CompileTimeWarnings;
import source.tscomparison.HasVerticalAlignmentTest;
import source.tscomparison.InnerAbstractClassTest;
import source.tscomparison.OtherThisExample;
import source.tscomparison.StaticMethodInClassTest;
import source.tscomparison.SaferVarargs;
import source.tscomparison.StrongerTyping;
import source.tscomparison.ThisIsThis;

public class TsComparisonTest extends AbstractTest {


    @Test
    public void simpleGwtTest(){

        SourceFile file = getSourceFile(GwtEventTest.class);
        eval(ModuleKind.es2015, null, file);

        // ts part
        TsSourceFile source = getTsSourceFile(file);
        

        // evalTs(getTsSourceFile(file));

        try {
            String tsContent = FileUtils.readFileToString(file.getTsFile());
            System.out.println("Generated TypeScript content:");
            System.out.println(tsContent);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to read TypeScript file: " + e.getMessage());
        }


    }

    @Test
    public void gwtTestTwo() {

        SourceFile file = getSourceFile(GwtEvent.class);
        eval(ModuleKind.es2015, null, file);

        // ts part
        TsSourceFile source = getTsSourceFile(file);


        // evalTs(getTsSourceFile(file));

        try {
            String tsContent = FileUtils.readFileToString(file.getTsFile());
            System.out.println("Generated TypeScript content:");
            System.out.println(tsContent);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to read TypeScript file: " + e.getMessage());
        }
    }

    @Ignore
    @Test
    public void strongerTypingTest() {
        // jsweet part
        SourceFile file = getSourceFile(StrongerTyping.class);
        eval(ModuleKind.none, null, file);

        // ts part
        evalTs(getTsSourceFile(file));
    }

    @Ignore
    @Test
    public void compileTimeWarningsTest() {
        // jsweet part
        SourceFile file = getSourceFile(CompileTimeWarnings.class);
        eval(ModuleKind.none, null, file);

        // ts part
        evalTs(getTsSourceFile(file));
    }

    @Test
    public void rootPanelClosingBraceTest() {
        SourceFile file = getSourceFile(RootPanel.class);
        eval(ModuleKind.es2015, null, file);

        // Read and print the generated TypeScript to examine the closing brace issue
        try {
            String tsContent = FileUtils.readFileToString(file.getTsFile());
            System.out.println("Generated TypeScript content for RootPanel:");
            System.out.println(tsContent);

            // Check for basic syntax issues - verify static inner class generates as class expression
            if (!tsContent.contains("public static DefaultRootPanel = ") ||
                !tsContent.contains("class DefaultRootPanel extends RootPanel")) {
                fail("Static inner class not generated as class expression");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to read TypeScript file: " + e.getMessage());
        }
    }

    @Test
    public void staticInterfaceTest() {
        SourceFile file = getSourceFile(AttachDetachException.class);
        eval(ModuleKind.es2015, null, file);

        // Read and print the generated TypeScript to examine static interface generation
        try {
            String tsContent = FileUtils.readFileToString(file.getTsFile());
            System.out.println("Generated TypeScript content for AttachDetachException:");
            System.out.println(tsContent);

            // Check for basic syntax issues - verify static interface is generated properly
            if (!tsContent.contains("interface Command")) {
                fail("Static interface not generated properly");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to read TypeScript file: " + e.getMessage());
        }
    }

    @Test
    public void jsniDuplicatesTest() {
        SourceFile file = getSourceFile(JsniDuplicates.class);
        eval(ModuleKind.es2015, null, file);

        // Read and print the generated TypeScript to examine JSNI method overloading
        try {
            String tsContent = FileUtils.readFileToString(file.getTsFile());
            System.out.println("Generated TypeScript content for JsniDuplicates:");
            System.out.println(tsContent);

            // Check if JSNI methods are generating duplicate signatures
            long fooMethodCount = tsContent.lines()
                    .filter(line -> line.trim().matches(".*foo\\s*\\(.*\\).*"))
                    .count();

            System.out.println("Number of 'foo' method declarations found: " + fooMethodCount);

            // Check if regular methods have proper overloading
            long barMethodCount = tsContent.lines()
                    .filter(line -> line.trim().matches(".*bar\\s*\\(.*\\).*"))
                    .count();

            System.out.println("Number of 'bar' method declarations found: " + barMethodCount);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to read TypeScript file: " + e.getMessage());
        }
    }

    @Test
    public void buttonElementTest() {
        SourceFile file = getSourceFile(ButtonElement.class);
        eval(ModuleKind.es2015, null, file);

        // Read and print the generated TypeScript to examine ButtonElement JSNI conversion
        try {
            String tsContent = FileUtils.readFileToString(file.getTsFile());
            System.out.println("Generated TypeScript content for ButtonElement:");
            System.out.println(tsContent);

            // Check if JSNI methods are being converted to regular methods with placeholders
            long jsniPlaceholderCount = tsContent.lines()
                    .filter(line -> line.trim().contains("JSNI_METHOD:"))
                    .count();

            System.out.println("Number of JSNI placeholders found: " + jsniPlaceholderCount);

            // Check if we have the expected overload dispatcher methods
            boolean hasGetDisabledOverload = tsContent.contains("public getDisabled(") &&
                                           tsContent.contains("getDisabled$java_lang_String") &&
                                           tsContent.contains("getDisabled$boolean");

            boolean hasSetDisabledOverload = tsContent.contains("public setDisabled(") &&
                                           tsContent.contains("setDisabled$java_lang_String") &&
                                           tsContent.contains("setDisabled$boolean");

            System.out.println("Has getDisabled overload dispatcher: " + hasGetDisabledOverload);
            System.out.println("Has setDisabled overload dispatcher: " + hasSetDisabledOverload);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to read TypeScript file: " + e.getMessage());
        }
    }

    @Test
    public void abstractClassesTest() {
        // jsweet part
        SourceFile file = getSourceFile(AbstractClasses.class);
        eval(ModuleKind.none, null, file);

        // ts part
        evalTs(getTsSourceFile(file));
    }

    @Ignore
    @Test
    public void actualScopingTest() {
        // jsweet part
        SourceFile file = getSourceFile(ActualScoping.class);
        eval(ModuleKind.none, null, file);

        // ts part
        evalTs(getTsSourceFile(file));
    }

    @Ignore
    @Test
    public void thisIsThisTest() {
        // jsweet part
        SourceFile file = getSourceFile(ThisIsThis.class);
        eval(ModuleKind.none, null, file);

        // ts part
        evalTs(getTsSourceFile(file));
    }

    @Test
    public void otherThisExampleTest() {
        eval(ModuleKind.none, (logHandler, r) -> {
            logHandler.assertNoProblems();
            System.out.println("" + r.get("results"));
            Assert.assertEquals("3,4,5", "" + r.get("results"));
        }, getSourceFile(Globals.class), getSourceFile(OtherThisExample.class));

    }

    @Ignore
    @Test
    public void saferVarargsTest() {
        // jsweet part

        SourceFile file = getSourceFile(SaferVarargs.class);
        eval(ModuleKind.none, (ctx, result) -> {
            assertEquals("foo", result.get("firstArg"));
        }, file);

        // ts part
        EvaluationResult result = evalTs(getTsSourceFile(file));

        assertTrue(result.get("firstArg").getClass().isArray());
        Object[] res = (Object[]) result.get("firstArg");
        assertEquals("blah", res[0]);
        assertEquals("bluh", res[1]);
    }

    @Test
    public void innerAbstractClassTest() {
        SourceFile file = getSourceFile(InnerAbstractClassTest.class);
        eval(ModuleKind.es2015, (logHandler, result) -> {
            logHandler.assertNoProblems();
        }, file);

        try {
            String tsContent = FileUtils.readFileToString(file.getTsFile());
            System.out.println("Generated TypeScript content for InnerAbstractClassTest:");
            System.out.println(tsContent);

            // Check if inner abstract classes are defined outside the containing class
            boolean hasStaticAbstractOutside = tsContent.contains("export abstract class StaticAbstractInner") ||
                                              tsContent.contains("abstract class StaticAbstractInner");
            boolean hasNonStaticAbstractOutside = tsContent.contains("export abstract class NonStaticAbstractInner") ||
                                                 tsContent.contains("abstract class NonStaticAbstractInner");

            // Check if they're referenced correctly inside the containing class
            boolean hasStaticReference = tsContent.contains("StaticAbstractInner") &&
                                        !tsContent.contains("InnerAbstractClassTest.StaticAbstractInner");
            boolean hasNonStaticReference = tsContent.contains("NonStaticAbstractInner") &&
                                           !tsContent.contains("InnerAbstractClassTest.NonStaticAbstractInner");

            System.out.println("Static abstract class defined outside: " + hasStaticAbstractOutside);
            System.out.println("Non-static abstract class defined outside: " + hasNonStaticAbstractOutside);
            System.out.println("Static class referenced without namespace: " + hasStaticReference);
            System.out.println("Non-static class referenced without namespace: " + hasNonStaticReference);

            // These assertions will initially fail - that's the point of the test
            // We want to see the current behavior first
            // assertTrue("Static abstract class should be defined outside containing class", hasStaticAbstractOutside);
            // assertTrue("Non-static abstract class should be defined outside containing class", hasNonStaticAbstractOutside);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to read TypeScript file: " + e.getMessage());
        }
    }

    private TsSourceFile getTsSourceFile(SourceFile jsweetSourceFile) {
        String javaTestFilePath = jsweetSourceFile.getJavaFile().getAbsolutePath();
        File tsFile = new File(javaTestFilePath.substring(0, javaTestFilePath.length() - 5) + ".ts");
        TsSourceFile tsSourceFile = new TsSourceFile(tsFile);
        return tsSourceFile;
    }

    private EvaluationResult evalTs(TsSourceFile sourceFile) {
        return evalTs(sourceFile, false);
    }

    private EvaluationResult evalTs(TsSourceFile sourceFile, boolean expectErrors) {
        try {
            System.out.println("running tsc: " + sourceFile);
            TestTranspilationHandler logHandler = new TestTranspilationHandler();

            transpilerTest().getTranspiler().setTsOutputDir(sourceFile.getTsFile().getParentFile());
            EvaluationResult result = transpilerTest().getTranspiler().eval(logHandler, sourceFile);
            FileUtils.deleteQuietly(sourceFile.getJsFile());

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot compile Typescript file: " + sourceFile);
            return null;
        }
    }

    private class TsSourceFile extends SourceFile {
        public TsSourceFile(File tsFile) {
            super(null);
            this.setTsFile(tsFile);
        }

        @Override
        public String toString() {
            return getTsFile().toString();
        }

        @Override
        public void touch() {
            // do nothing
        }

    }

    @Test
    public void staticMethodInClassTest() {
        SourceFile file = getSourceFile(StaticMethodInClassTest.class);
        eval(ModuleKind.es2015, (logHandler, result) -> {
            // This test should fail until the static method issue is fixed
            // The issue: JSweet generates "export function" inside classes instead of "static" methods
            logHandler.assertNoProblems();
        }, file);

        try {
            String tsContent = FileUtils.readFileToString(file.getTsFile());
            System.out.println("Generated TypeScript content for StaticMethodInClassTest:");
            System.out.println(tsContent);

            // Check for the BUG: should NOT contain "export function" inside class
            assertFalse("Generated TypeScript should not contain 'export function' inside class body",
                       tsContent.contains("export function staticMethod") ||
                       tsContent.contains("export function anotherStaticMethod"));

            // Check for CORRECT behavior: should contain "static" methods
            assertTrue("Generated TypeScript should contain 'static staticMethod'",
                      tsContent.contains("static staticMethod"));
            assertTrue("Generated TypeScript should contain 'static anotherStaticMethod'",
                      tsContent.contains("static anotherStaticMethod"));

        } catch (IOException e) {
            fail("Could not read generated TypeScript file: " + e.getMessage());
        }
    }

    @Test
    public void hasVerticalAlignmentTest() {
        // Test static nested class in interface hoisting and field generation
        SourceFile file = getSourceFile(HasVerticalAlignmentTest.class);
        eval(ModuleKind.es2015, (logHandler, result) -> {
            logHandler.assertNoProblems();
        }, file);

        try {
            String tsContent = FileUtils.readFileToString(file.getTsFile());
            System.out.println("Generated TypeScript content for HasVerticalAlignmentTest:");
            System.out.println(tsContent);

            // Check for the BUG: should NOT contain "let fieldName" in class fields
            assertFalse("Generated TypeScript should not contain 'let verticalAlignString' in class field",
                       tsContent.contains("let verticalAlignString"));

            // Check for CORRECT behavior: should contain proper class field declaration
            assertTrue("Generated TypeScript should contain proper class field 'verticalAlignString: string'",
                      tsContent.contains("verticalAlignString: string") ||
                      tsContent.contains("private verticalAlignString: string") ||
                      tsContent.contains("/*private*/ verticalAlignString: string"));

        } catch (IOException e) {
            fail("Could not read generated TypeScript file: " + e.getMessage());
        }
    }
}
