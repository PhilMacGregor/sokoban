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
  
  private final BoardState board;
  
  private final SokobanProcessor processor;
  
  private final Deque<BoardState> previousStates;
  
  public CmdUi(final String name, final String content, final boolean isCompleted) {
    this.processor = new SokobanProcessor();
    this.name = name;
    this.isCompleted = isCompleted;
    this.board = processor.initialize(content);
    this.previousStates = new LinkedList<>();
  }
  
  @Override
  public PlayResult start() throws UiException {
    try {
      @Cleanup
      final WindowsInput in = new WindowsInput();
      
      BoardState board = this.board;
      
      while (!board.isWon()) {
        drawBoard(board);
        
        final Key key = in.readKey();
        
        if (key == Key.F5) {
          System.out.println("RELOADING");
          board = this.board;
          previousStates.clear();
          continue;
        }
        
        if (key == Key.Y || key == Key.Z) {
          
          if (!previousStates.isEmpty()) {
            board = previousStates.getLast();
            previousStates.removeLast();
          }
          continue;
          
        }
        
        if (key == Key.PG_UP || key == Key.ESC) {
          return PlayResult.NEXT_MAP;
        }
        
        if (key == Key.PG_DOWN) {
          return PlayResult.PREVIOUS_MAP;
        }
        
        if (key == Key.OTHER) {
          continue;
        }
        
        BoardState newBoard = processor.play(board, INPUTS.get(key));
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
    
    System.out.println(StringUtils.EMPTY);
    System.out.printf("Map name: %s%s%n", name,
        isCompleted ? " - THIS MAP WAS COMPLETED" : StringUtils.EMPTY);
    
  }
  
  private void clearScreen() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }
  
}
