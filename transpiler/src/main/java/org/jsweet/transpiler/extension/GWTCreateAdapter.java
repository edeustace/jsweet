/*
 * JSweet transpiler - http://www.jsweet.org
 * Copyright (C) 2015 CINCHEO SAS <renaud.pawlak@cincheo.fr>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jsweet.transpiler.extension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.jsweet.transpiler.model.ExtendedElement;
import org.jsweet.transpiler.model.MethodInvocationElement;

/**
 * This adapter replaces GWT.create() calls with direct instantiation of implementation classes.
 *
 * This is because in GWT, GWT.create() is populated by the compiler.
 * For example:
 * - GWT.create(Foo.class) becomes new FooImpl()
 *
 * The mapping from interface to implementation is configurable via the typeMapping.
 *
 * @author JSweet
 */
public class GWTCreateAdapter extends PrinterAdapter {

    private Map<String, String> typeMapping = new HashMap<>();

    /**
     * Default constructor that sets up basic type mappings.
     */
    public GWTCreateAdapter(PrinterAdapter parent) {
        super(parent);

        // Default mappings - can be extended
        typeMapping.put("source.gwt.Foo", "source.gwt.FooImpl");
    }

    /**
     * Adds a mapping from interface/class to implementation class.
     *
     * @param sourceType the fully qualified name of the interface/class used in GWT.create()
     * @param implType the fully qualified name of the implementation class to instantiate
     */
    public void addGWTTypeMapping(String sourceType, String implType) {
        typeMapping.put(sourceType, implType);
    }

    @Override
    public boolean substituteMethodInvocation(MethodInvocationElement invocation) {
        // Check if this is a GWT.create() call
        if ("create".equals(invocation.getMethodName()) &&
            invocation.getTargetExpression() != null) {

            Element targetType = invocation.getTargetExpression().getTypeAsElement();
            if (targetType != null && targetType.toString().endsWith("GWT")) {

                // Get the first argument (should be the class literal)
                if (invocation.getArgumentCount() > 0) {
                    ExtendedElement firstArg = invocation.getArgument(0);

                    // Extract the type from the class literal
                    // For Foo.class, we want to extract the fully qualified type name
                    String argString = firstArg.toString();
                    if (argString.endsWith(".class")) {
                        String typeName = argString.substring(0, argString.length() - 6);

                        // Look up the implementation class - try both simple name and fully qualified name
                        String implClassName = typeMapping.get(typeName);
                        if (implClassName == null && !typeName.contains(".")) {
                            // If simple name didn't work, try with the current package
                            String packageName = getCompilationUnit().getPackage().toString();
                            String fullyQualifiedTypeName = packageName + "." + typeName;
                            implClassName = typeMapping.get(fullyQualifiedTypeName);
                        }

                        if (implClassName != null) {
                            // Replace GWT.create(Foo.class) with new FooImpl()
                            // Use simple name - JSweet will handle imports if they exist in Java
                            String simpleImplName = implClassName.substring(implClassName.lastIndexOf('.') + 1);
                            printMacroName("GWT.create");
                            print("new " + simpleImplName + "()");
                            return true;
                        } else {
                            // Log for debugging
                            System.err.println("No mapping found for type: " + typeName);
                            System.err.println("Current package: " + getCompilationUnit().getPackage().toString());
                            System.err.println("Available mappings: " + typeMapping.keySet());
                        }
                    } else {
                        System.err.println("First argument is not a class literal: " + argString);
                    }
                } else {
                    System.err.println("GWT.create() called with no arguments");
                }
            }
        }

        return super.substituteMethodInvocation(invocation);
    }
}