package source.structural.gwt.dom.client;

/**
 * Simple Element class for testing ButtonElement.
 */
public class Element {

    public static boolean is(Object o) {
        return o instanceof Element;
    }

    public boolean hasTagName(String tagName) {
        return true; // Simplified for testing
    }
}