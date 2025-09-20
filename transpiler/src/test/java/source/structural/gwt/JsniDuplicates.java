package source.structural.gwt;

/**
 * Test class to examine JSNI method overloading behavior.
 * This class has overloaded JSNI methods to test if JSweet's overloading logic
 * is properly applied to JSNI methods or if they bypass it.
 */
public class JsniDuplicates {

    /**
     * JSNI method that takes a String parameter.
     * @param string the string parameter
     * @return the processed string
     */
    public native String foo(String string) /*-{
        return "string: " + string;
    }-*/;

    /**
     * JSNI method that takes an int parameter.
     * @param intValue the integer parameter
     * @return the processed integer
     */
    public native String foo(int intValue) /*-{
        return "int: " + intValue;
    }-*/;

    /**
     * JSNI method that takes a boolean parameter.
     * @param boolValue the boolean parameter
     * @return the processed boolean
     */
    public native String foo(boolean boolValue) /*-{
        return "boolean: " + boolValue;
    }-*/;

    /**
     * Regular Java method for comparison.
     * @param value the double parameter
     * @return the processed double
     */
    public String bar(double value) {
        return "double: " + value;
    }

    /**
     * Another regular Java method for comparison.
     * @param value the float parameter
     * @return the processed float
     */
    public String bar(float value) {
        return "float: " + value;
    }

    public JsniDuplicates() {
        // Simple constructor
    }

    /**
     * Test method to verify we can access source and detect JSNI.
     */
    public void testJsniDetection() {
        System.out.println("Testing JSNI detection");
    }
}