package source.tscomparison;

/**
 * Test case for inner abstract class handling.
 * This should be transpiled to separate TypeScript classes to avoid namespace issues.
 */
public class InnerAbstractClassTest {

    // Static inner abstract class
    public static abstract class StaticAbstractInner {
        public abstract void abstractMethod();

        public void concreteMethod() {
            System.out.println("Static concrete method");
        }
    }

    // Non-static inner abstract class
    public abstract class NonStaticAbstractInner {
        public abstract void abstractMethod();

        public void concreteMethod() {
            System.out.println("Non-static concrete method");
        }
    }

    // Concrete implementations
    public static class ConcreteStaticImpl extends StaticAbstractInner {
        @Override
        public void abstractMethod() {
            System.out.println("Static implementation");
        }
    }

    public class ConcreteNonStaticImpl extends NonStaticAbstractInner {
        @Override
        public void abstractMethod() {
            System.out.println("Non-static implementation");
        }
    }

    // Test field types
    private StaticAbstractInner staticField = new ConcreteStaticImpl();
    private NonStaticAbstractInner nonStaticField;

    // Usage
    public void testMethod() {
        StaticAbstractInner staticInstance = new ConcreteStaticImpl();
        staticInstance.abstractMethod();
        staticInstance.concreteMethod();

        NonStaticAbstractInner nonStaticInstance = new ConcreteNonStaticImpl();
        nonStaticInstance.abstractMethod();
        nonStaticInstance.concreteMethod();
    }

    // Test method parameter types
    public void methodWithParameters(StaticAbstractInner param1, NonStaticAbstractInner param2) {
        param1.concreteMethod();
        param2.concreteMethod();
    }

    // Test method return types
    public StaticAbstractInner returnStaticAbstract() {
        return new ConcreteStaticImpl();
    }

    public NonStaticAbstractInner returnNonStaticAbstract() {
        return new ConcreteNonStaticImpl();
    }

    // Test array types
    public void testArrayTypes() {
        StaticAbstractInner[] staticArray = new StaticAbstractInner[1];
        NonStaticAbstractInner[] nonStaticArray = new NonStaticAbstractInner[1];
        staticArray[0] = new ConcreteStaticImpl();
        nonStaticArray[0] = new ConcreteNonStaticImpl();
    }

    public static void main(String[] args) {
        InnerAbstractClassTest test = new InnerAbstractClassTest();
        test.testMethod();

        // Test parameter usage
        test.methodWithParameters(test.returnStaticAbstract(), test.returnNonStaticAbstract());
        test.testArrayTypes();
    }
}