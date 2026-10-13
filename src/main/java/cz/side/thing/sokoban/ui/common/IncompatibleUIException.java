package cz.side.thing.sokoban.ui.common;

/**
 * Signals that a user interface cannot run in the current environment (e.g. no Windows console
 * is attached). The caller is expected to fall back to another interface.
 */
public class IncompatibleUIException extends UiException {
  
  private static final long serialVersionUID = -5134749847233854465L;
  
  /**
   * Creates the exception.
   *
   * @param message reason why the interface cannot be used
   */
  public IncompatibleUIException(final String message) {
    super(message);
  }

}
