package source.structural.gwt.client.ui;

/**
 * Simplified RootPanel test case to debug static inner class closing brace issue.
 */
public class RootPanel {

  /**
   * A static inner class that should generate as a class expression.
   */
  private static class DefaultRootPanel extends RootPanel implements Runnable {

    public DefaultRootPanel() {
      super();
    }

    public void testMethod() {
      System.out.println("This is a test method");
    }

    @Override
    public void run() {
      // Implementation for Runnable interface
    }
  }

  // Some static fields to test the class structure
  private static RootPanel instance;

  public RootPanel() {
    // Simple constructor
  }

  public static RootPanel get() {
    if (instance == null) {
      instance = new DefaultRootPanel();
    }
    return instance;
  }

  public void someMethod() {
    System.out.println("Some method in outer class");
  }
}