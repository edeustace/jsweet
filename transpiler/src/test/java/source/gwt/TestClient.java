package source.gwt;

public class TestClient {
    public static void main(String[] args) {
        // Test accessing the static field directly
        Foo directAccess = FooContainer.impl;
        directAccess.doSomething();

        // Test accessing via getter method
        Foo viaGetter = FooContainer.getImpl();
        viaGetter.doSomething();
    }
}