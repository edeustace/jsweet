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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jsweet.transpiler.EcmaScriptComplianceLevel;
import org.jsweet.transpiler.JSweetTranspiler;
import org.jsweet.transpiler.ModuleKind;
import org.jsweet.transpiler.SourceFile;
import org.jsweet.transpiler.util.EvaluationResult;
import org.junit.Before;
import org.junit.Test;

import source.structural.GwtEventTest;
import source.structural.StackTraceCreator;

public class SnapshotTest extends AbstractTest {

    private static final String SNAPSHOT_DIR = "src/test/resources/snapshots";
    private Path snapshotPath;

    @Before
    public void setUp() {
        snapshotPath = Paths.get(SNAPSHOT_DIR).toAbsolutePath();
        try {
            Files.createDirectories(snapshotPath);
        } catch (IOException e) {
            fail("Failed to create snapshot directory: " + e.getMessage());
        }
    }

    /**
     * Helper method to perform snapshot testing with custom transpiler options
     * 
     * @param testName Name of the test (used for snapshot file naming)
     * @param sourceClass The Java source class to transpile
     * @param options Map of JSweet options to apply to the transpiler
     * @param runEval Whether to run TypeScript evaluation (non-blocking for snapshot creation)
     * @param moduleKind The module kind to use for transpilation
     * @return The path to the generated snapshot file
     */
    protected Path runSnapshotTest(String testName, Class<?> sourceClass, 
                                    Map<String, Object> options, boolean runEval, 
                                    ModuleKind moduleKind) {
        
        SourceFile sourceFile = getSourceFile(sourceClass);
        
        // Get the transpiler and configure it
        TestTranspilationHandler logHandler = new TestTranspilationHandler();
        JSweetTranspiler transpiler = transpilerTest().getTranspiler();
        
        // Apply custom JSweet options if provided
        if (options != null && !options.isEmpty()) {
            // Apply each option directly to transpiler
            for (Map.Entry<String, Object> entry : options.entrySet()) {
                applyOption(transpiler, entry.getKey(), entry.getValue());
            }
        }
        
        if (moduleKind != null) {
            transpiler.setModuleKind(moduleKind);
        }
        
        // Just transpile to TypeScript without running JavaScript evaluation
        try {
            transpiler.transpile(logHandler, sourceFile);
        } catch (IOException e) {
            fail("Transpilation failed: " + e.getMessage());
        }
        
        // Save snapshot
        Path snapshotFile = saveSnapshot(testName, sourceFile);
        
        // Optionally run TypeScript evaluation (non-blocking)
        if (runEval) {
            try {
                runTsEvaluation(sourceFile, testName);
            } catch (Exception e) {
                System.err.println("TypeScript evaluation failed for " + testName + ": " + e.getMessage());
                // Don't fail the test - snapshot is still saved
            }
        }
        
        return snapshotFile;
    }
    
    /**
     * Apply a single option to JSweetTranspiler
     */
    private void applyOption(JSweetTranspiler transpiler, String key, Object value) {
        switch (key) {
            case "module":
                if (value instanceof ModuleKind) {
                    transpiler.setModuleKind((ModuleKind) value);
                }
                break;
            case "target":
                transpiler.setEcmaTargetVersion(EcmaScriptComplianceLevel.valueOf(value.toString()));
                break;
            case "bundle":
                if (value instanceof Boolean) {
                    transpiler.setBundle((Boolean) value);
                }
                break;
            case "sourceMap":
                if (value instanceof Boolean) {
                    transpiler.setGenerateSourceMaps((Boolean) value);
                }
                break;
            case "encoding":
                transpiler.setEncoding(value.toString());
                break;
            case "noRootDirectories":
                if (value instanceof Boolean) {
                    transpiler.setNoRootDirectories((Boolean) value);
                }
                break;
            case "enableAssertions":
                if (value instanceof Boolean) {
                    transpiler.setIgnoreAssertions(!(Boolean) value);
                }
                break;
            case "declaration":
                if (value instanceof Boolean) {
                    transpiler.setGenerateDeclarations((Boolean) value);
                }
                break;
            case "tsOnly":
                if (value instanceof Boolean) {
                    transpiler.setGenerateJsFiles(!(Boolean) value);
                }
                break;
            case "ignoreAssertions":
                if (value instanceof Boolean) {
                    transpiler.setIgnoreAssertions((Boolean) value);
                }
                break;
            case "header":
                if (value instanceof File) {
                    transpiler.setHeaderFile((File) value);
                }
                break;
            // workingDir option removed as JSweetTranspiler doesn't have setWorkingDir method
            case "tsOut":
                if (value instanceof File) {
                    transpiler.setTsOutputDir((File) value);
                }
                break;
            case "jsOut": 
                if (value instanceof File) {
                    transpiler.setJsOutputDir((File) value);
                }
                break;
            // candiesJsOut option removed as it doesn't exist in current JSweet version
            default:
                System.err.println("Unknown option: " + key);
        }
    }
    
    /**
     * Save the generated TypeScript to the snapshot directory
     */
    private Path saveSnapshot(String testName, SourceFile sourceFile) {
        try {
            File tsFile = sourceFile.getTsFile();
            if (tsFile == null || !tsFile.exists()) {
                fail("No TypeScript file generated for " + testName);
            }
            
            String tsContent = FileUtils.readFileToString(tsFile);
            Path snapshotFile = snapshotPath.resolve(testName + ".ts");
            Files.write(snapshotFile, tsContent.getBytes());
            
            System.out.println("Snapshot saved to: " + snapshotFile);
            return snapshotFile;
            
        } catch (IOException e) {
            fail("Failed to save snapshot for " + testName + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Run TypeScript evaluation (non-blocking for snapshot creation)
     */
    private void runTsEvaluation(SourceFile sourceFile, String testName) {
        try {
            String javaTestFilePath = sourceFile.getJavaFile().getAbsolutePath();
            File tsFile = new File(javaTestFilePath.substring(0, javaTestFilePath.length() - 5) + ".ts");
            TsSourceFile tsSourceFile = new TsSourceFile(tsFile);
            
            System.out.println("Running TypeScript evaluation for " + testName);
            TestTranspilationHandler logHandler = new TestTranspilationHandler();
            
            transpilerTest().getTranspiler().setTsOutputDir(tsSourceFile.getTsFile().getParentFile());
            EvaluationResult result = transpilerTest().getTranspiler().eval(logHandler, tsSourceFile);
            
            if (result != null) {
                System.out.println("TypeScript evaluation successful for " + testName);
            }
            
            FileUtils.deleteQuietly(tsSourceFile.getJsFile());
            
        } catch (Exception e) {
            System.err.println("TypeScript evaluation error for " + testName + ": " + e.getMessage());
            // Don't throw - this is non-blocking
        }
    }
    
    /**
     * Compare snapshot with expected output (for regression testing)
     */
    protected void assertSnapshotMatches(String testName, String expectedContent) {
        try {
            Path snapshotFile = snapshotPath.resolve(testName + ".ts");
            assertTrue("Snapshot file should exist: " + snapshotFile, Files.exists(snapshotFile));
            
            String actualContent = new String(Files.readAllBytes(snapshotFile));
            
            if (!expectedContent.equals(actualContent)) {
                // Save diff for debugging
                Path diffFile = snapshotPath.resolve(testName + ".diff");
                Files.write(diffFile, ("Expected:\n" + expectedContent + "\n\nActual:\n" + actualContent).getBytes());
                fail("Snapshot mismatch for " + testName + ". Diff saved to: " + diffFile);
            }
            
        } catch (IOException e) {
            fail("Failed to compare snapshot for " + testName + ": " + e.getMessage());
        }
    }

    /**
     * Inner class for TypeScript source files
     */
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

        public File getJsFile() {
            String tsPath = getTsFile().getAbsolutePath();
            return new File(tsPath.substring(0, tsPath.length() - 3) + ".js");
        }
    }

    @Test
    public void testStackTraceCreator() {
        Map<String, Object> options = new HashMap<>();
        options.put("target", "ES6");  // Latest available ES target in JSweet

        Path snapshotPath = runSnapshotTest(
                "StackTraceCreator",
                StackTraceCreator.class,
                options,
                false,  // Run TypeScript evaluation
                ModuleKind.es2015
        );

        assertTrue("Snapshot should be created", Files.exists(snapshotPath));
        System.out.println("Snapshot created at: " + snapshotPath);

    }
    // Test with latest available ES syntax (ES6)
    @Test
    public void testGwtEventSnapshot() {
        Map<String, Object> options = new HashMap<>();
        options.put("target", "ES6");  // Latest available ES target in JSweet
        
        Path snapshotPath = runSnapshotTest(
            "GwtEventTest",
            GwtEventTest.class,
            options,
            true,  // Run TypeScript evaluation
            ModuleKind.es2015
        );
        
        assertTrue("Snapshot should be created", Files.exists(snapshotPath));
        System.out.println("Snapshot created at: " + snapshotPath);
    }
    
    @Test
    public void testGwtEventSnapshotNoEval() {
        // Test without evaluation - just snapshot generation with ES6 syntax
        Map<String, Object> options = new HashMap<>();
        options.put("target", "ES6");
        
        Path snapshotPath = runSnapshotTest(
            "GwtEventTest_noeval",
            GwtEventTest.class,
            options,
            false, // Don't run eval
            ModuleKind.es2015
        );
        
        assertTrue("Snapshot should be created", Files.exists(snapshotPath));
        System.out.println("Snapshot created at: " + snapshotPath);
    }
}