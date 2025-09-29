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

import org.jsweet.transpiler.extension.PrinterAdapter;
import org.jsweet.transpiler.model.MethodInvocationElement;

/**
 * This adapter removes calls to JavaScriptObject.cast() method.
 * <p>
 * In GWT, JavaScriptObject.cast() is used to safely cast JSO types:
 * {@code JavaScriptObject.createObject().cast()}
 * <p>
 * In TypeScript, this is unnecessary because:
 * 1. createObject() returns a plain {} object, not a JavaScriptObject instance
 * 2. TypeScript's type system handles casting through type assertions
 * <p>
 * This adapter replaces:
 *   expression.cast()
 * with:
 *   expression
 * <p>
 * The cast() call is simply removed, leaving just the target expression.
 *
 * @author JSweet
 */
public class RemoveCastAdapter extends PrinterAdapter {

    public RemoveCastAdapter(PrinterAdapter parent) {
        super(parent);
    }

    @Override
    public boolean substituteMethodInvocation(MethodInvocationElement invocation) {
        // Check if this is a call to cast() method
        if ("cast".equals(invocation.getMethodName())) {
            // Check if it's being called on a JavaScriptObject or subclass
            if (invocation.getTargetExpression() != null) {
                String targetType = invocation.getTargetExpression().getType().toString();

                // If it's a JavaScriptObject type, remove the .cast() call
                if (targetType.contains("JavaScriptObject") || targetType.contains("JsMap")) {
                    // Just print the target expression without the .cast() call
                    print(invocation.getTargetExpression());
                    return true;
                }
            }
        }

        return super.substituteMethodInvocation(invocation);
    }
}