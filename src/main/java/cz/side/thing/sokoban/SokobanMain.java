package cz.side.thing.sokoban;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.stream.Stream;

import cz.side.thing.sokoban.ui.cmd.CmdUi;
import cz.side.thing.sokoban.ui.common.IncompatibleUIException;
import cz.side.thing.sokoban.ui.common.Ui;
import cz.side.thing.sokoban.ui.common.UiException;
import cz.side.thing.sokoban.ui.debug.DebugUi;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

/**
 * Sample program for maven Java build. TODO: Change it to your needs.
 *
 * @author maven-archetype-generated
 *
 */
@Slf4j
public final class SokobanMain {

  private static final String MAPS_DIR_PROPERTY = "sokoban.maps.dir";

  private static final String MAPS_DIR = "maps/original";

  /**
   * Sample main method.
   *
   * @param args program arguments
   */
  public static void main(final String[] args) {
    try {
      
      final Path mapsPath = resolveMapsPath();

      try (Stream<Path> maps = Files.list(mapsPath)) {
        maps.filter(Files::isRegularFile).sorted().forEachOrdered(SokobanMain::playMap);
      }
      
    } catch (final Throwable ex) {
      log.error("The application ended with error", ex);
      System.exit(1);
    }
  }

  /**
   * Finds the maps directory. Order: system property {@value #MAPS_DIR_PROPERTY}, then
   * {@value #MAPS_DIR} relative to the working directory, then {@value #MAPS_DIR} in any parent
   * of the location the classes were loaded from (e.g. project root when running from IDE, where
   * the working directory may be the workspace root).
   */
  private static Path resolveMapsPath() throws URISyntaxException {
    final String configured = System.getProperty(MAPS_DIR_PROPERTY);
    if (configured != null) {
      final Path path = Path.of(configured);
      if (!Files.isDirectory(path)) {
        throw new IllegalStateException("Maps directory not found: " + path.toAbsolutePath());
      }
      return path;
    }

    final Path workingDirMaps = Path.of(MAPS_DIR);
    if (Files.isDirectory(workingDirMaps)) {
      return workingDirMaps;
    }

    final CodeSource codeSource = SokobanMain.class.getProtectionDomain().getCodeSource();
    if (codeSource != null) {
      final URI location = codeSource.getLocation().toURI();
      if ("file".equals(location.getScheme())) {
        for (Path dir = Path.of(location); dir != null; dir = dir.getParent()) {
          final Path candidate = dir.resolve(MAPS_DIR);
          if (Files.isDirectory(candidate)) {
            return candidate;
          }
        }
      }
    }

    throw new IllegalStateException(
        "Maps directory not found: " + workingDirMaps.toAbsolutePath());
  }
  
  @SuppressFBWarnings(value = "THROWS_METHOD_THROWS_RUNTIMEEXCEPTION", justification = """
      Needed for usage inside a Stream.
      """)
  private static void playMap(final Path map) {
    System.out.printf("------------------------------------%n", map.getFileName());
    System.out.printf("MAP %s%n", map.getFileName());
    System.out.printf("------------------------------------%n", map.getFileName());
    
    try {
      final String content = Files.readString(map);
      
      try {
        final CmdUi ui = new CmdUi(content);
        ui.start();
      } catch (final IncompatibleUIException ex) {
        log.error("Unable to load console interface. Running in fallback mode...", ex);
        final Ui ui = new DebugUi(content);
        ui.start();
      }
      
    } catch (final UiException | IOException e) {
      throw new RuntimeException(e);
    }
    
  }
  
}
