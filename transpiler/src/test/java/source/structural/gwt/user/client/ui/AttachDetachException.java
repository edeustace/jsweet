package source.structural.gwt.user.client.ui;

/**
 * Test case for static interface and static inner class generation.
 */
public class AttachDetachException {

  /**
   * The command to execute when iterating through child widgets.
   */
  public static interface Command {
    void execute(Widget w);
  }

  /**
   * A static inner class to test alongside the interface.
   */
  private static class Helper extends AttachDetachException {

    public Helper() {
      super();
    }

    public void doSomething() {
      System.out.println("Helper doing something");
    }
  }

  /**
   * The singleton command used to attach widgets.
   */
  static final AttachDetachException.Command attachCommand = new AttachDetachException.Command() {
    public void execute(Widget w) {
      // Simplified implementation
      System.out.println("Attaching widget");
    }
  };

  /**
   * Static method that uses the interface.
   */
  public static void tryCommand(Command c, Widget w) {
    try {
      c.execute(w);
    } catch (Exception e) {
      System.out.println("Command failed");
    }
  }

  /**
   * Simple constructor.
   */
  public AttachDetachException() {
    // Simple constructor
  }
}