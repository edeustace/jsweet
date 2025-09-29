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

import javax.lang.model.element.Element;

import org.jsweet.transpiler.extension.PrinterAdapter;
import org.jsweet.transpiler.model.VariableAccessElement;

/**
 * This adapter replaces DOMImpl.impl field access with a macro to avoid circular dependencies.
 * <p>
 * In the GWT codebase, many classes access DOMImpl.impl to get the singleton instance.
 * In Java source it's: DOMImpl.impl
 * JSweet generates it as: DOMImpl.impl_$LI$()
 * <p>
 * This creates circular dependency issues when:
 * - Element imports DOMImpl
 * - DOMImpl imports Element (for type signatures)
 * - AnchorElement extends Element
 * <p>
 * This adapter replaces:
 *   DOMImpl.impl (Java field access)
 * with:
 *   $domImpl() (macro call)
 * <p>
 * This allows us to define $domImpl in a separate module that doesn't create circular dependencies.
 *
 * @author JSweet
 */
public class DOMImplAdapter extends PrinterAdapter {

    public DOMImplAdapter(PrinterAdapter parent) {
        super(parent);
    }

    private boolean isDOMImplType(Element element) {
        if (element == null) {
            return false;
        }
        String typeName = element.toString();
        return typeName.endsWith(".DOMImpl") ||
               typeName.equals("com.google.gwt.dom.client.DOMImpl");
    }

    @Override
    public boolean substituteVariableAccess(VariableAccessElement variableAccess) {
        // Check if this is accessing DOMImpl.impl
        if ("impl".equals(variableAccess.getVariableName())) {
            Element targetType = variableAccess.getTargetExpression() != null ?
                                variableAccess.getTargetExpression().getTypeAsElement() : null;

            if (isDOMImplType(targetType)) {
                // Replace DOMImpl.impl with $domImpl()
                printMacroName("DOMImpl.impl");
                print("$domImpl");
                return true;
            }
        }

        return super.substituteVariableAccess(variableAccess);
    }
}