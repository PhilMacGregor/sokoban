package cz.side.thing.sokoban.ui.cmd;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The 16 standard console colors and their ANSI SGR foreground escape
 * sequences.
 *
 * <p>
 * The sequences are interpreted only when the console has virtual terminal
 * processing enabled (see {@link WindowsOutput}). The exact shade of each color
 * depends on the color scheme of the terminal.
 *
 * @author MacGregor
 */
@RequiredArgsConstructor
@Getter
public enum AnsiColor {
  
  /** Black. */
  BLACK(30),
  /** Dark red. */
  RED(31),
  /** Dark green. */
  GREEN(32),
  /** Dark yellow (often rendered as brown or olive). */
  YELLOW(33),
  /** Dark blue. */
  BLUE(34),
  /** Dark magenta. */
  MAGENTA(35),
  /** Dark cyan. */
  CYAN(36),
  /** Light gray. */
  WHITE(37),
  /** Dark gray. */
  BRIGHT_BLACK(90),
  /** Bright red. */
  BRIGHT_RED(91),
  /** Bright green. */
  BRIGHT_GREEN(92),
  /** Bright yellow. */
  BRIGHT_YELLOW(93),
  /** Bright blue. */
  BRIGHT_BLUE(94),
  /** Bright magenta. */
  BRIGHT_MAGENTA(95),
  /** Bright cyan. */
  BRIGHT_CYAN(96),
  /** White. */
  BRIGHT_WHITE(97);
  
  /**
   * Escape sequence that resets all colors and text attributes to the terminal
   * default.
   */
  public static final String RESET = "\033[0m";
  
  /** SGR parameter of the foreground color. */
  private final int code;
  
  /**
   * Returns the escape sequence that switches the foreground (text) color to this
   * color.
   *
   * @return ANSI escape sequence, e.g. {@code ESC[33m} for {@link #YELLOW}
   */
  @Override
  public String toString() {
    return "\033[" + code + "m";
  }
  
}
