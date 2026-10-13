package cz.side.thing.sokoban.test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import cz.side.thing.sokoban.ui.cmd.WindowsInput;
import cz.side.thing.sokoban.ui.common.IncompatibleUIException;

/**
 * Tests of {@link WindowsInput}.
 */
public class WindowsInputTest {

  /**
   * On a non-Windows system the constructor must fail with {@link IncompatibleUIException}
   * (not with a JNA {@link UnsatisfiedLinkError}), so that the game can fall back to the debug
   * interface.
   */
  @Test
  @DisabledOnOs(OS.WINDOWS)
  public void failsWithIncompatibleUiOutsideWindows() {
    assertThrows(IncompatibleUIException.class, WindowsInput::new);
  }

}
