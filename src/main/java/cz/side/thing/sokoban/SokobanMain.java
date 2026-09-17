package cz.side.thing.sokoban;

import java.util.List;
import java.util.Map;

import cz.side.thing.sokoban.core.BoardState;
import cz.side.thing.sokoban.core.BoardState.Field;
import cz.side.thing.sokoban.core.FieldType;
import lombok.extern.slf4j.Slf4j;

/**
 * Sample program for maven Java build. TODO: Change it to your needs.
 *
 * @author maven-archetype-generated
 *
 */
@Slf4j
public final class SokobanMain {
  
  private static final String MAP1 = """
          WWWWW
          W...W
          WC..W
        WWW..CWW
        W..C.C.W
      WWW.W.WW.W   WWWWWW
      W...W.WW.WWWWW..XXW
      W.C..C..........XXW
      WWWWW.WWW.WPWW..XXW
          W.....WWWWWWWWW
          WWWWWWW
      """;
  
  private static final Map<FieldType, String> SPRITES = Map.ofEntries(
      Map.entry(FieldType.WALL, "█"), Map.entry(FieldType.VOID, " "),
      Map.entry(FieldType.CRATE, "▒"), Map.entry(FieldType.TARGET, "X"),
      Map.entry(FieldType.CRATE_ON_TARGET, "▓"), Map.entry(FieldType.FLOOR, " "),
      Map.entry(FieldType.PLAYER, "I"), Map.entry(FieldType.PLAYER_ON_TARGET, "T"));

  /**
   * Disable creation of instances.
   */
  private SokobanMain() {
    final BoardState board1 = BoardState.fromString(MAP1);
    drawBoard(board1);
  }
  
  private void drawBoard(final BoardState board1) {
    for (final List<Field> row : board1.getFields()) {
      
      final StringBuilder str = new StringBuilder();
      row.forEach(it -> str.append(SPRITES.get(it.type())));

      System.out.println(str);
      
    }
  }

  /**
   * Sample main method.
   *
   * @param args program arguments
   */
  public static void main(final String[] args) {
    try {
      new SokobanMain();
    } catch (final Throwable ex) {
      ex.printStackTrace();
    }
  }
}
