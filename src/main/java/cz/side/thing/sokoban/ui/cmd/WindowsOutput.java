package cz.side.thing.sokoban.ui.cmd;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

import lombok.extern.slf4j.Slf4j;

/**
 * Enables virtual terminal (ANSI escape sequence) processing on the Windows console output, so
 * that colors are rendered instead of being printed as raw characters.
 *
 * <p>
 * Windows Terminal has the processing enabled by default, the classic console host may not.
 * Failure never throws: when the output is not a console (redirected, IDE console, non-Windows
 * system) or the mode cannot be changed, {@link #isVtEnabled()} returns {@code false} and the
 * caller should render without colors.
 *
 * <p>
 * The original output mode is restored on {@link #close()} when it was changed, so the instance
 * should be used in a try-with-resources block.
 *
 * @author MacGregor
 */
@Slf4j
public class WindowsOutput implements AutoCloseable {

  private static final int STD_OUTPUT_HANDLE = -11;

  // Console output mode flags, see SetConsoleMode documentation.
  private static final int ENABLE_PROCESSED_OUTPUT = 0x0001;
  private static final int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 0x0004;

  private final HANDLE consoleOutput;
  private final int originalConsoleMode;

  /** Whether {@link #close()} has to restore the original mode. */
  private final boolean modeChanged;

  private final boolean vtEnabled;

  /**
   * Opens the console output and tries to enable virtual terminal processing.
   */
  public WindowsOutput() {
    HANDLE handle = null;
    int mode = 0;
    boolean changed = false;
    boolean enabled = false;

    // Must be checked before the first access to Kernel32, see WindowsInput.
    if (Platform.isWindows()) {
      handle = Kernel32.INSTANCE.GetStdHandle(STD_OUTPUT_HANDLE);
      final IntByReference modeRef = new IntByReference();

      if (handle != null && Kernel32.INSTANCE.GetConsoleMode(handle, modeRef)) {
        mode = modeRef.getValue();
        final int requiredFlags = ENABLE_PROCESSED_OUTPUT | ENABLE_VIRTUAL_TERMINAL_PROCESSING;

        if ((mode & requiredFlags) == requiredFlags) {
          enabled = true;
        } else if (Kernel32.INSTANCE.SetConsoleMode(handle, mode | requiredFlags)) {
          enabled = true;
          changed = true;
        } else {
          log.debug("Cannot enable virtual terminal processing. Error: {}",
              Kernel32.INSTANCE.GetLastError());
        }

      } else {
        log.debug("Standard output is not a Windows console, colors are disabled.");
      }
    }

    this.consoleOutput = handle;
    this.originalConsoleMode = mode;
    this.modeChanged = changed;
    this.vtEnabled = enabled;
  }

  /**
   * Returns whether the console interprets ANSI escape sequences, i.e. whether colors can be
   * used.
   *
   * @return {@code true} when virtual terminal processing is enabled
   */
  public boolean isVtEnabled() {
    return vtEnabled;
  }

  /**
   * Restores the console output mode that was active before this instance was created, if it
   * was changed.
   */
  @Override
  public void close() {
    if (modeChanged) {
      Kernel32.INSTANCE.SetConsoleMode(consoleOutput, originalConsoleMode);
    }
  }

}
