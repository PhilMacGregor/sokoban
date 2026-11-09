package cz.side.thing.sokoban.ui.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cz.side.thing.sokoban.core.BoardState;
import cz.side.thing.sokoban.core.BoardState.Field;
import cz.side.thing.sokoban.core.FieldType;

/**
 * Converts a {@link BoardState} into text rows for the console, optionally
 * colored with ANSI escape sequences.
 *
 * <p>
 * The class has no console dependency, so rendering can be tested without a
 * terminal.
 *
 * @author MacGregor
 */
public final class BoardRenderer {
  
  private static final Map<FieldType, String> SPRITES = Map.ofEntries(
      Map.entry(FieldType.WALL, "█"), Map.entry(FieldType.VOID, " "),
      Map.entry(FieldType.CRATE, "▒"), Map.entry(FieldType.TARGET, "X"),
      Map.entry(FieldType.CRATE_ON_TARGET, "▓"), Map.entry(FieldType.FLOOR, " "),
      Map.entry(FieldType.PLAYER, "I"), Map.entry(FieldType.PLAYER_ON_TARGET, "T"));
  
  /** Field colors; field types not listed here use the terminal default color. */
  private static final Map<FieldType, AnsiColor> COLORS = Map.of(FieldType.WALL, AnsiColor.WHITE,
      FieldType.CRATE, AnsiColor.YELLOW, FieldType.CRATE_ON_TARGET, AnsiColor.BRIGHT_GREEN,
      FieldType.TARGET, AnsiColor.BRIGHT_RED, FieldType.PLAYER, AnsiColor.BRIGHT_CYAN,
      FieldType.PLAYER_ON_TARGET, AnsiColor.BRIGHT_CYAN);
  
  private BoardRenderer() {
    // utility class
  }
  
  /**
   * Renders the board row by row.
   *
   * <p>
   * With colors enabled, a color escape sequence is emitted only when the color
   * changes between adjacent fields, and every row that switched color ends with
   * {@link AnsiColor#RESET}, so the color never leaks into the following output.
   *
   * @param board     board to render
   * @param useColors whether to include ANSI color escape sequences
   * @return one string per board row, without line separators
   */
  public static List<String> render(final BoardState board, final boolean useColors) {
    final List<String> rows = new ArrayList<>();
    
    for (final List<Field> row : board.getFields()) {
      rows.add(renderRow(row, useColors));
    }
    
    return rows;
  }
  
  private static String renderRow(final List<Field> row, final boolean useColors) {
    final StringBuilder str = new StringBuilder();
    
    AnsiColor currentColor = null;
    
    for (final Field field : row) {
      
      if (useColors) {
        final AnsiColor color = COLORS.get(field.type());
        
        /*
         * If the color changed, add an appropriate escape sequence for the new color or
         * RESET to set the color to default.
         */
        if (!Objects.equals(color, currentColor)) {
          str.append(color == null ? AnsiColor.RESET : color.toString());
          currentColor = color;
        }
      }
      
      str.append(SPRITES.get(field.type()));
    }
    
    /*
     * If a color was changed during rendering, add RESET afterwards.
     */
    if (currentColor != null) {
      str.append(AnsiColor.RESET);
    }
    
    return str.toString();
  }
  
}
