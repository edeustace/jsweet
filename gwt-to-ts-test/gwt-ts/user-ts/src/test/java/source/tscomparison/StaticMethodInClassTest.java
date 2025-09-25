package source.tscomparison;

/**
 * Test case for static methods inside classes.
 * This should generate proper TypeScript static methods, not export functions inside classes.
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

    public static void main(String[] args) {
        // Test static method calls
        String result = TestClass.staticMethod("hello");
        int doubled = TestClass.anotherStaticMethod(5);

        // Test instance creation and usage
        TestClass instance = new TestClass("test");
        String field = instance.getInstanceField();

        System.out.println(result + ", " + doubled + ", " + field);
    }
}