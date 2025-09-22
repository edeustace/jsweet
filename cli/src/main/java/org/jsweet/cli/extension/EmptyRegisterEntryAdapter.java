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
package org.jsweet.cli.extension;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import org.jsweet.transpiler.JSweetContext;
import org.jsweet.transpiler.extension.PrinterAdapter;

/**
 * An adapter that empties out the registerEntry function in the
 * com.google.gwt.core.client.impl.Impl class, making it a no-op function.
 *
 * @author Ed Eustace
 */
public class EmptyRegisterEntryAdapter extends PrinterAdapter {

    public EmptyRegisterEntryAdapter(JSweetContext context) {
        super(context);
    }

    public EmptyRegisterEntryAdapter(PrinterAdapter parentAdapter) {
        super(parentAdapter);
    }

    @Override
    public boolean substituteExecutable(ExecutableElement executable) {
        // Check if this is the registerEntry method in the Impl class
        TypeElement parentType = (TypeElement) executable.getEnclosingElement();
        if (parentType != null &&
            "com.google.gwt.core.client.impl.Impl".equals(parentType.getQualifiedName().toString()) &&
            "registerEntry".equals(executable.getSimpleName().toString()) &&
            executable.getModifiers().contains(Modifier.NATIVE)) {

            // Print the method signature
            print("public static registerEntry(): any {");
            println();
            startIndent();
            printIndent();
            print("return null;");
            println();
            endIndent();
            printIndent();
            print("}");

            return true;
        }

        return super.substituteExecutable(executable);
    }

    @Override
    public boolean substituteMethodBody(TypeElement parentTypeElement, ExecutableElement method) {
        // Check if this is the registerEntry method in the Impl class
        if (parentTypeElement != null &&
            "com.google.gwt.core.client.impl.Impl".equals(parentTypeElement.getQualifiedName().toString()) &&
            "registerEntry".equals(method.getSimpleName().toString())) {

            print("return null;");
            return true;
        }

        return super.substituteMethodBody(parentTypeElement, method);
    }
}