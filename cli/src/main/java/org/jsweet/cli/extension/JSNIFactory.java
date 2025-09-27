package org.jsweet.cli.extension;

import org.jsweet.transpiler.JSweetContext;
import org.jsweet.transpiler.JSweetFactory;
import org.jsweet.transpiler.extension.Java2TypeScriptAdapter;
import org.jsweet.transpiler.extension.PrinterAdapter;
import org.jsweet.transpiler.extension.RemoveJavaDependenciesAdapter;

/**
 * Custom JSweet factory that injects our JSNIAdapter into the adapter chain.
 *
 * This factory follows the pattern from JSweetFactory.createAdapter() to properly
 * chain our JSNI adapter with the default JSweet adapters.
 */
public class JSNIFactory extends JSweetFactory {

    private final String sourceRootPath;

    public JSNIFactory(String sourceRootPath) {
        this.sourceRootPath = sourceRootPath;
        System.out.println(
            "🏭 JSNIFactory: Initialized with source root: " + sourceRootPath
        );
    }

    @Override
    public PrinterAdapter createAdapter(JSweetContext context) {
        System.out.println(
            "🏭 JSNIFactory: Creating adapter chain with JSNI support"
        );

        // Get the default adapter from parent factory
        PrinterAdapter baseAdapter = super.createAdapter(context);
        System.out.println("   Got base adapter from parent factory");

        // Chain our EmptyRegisterEntryAdapter first
        System.out.println("   Chaining EmptyRegisterEntryAdapter");
        EmptyRegisterEntryAdapter emptyRegisterEntryAdapter = new EmptyRegisterEntryAdapter(baseAdapter);

        // Chain GWTCreateAdapter
        System.out.println("   Chaining GWTCreateAdapter on top of EmptyRegisterEntryAdapter");
        GWTCreateAdapter gwtAdapter = new GWTCreateAdapter(emptyRegisterEntryAdapter);
        
        // Chain our JSNI adapter on top with source root path
        System.out.println("   Chaining JSNIAdapter on top of GWTCreateAdapter");
        JSNIAdapter jsniAdapter = new JSNIAdapter(gwtAdapter, sourceRootPath);

        System.out.println(
            "✅ JSNIFactory: Adapter chain complete - JSNI support, GWT.isClient/create, and EmptyRegisterEntry enabled"
        );
        return jsniAdapter;
    }
}
