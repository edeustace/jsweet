package source.structural.gwt.user.client.ui;

/**
 * Simple Widget class for testing.
 */
public class Widget {

    public Widget() {
        // Simple constructor
    }

    public void onAttach() {
        System.out.println("Widget attached");
    }

    public void onDetach() {
        System.out.println("Widget detached");
    }
}