package cz.side.thing.sokoban.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import cz.side.thing.sokoban.core.BoardState;
import cz.side.thing.sokoban.ui.cmd.AnsiColor;
import cz.side.thing.sokoban.ui.cmd.BoardRenderer;

/**
 * Tests of {@link BoardRenderer}.
 *
 * @author MacGregor
 */
public class BoardRendererTest {
  
  private static final String WALL = AnsiColor.WHITE.toString() + "█";
  private static final String RESET = AnsiColor.RESET;
  
  /** Without colors only the sprites are rendered. */
  @Test
  public void rendersPlainSprites() {
    final BoardState board = BoardState.fromString("WWWWW|WPCXW|WWWWW");
    
    assertEquals(List.of("█████", "█I▒X█", "█████"), BoardRenderer.render(board, false));
  }
  
  /**
   * With colors, adjacent fields of the same color share one escape sequence and
   * each colored row ends with a reset.
   */
  @Test
  public void rendersColorsOnlyOnChange() {
    final BoardState board = BoardState.fromString("WWW|WPW");
    
    final List<String> rows = BoardRenderer.render(board, true);
    
    assertEquals(AnsiColor.WHITE.toString() + "███" + RESET, rows.get(0));
    assertEquals(WALL + AnsiColor.BRIGHT_CYAN.toString() + "I" + WALL + RESET, rows.get(1));
  }
  
  /** Uncolored fields between colored ones switch back to the default color. */
  @Test
  public void resetsColorForUncoloredFields() {
    final BoardState board = BoardState.fromString("W.P");
    
    assertEquals(WALL + RESET + " " + AnsiColor.BRIGHT_CYAN.toString() + "I" + RESET,
        BoardRenderer.render(board, true).get(0));
  }
  
}
