package cz.side.thing.sokoban;

import cz.side.thing.sokoban.ui.cmd.CmdUi;
import cz.side.thing.sokoban.ui.common.IncompatibleUIException;
import cz.side.thing.sokoban.ui.debug.DebugUi;
import cz.side.thing.sokoban.ui.debug.Ui;
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
  
  /**
   * Sample main method.
   *
   * @param args program arguments
   */
  public static void main(final String[] args) {
    try {
      try {
        final CmdUi ui = new CmdUi(MAP1);
        ui.play();
      } catch (final IncompatibleUIException ex) {
        ex.printStackTrace();
        final Ui ui = new DebugUi(MAP1);
        ui.play();
      }
    } catch (final Throwable ex) {
      ex.printStackTrace();
    }
  }
}
