package source.gwt;

import source.gwt.FooImpl;

public class FooContainer {
    public static final Foo impl = GWT.create(Foo.class);

    public static Foo getImpl() {
        return impl;
    }

    public static void testMethod() {
        Foo testInstance = GWT.create(Foo.class);
        testInstance.doSomething();
    }
}