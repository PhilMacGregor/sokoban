package cz.side.thing.sokoban.ui.cmd;

import java.util.List;
import java.util.Map;

import cz.side.thing.sokoban.core.BoardState;
import cz.side.thing.sokoban.core.BoardState.Field;
import cz.side.thing.sokoban.core.FieldType;
import cz.side.thing.sokoban.core.Input;
import cz.side.thing.sokoban.core.SokobanProcessor;
import cz.side.thing.sokoban.ui.cmd.WindowsInput.Key;
import cz.side.thing.sokoban.ui.common.IncompatibleUIException;
import cz.side.thing.sokoban.ui.common.UiException;
import cz.side.thing.sokoban.ui.common.Ui;
import lombok.Cleanup;

public class CmdUi implements Ui {
  
  private static final Map<FieldType, String> SPRITES = Map.ofEntries(
      Map.entry(FieldType.WALL, "█"), Map.entry(FieldType.VOID, " "),
      Map.entry(FieldType.CRATE, "▒"), Map.entry(FieldType.TARGET, "X"),
      Map.entry(FieldType.CRATE_ON_TARGET, "▓"), Map.entry(FieldType.FLOOR, " "),
      Map.entry(FieldType.PLAYER, "I"), Map.entry(FieldType.PLAYER_ON_TARGET, "T"));

  private static final Map<Key, Input> INPUTS = Map.of(Key.UP, Input.UP, Key.DOWN, Input.DOWN,
      Key.LEFT, Input.LEFT, Key.RIGHT, Input.RIGHT);
  
  private final BoardState board;
  
  private final SokobanProcessor processor;
  
  /**
   * Disable creation of instances.
   */
  public CmdUi(final String map) {
    this.processor = new SokobanProcessor();
    this.board = processor.initialize(map);
  }

  @Override
  public void play() throws UiException {
    try {
      @Cleanup
      final WindowsInput in = new WindowsInput();
      
      BoardState board = this.board;
      drawBoard(board);
      
      while (!board.isWon()) {
        final Key key = in.readKey();
        
        if (key == Key.ESC) {
          break;
        }
        
        if (key == Key.OTHER) {
          continue;
        }
        
        board = processor.play(board, INPUTS.get(key));
        drawBoard(board);
      }
      
      System.out.println("");
      System.out.println("");
      System.out.println("----------------------");
      System.out.println("       You win!       ");
      System.out.println("----------------------");
      System.out.println("");
      System.out.println("");

    } catch (final IncompatibleUIException e) {
      throw e;
    } catch (final Exception e) {
      throw new UiException(e);
    }

  }

  private void drawBoard(final BoardState board1) {
    clearScreen();
    
    for (final List<Field> row : board1.getFields()) {

      final StringBuilder str = new StringBuilder();
      row.forEach(it -> str.append(SPRITES.get(it.type())));
      
      System.out.println(str);

    }
  }
  
  private void clearScreen() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }

}
