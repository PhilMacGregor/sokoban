package cz.side.thing.sokoban.ui.debug;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cz.side.thing.sokoban.core.BoardState;
import cz.side.thing.sokoban.core.BoardState.Field;
import cz.side.thing.sokoban.core.FieldType;
import cz.side.thing.sokoban.core.Input;
import cz.side.thing.sokoban.core.SokobanProcessor;
import cz.side.thing.sokoban.ui.common.Ui;
import cz.side.thing.sokoban.ui.common.UiException;
import lombok.Cleanup;

public class DebugUi implements Ui {

  private static final Map<FieldType, String> SPRITES = Map.ofEntries(
      Map.entry(FieldType.WALL, "█"), Map.entry(FieldType.VOID, " "),
      Map.entry(FieldType.CRATE, "▒"), Map.entry(FieldType.TARGET, "X"),
      Map.entry(FieldType.CRATE_ON_TARGET, "▓"), Map.entry(FieldType.FLOOR, " "),
      Map.entry(FieldType.PLAYER, "I"), Map.entry(FieldType.PLAYER_ON_TARGET, "T"));
  
  private static final Map<String, Input> INPUTS = Map.ofEntries(Map.entry("up", Input.UP),
      Map.entry("w", Input.UP), Map.entry("down", Input.DOWN), Map.entry("s", Input.DOWN),
      Map.entry("left", Input.LEFT), Map.entry("a", Input.LEFT), Map.entry("right", Input.RIGHT),
      Map.entry("d", Input.RIGHT));

  private final SokobanProcessor processor;

  private final BoardState board;

  public DebugUi(final String map) {
    this.processor = new SokobanProcessor();
    this.board = processor.initialize(map);
  }

  @Override
  public void play() throws UiException {
    try {
      @Cleanup
      final Scanner scan = new Scanner(System.in, Charset.defaultCharset());

      BoardState board = this.board;
      drawBoard(board);

      while (!board.isWon()) {
        String in = scan.nextLine();

        if (!INPUTS.containsKey(in)) {
          continue;
        }

        board = processor.play(board, INPUTS.get(in));
        drawBoard(board);
      }

      System.out.println("");
      System.out.println("");
      System.out.println("----------------------");
      System.out.println("       You win!       ");
      System.out.println("----------------------");
      System.out.println("");
      System.out.println("");
      
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
