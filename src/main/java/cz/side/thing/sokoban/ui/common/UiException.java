package cz.side.thing.sokoban.ui.common;

public class UiException extends Exception {

  private static final long serialVersionUID = 3342583832770432426L;
  
  public UiException(final String message, final Throwable cause, final boolean enableSuppression,
      final boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    // TODO Auto-generated constructor stub
  }

  public UiException(final String message, final Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

  public UiException(final String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }

  public UiException(final Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }

}
