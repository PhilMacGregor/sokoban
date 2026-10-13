package cz.side.thing.sokoban.ui.cmd;

import java.util.Arrays;
import java.util.Objects;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.Wincon;
import com.sun.jna.ptr.IntByReference;

import cz.side.thing.sokoban.ui.common.IncompatibleUIException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Reads single key presses from the Windows console via the Win32 API ({@code Kernel32} through
 * JNA), without waiting for Enter.
 *
 * <p>
 * The console input mode is changed on creation and restored on {@link #close()}, so the
 * instance should be used in a try-with-resources block.
 */
@SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "WIP. To be refactored later.")
public class WindowsInput implements AutoCloseable {
  
  // Windows virtual-key codes of the supported keys.
  private static final short VK_NULL = 0x00;
  private static final short VK_ESCAPE = 0x1B;
  private static final short VK_LEFT = 0x25;
  private static final short VK_UP = 0x26;
  private static final short VK_RIGHT = 0x27;
  private static final short VK_DOWN = 0x28;
  private static final short VK_F5 = 0x74;
  private static final short VK_Y = 0x59;
  private static final short VK_Z = 0x5A;
  private static final short VK_PGUP = 0x21;
  private static final short VK_PGDOWN = 0x22;
  
  private static final int STD_INPUT_HANDLE = -10;
  
  // Console mode flags, see SetConsoleMode documentation.
  private static final int ENABLE_EXTENDED_FLAGS = 0x0080;
  private static final int ENABLE_QUICK_EDIT_MODE = 0x0040;
  
  private final HANDLE consoleInput;
  private final int originalConsoleMode;
  
  /**
   * Opens the console input and disables the quick edit mode.
   *
   * @throws IncompatibleUIException when the process is not attached to a Windows console (e.g.
   *         IDE console or a non-Windows system)
   */
  public WindowsInput() throws IncompatibleUIException {
    // Must be checked before the first access to Kernel32 - on other systems its static
    // initialization fails with UnsatisfiedLinkError, which would bypass the UI fallback.
    if (!Platform.isWindows()) {
      throw new IncompatibleUIException(
          "Windows console is not available on this system: " + System.getProperty("os.name"));
    }

    consoleInput = Kernel32.INSTANCE.GetStdHandle(STD_INPUT_HANDLE);
    
    if (consoleInput == null) {
      throw new IncompatibleUIException("Cannot get console input handle.");
    }
    
    final IntByReference mode = new IntByReference();
    
    // Fails when stdin is not a real console, e.g. when redirected or running in an IDE.
    if (!Kernel32.INSTANCE.GetConsoleMode(consoleInput, mode)) {
      throw new IncompatibleUIException(
          "Cannot get console input mode. Error: " + Kernel32.INSTANCE.GetLastError());
    }
    
    originalConsoleMode = mode.getValue();
    
    disableQuickEditMode();
  }
  
  /**
   * Disables the quick edit mode. With it enabled, a mouse click into the console starts a text
   * selection and pauses the application until the selection is cancelled.
   */
  private void disableQuickEditMode() throws IncompatibleUIException {
    // ENABLE_EXTENDED_FLAGS is required for the quick edit flag to be taken into account.
    int newMode = originalConsoleMode | ENABLE_EXTENDED_FLAGS;
    
    newMode &= ~ENABLE_QUICK_EDIT_MODE;
    
    if (!Kernel32.INSTANCE.SetConsoleMode(consoleInput, newMode)) {
      throw new IncompatibleUIException(
          "Cannot configure console input mode. Error: " + Kernel32.INSTANCE.GetLastError());
    }
  }
  
  /**
   * Blocks until a key is pressed. Non-keyboard events (mouse, resize, focus) and key releases
   * are ignored.
   *
   * @return the pressed key, or {@link Key#OTHER} for an unsupported key
   * @throws IllegalStateException when reading the console input fails
   */
  public Key readKey() {
    
    while (true) {
      
      final Wincon.INPUT_RECORD[] records = (Wincon.INPUT_RECORD[]) new Wincon.INPUT_RECORD()
          .toArray(1);
      
      final IntByReference eventsRead = new IntByReference();
      
      final boolean success = Kernel32.INSTANCE.ReadConsoleInput(consoleInput, records, 1,
          eventsRead);
      
      if (!success) {
        throw new IllegalStateException(
            "ReadConsoleInput failed. Error: " + Kernel32.INSTANCE.GetLastError());
      }
      
      if (eventsRead.getValue() == 0) {
        continue;
      }
      
      final Wincon.INPUT_RECORD record = records[0];
      
      if (record.EventType != Wincon.INPUT_RECORD.KEY_EVENT) {
        continue;
      }
      
      final Wincon.KEY_EVENT_RECORD keyEvent = record.Event.KeyEvent;
      
      // ReadConsoleInput dostává KEY_DOWN i KEY_UP.
      // Pro hru nás zajímá pouze stisk.
      if (!keyEvent.bKeyDown) {
        continue;
      }
      
      return Key.fromCode(keyEvent.wVirtualKeyCode);
      
    }
  }
  
  /**
   * Restores the console input mode that was active before this instance was created.
   */
  @Override
  public void close() {
    Kernel32.INSTANCE.SetConsoleMode(consoleInput, originalConsoleMode);
  }
  
  /**
   * Keys recognized by the game, each with its Windows virtual-key code.
   */
  @RequiredArgsConstructor
  @Getter
  public enum Key {
    /** Arrow up. */
    UP(VK_UP),
    /** Arrow down. */
    DOWN(VK_DOWN),
    /** Arrow left. */
    LEFT(VK_LEFT),
    /** Arrow right. */
    RIGHT(VK_RIGHT),
    /** Escape. */
    ESC(VK_ESCAPE),
    /** F5. */
    F5(VK_F5),
    /** Letter Y. */
    Y(VK_Y),
    /** Letter Z. */
    Z(VK_Z),
    /** Page Up. */
    PG_UP(VK_PGUP),
    /** Page Down. */
    PG_DOWN(VK_PGDOWN),
    /** Any key not listed here. */
    OTHER(VK_NULL);
    
    /** Windows virtual-key code. */
    private final short code;
    
    /**
     * Finds the key for a virtual-key code.
     *
     * @param code Windows virtual-key code
     * @return matching key, or {@link #OTHER} when the key is not supported
     */
    public static Key fromCode(final short code) {
      return Arrays.stream(values()).filter(it -> Objects.equals(it.getCode(), code)).findFirst()
          .orElse(OTHER);
    }
    
  }
  
}
