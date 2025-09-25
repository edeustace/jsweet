package source.tscomparison;

/**
 * Test case for static methods inside classes.
 * This should generate proper TypeScript static methods, not export functions inside classes.
 * This reproduces the bug seen in HasHorizontalAlignment where static methods in static inner classes
 * are transpiled as "export function" instead of "static" methods.
 */
public class StaticMethodInClassTest {

    public static class TestClass {

        public static String staticMethod(String input) {
            return "processed: " + input;
        }

        public static int anotherStaticMethod(int value) {
            return value * 2;
        }

        private final String instanceField;

        public TestClass(String field) {
            this.instanceField = field;
        }

        public String getInstanceField() {
            return instanceField;
        }
    }

    // Additional test case that matches HasHorizontalAlignment pattern more closely
    public interface TestInterface {

        class InnerTestClass {

            // These static methods should generate as "static" not "export function"
            public static InnerTestClass createFrom(String value) {
                return new InnerTestClass(value);
            }

            public static String formatValue(String input) {
                return "formatted: " + input;
            }

            private final String data;

            public InnerTestClass(String data) {
                this.data = data;
            }

            public String getData() {
                return data;
            }
        }
    }

    public static void main(String[] args) {
        // Test static method calls on static inner class
        String result = TestClass.staticMethod("hello");
        int doubled = TestClass.anotherStaticMethod(5);

        // Test instance creation and usage
        TestClass instance = new TestClass("test");
        String field = instance.getInstanceField();

        // Test the interface inner class static methods
        TestInterface.InnerTestClass innerInstance = TestInterface.InnerTestClass.createFrom("data");
        String formatted = TestInterface.InnerTestClass.formatValue("input");

        System.out.println(result + ", " + doubled + ", " + field + ", " + formatted + ", " + innerInstance.getData());
    }
}