package cz.side.thing.sokoban.ui.cmd;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import cz.side.thing.sokoban.core.BoardState;
import cz.side.thing.sokoban.core.BoardState.Field;
import cz.side.thing.sokoban.core.FieldType;
import cz.side.thing.sokoban.core.Input;
import cz.side.thing.sokoban.core.SokobanProcessor;
import cz.side.thing.sokoban.ui.cmd.WindowsInput.Key;
import cz.side.thing.sokoban.ui.common.IncompatibleUIException;
import cz.side.thing.sokoban.ui.common.PlayResult;
import cz.side.thing.sokoban.ui.common.Ui;
import cz.side.thing.sokoban.ui.common.UiException;
import lombok.Cleanup;

/**
 * Primary console interface. Reads single key presses directly from the Windows
 * console (see {@link WindowsInput}) and redraws the board after every key.
 *
 * <p>
 * Controls:
 * <ul>
 * <li>arrow keys - move the player</li>
 * <li>{@code Y} / {@code Z} - undo the last move</li>
 * <li>{@code F5} - restart the map</li>
 * <li>{@code Page Up} / {@code Esc} - skip to the next map</li>
 * <li>{@code Page Down} - go to the previous map</li>
 * </ul>
 */
public class CmdUi implements Ui {
  
  private static final Map<FieldType, String> SPRITES = Map.ofEntries(
      Map.entry(FieldType.WALL, "█"), Map.entry(FieldType.VOID, " "),
      Map.entry(FieldType.CRATE, "▒"), Map.entry(FieldType.TARGET, "X"),
      Map.entry(FieldType.CRATE_ON_TARGET, "▓"), Map.entry(FieldType.FLOOR, " "),
      Map.entry(FieldType.PLAYER, "I"), Map.entry(FieldType.PLAYER_ON_TARGET, "T"));
  
  private static final Map<Key, Input> INPUTS = Map.of(Key.UP, Input.UP, Key.DOWN, Input.DOWN,
      Key.LEFT, Input.LEFT, Key.RIGHT, Input.RIGHT);
  
  private final String name;
  
  private final boolean isCompleted;
  
  /** Initial board of the map, used for restart. */
  private final BoardState board;
  
  private final SokobanProcessor processor;
  
  /** Undo history; the most recent state is at the end. */
  private final Deque<BoardState> previousStates;
  
  /**
   * Creates the interface for one map.
   *
   * @param name        map name shown below the board
   * @param content     map string, see {@link BoardState}
   * @param isCompleted whether the map was already solved in this session
   */
  public CmdUi(final String name, final String content, final boolean isCompleted) {
    this.processor = new SokobanProcessor();
    this.name = name;
    this.isCompleted = isCompleted;
    this.board = processor.initialize(content);
    this.previousStates = new LinkedList<>();
  }
  
  /**
   * {@inheritDoc}
   *
   * @throws IncompatibleUIException when the Windows console is not available
   */
  @Override
  public PlayResult start() throws UiException {
    try {
      @Cleanup
      final WindowsInput in = new WindowsInput();
      
      BoardState board = this.board;
      
      while (!board.isWon()) {
        drawBoard(board);
        
        final Key key = in.readKey();
        
        // Restart - back to the initial board, undo history is discarded.
        if (key == Key.F5) {
          System.out.println("RELOADING");
          board = this.board;
          previousStates.clear();
          continue;
        }
        
        // Undo - both Y and Z are accepted to work with QWERTY and QWERTZ layouts.
        if (key == Key.Y || key == Key.Z) {
          
          if (!previousStates.isEmpty()) {
            board = previousStates.getLast();
            previousStates.removeLast();
          }
          continue;
          
        }
        
        if (key == Key.PG_UP) {
          return PlayResult.PREVIOUS_MAP;
        }
        
        if (key == Key.PG_DOWN || key == Key.ESC) {
          return PlayResult.NEXT_MAP;
        }
        
        if (key == Key.OTHER) {
          continue;
        }
        
        BoardState newBoard = processor.play(board, INPUTS.get(key));
        // Blocked moves (into a wall etc.) are not stored, so undo always reverts a
        // real move.
        if (!newBoard.equals(board)) {
          previousStates.addLast(board);
        }
        
        board = newBoard;
        
      }
      
      System.out.println("");
      System.out.println("");
      System.out.println("----------------------");
      System.out.println("       You win!       ");
      System.out.println("----------------------");
      System.out.println("");
      System.out.println("");
      
      return PlayResult.VICTORY;
      
    } catch (final IncompatibleUIException e) {
      // Rethrown unwrapped so the caller can switch to the fallback interface.
      throw e;
    } catch (final Exception e) {
      throw new UiException(e);
    }
    
  }
  
  /**
   * Clears the console and prints the board followed by the map name.
   */
  private void drawBoard(final BoardState board1) {
    clearScreen();
    
    for (final List<Field> row : board1.getFields()) {
      
      final StringBuilder str = new StringBuilder();
      row.forEach(it -> str.append(SPRITES.get(it.type())));
      System.out.println(str);
    }
    
    System.out.println(StringUtils.EMPTY);
    System.out.printf("Map name: %s%s%n", name,
        isCompleted ? " - THIS MAP WAS COMPLETED" : StringUtils.EMPTY);
    
  }
  
  /**
   * Clears the console using ANSI escape sequences (cursor home + erase screen).
   */
  private void clearScreen() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }
  
}
