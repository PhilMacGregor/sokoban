package cz.side.thing.sokoban.ui.common;

/**
 * General failure of a user interface.
 */
public class UiException extends Exception {

  private static final long serialVersionUID = 3342583832770432426L;
  
  /**
   * Creates the exception.
   *
   * @param message detail message
   * @param cause original cause
   * @param enableSuppression whether suppression is enabled
   * @param writableStackTrace whether the stack trace should be writable
   */
  public UiException(final String message, final Throwable cause, final boolean enableSuppression,
      final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  /**
   * Creates the exception.
   *
   * @param message detail message
   * @param cause original cause
   */
  public UiException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates the exception.
   *
   * @param message detail message
   */
  public UiException(final String message) {
    super(message);
  }

  /**
   * Creates the exception.
   *
   * @param cause original cause
   */
  public UiException(final Throwable cause) {
    super(cause);
  }

}
