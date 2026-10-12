package cz.side.thing.sokoban.ui.cmd;

import java.util.Arrays;
import java.util.Objects;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.Wincon;
import com.sun.jna.ptr.IntByReference;

import cz.side.thing.sokoban.ui.common.IncompatibleUIException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SuppressFBWarnings(value = "CT_CONSTRUCTOR_THROW", justification = "WIP. To be refactored later.")
public class WindowsInput implements AutoCloseable {
  
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
  
  private static final int ENABLE_EXTENDED_FLAGS = 0x0080;
  private static final int ENABLE_QUICK_EDIT_MODE = 0x0040;
  
  private final HANDLE consoleInput;
  private final int originalConsoleMode;
  
  public WindowsInput() throws IncompatibleUIException {
    consoleInput = Kernel32.INSTANCE.GetStdHandle(STD_INPUT_HANDLE);
    
    if (consoleInput == null) {
      throw new IncompatibleUIException("Cannot get console input handle.");
    }
    
    final IntByReference mode = new IntByReference();
    
    if (!Kernel32.INSTANCE.GetConsoleMode(consoleInput, mode)) {
      throw new IncompatibleUIException(
          "Cannot get console input mode. Error: " + Kernel32.INSTANCE.GetLastError());
    }
    
    originalConsoleMode = mode.getValue();
    
    disableQuickEditMode();
  }
  
  private void disableQuickEditMode() throws IncompatibleUIException {
    int newMode = originalConsoleMode | ENABLE_EXTENDED_FLAGS;
    
    newMode &= ~ENABLE_QUICK_EDIT_MODE;
    
    if (!Kernel32.INSTANCE.SetConsoleMode(consoleInput, newMode)) {
      throw new IncompatibleUIException(
          "Cannot configure console input mode. Error: " + Kernel32.INSTANCE.GetLastError());
    }
  }
  
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
  
  @Override
  public void close() {
    Kernel32.INSTANCE.SetConsoleMode(consoleInput, originalConsoleMode);
  }
  
  @RequiredArgsConstructor
  @Getter
  public enum Key {
    UP(VK_UP), DOWN(VK_DOWN), LEFT(VK_LEFT), RIGHT(VK_RIGHT), ESC(VK_ESCAPE), F5(VK_F5),
    OTHER(VK_NULL), Y(VK_Y), Z(VK_Z), PG_UP(VK_PGUP), PG_DOWN(VK_PGDOWN);
    
    private final short code;
    
    public static Key fromCode(final short code) {
      return Arrays.stream(values()).filter(it -> Objects.equals(it.getCode(), code)).findFirst()
          .orElse(OTHER);
    }
    
  }
  
}
