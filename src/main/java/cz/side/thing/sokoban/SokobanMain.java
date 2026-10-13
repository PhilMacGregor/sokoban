package cz.side.thing.sokoban;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import cz.side.thing.sokoban.ui.cmd.CmdUi;
import cz.side.thing.sokoban.ui.common.IncompatibleUIException;
import cz.side.thing.sokoban.ui.common.PlayResult;
import cz.side.thing.sokoban.ui.common.Ui;
import cz.side.thing.sokoban.ui.common.UiException;
import cz.side.thing.sokoban.ui.debug.DebugUi;
import lombok.extern.slf4j.Slf4j;

/**
 * Entry point of the console Sokoban game.
 *
 * <p>
 * Loads all map files from the maps directory (see {@link #resolveMapsPath()}), sorted by file
 * name, and lets the player go through them. Each map is played in {@link CmdUi}; when the
 * Windows console is not available, {@link DebugUi} is used instead. The game ends once every
 * map has been solved.
 *
 * @author MacGregor
 */
@Slf4j
public final class SokobanMain {
  
  /** System property that overrides the maps directory. */
  private static final String MAPS_DIR_PROPERTY = "sokoban.maps.dir";
  
  /** Default maps directory, relative to the working directory or the project root. */
  private static final String MAPS_DIR = "maps/original";
  
  /** Map names (file names) in play order. */
  private static final List<String> MAPS_NAMES = new ArrayList<>();
  
  /** Map contents keyed by map name. */
  private static final Map<String, String> MAPS_RESOLVED = new HashMap<>();
  
  /** Names of maps solved during this run. */
  private static final Collection<String> MAPS_COMPLETED = new HashSet<>();
  
  /**
   * Loads the maps and runs the game loop until all maps are solved. Exits with status
   * {@code 1} on any error.
   *
   * @param args program arguments (not used)
   */
  public static void main(final String[] args) {
    try {
      
      final Path mapsPath = resolveMapsPath();
      
      try (final Stream<Path> maps = Files.list(mapsPath)) {
        maps.filter(Files::isRegularFile).sorted().forEachOrdered(it -> {
          try {
            
            final String filename = getFilename(it);
            if (StringUtils.isEmpty(filename)) {
              throw new IllegalArgumentException();
            }
            
            MAPS_NAMES.add(filename);
            MAPS_RESOLVED.put(filename, Files.readString(it));
            
          } catch (final IOException e) {
            throw new IllegalArgumentException(e);
          }
        });
      }
      
      // Index of the map currently being played in MAPS_NAMES.
      int index = 0;
      
      while (index < MAPS_NAMES.size()) {
        
        final String name = MAPS_NAMES.get(index);
        final String content = MAPS_RESOLVED.get(name);
        
        final PlayResult result = playMap(name, content, MAPS_COMPLETED.contains(name));
        
        // PREVIOUS_MAP on the first map stays on the first map.
        switch (result) {
          case NEXT_MAP -> index++;
          case PREVIOUS_MAP -> index = index == 0 ? index : index - 1;
          case VICTORY -> {
            MAPS_COMPLETED.add(name);
            index++;
          }
        }
        
        if (MAPS_COMPLETED.size() == MAPS_NAMES.size()) {
          break;
        }
        
        // After the last map, continue with the first map that has not been solved yet.
        if (index >= MAPS_NAMES.size()) {
          final String lowesIncomplete = findLowestIncompleteMap();
          index = MAPS_NAMES.indexOf(lowesIncomplete);
        }
        
      }
      
      System.out.printf("------------------------------------%n");
      System.out.printf("ALL MAPS COMPLETE!%n");
      System.out.printf("------------------------------------%n");
      
    } catch (final Throwable ex) {
      log.error("The application ended with error", ex);
      System.exit(1);
    }
  }
  
  /**
   * Finds the first map (in play order) that has not been solved yet.
   *
   * @return map name, or an empty string when all maps are solved
   */
  private static String findLowestIncompleteMap() {
    for (final String name : MAPS_NAMES) {
      if (!MAPS_COMPLETED.contains(name)) {
        return name;
      }
    }
    return StringUtils.EMPTY;
  }
  
  /**
   * Returns the file name of a path.
   *
   * @param path path to take the name from
   * @return file name, or an empty string when the path is {@code null} or has no name
   */
  private static String getFilename(final Path path) {
    return Optional.ofNullable(path).map(Path::getFileName).map(Path::toString)
        .orElse(StringUtils.EMPTY);
  }
  
  /**
   * Finds the maps directory. Order: system property {@value #MAPS_DIR_PROPERTY},
   * then {@value #MAPS_DIR} relative to the working directory, then
   * {@value #MAPS_DIR} in any parent of the location the classes were loaded from
   * (e.g. project root when running from IDE, where the working directory may be
   * the workspace root).
   *
   * @return existing maps directory
   * @throws IllegalStateException when no maps directory is found, or the directory given by
   *         the system property does not exist
   * @throws URISyntaxException when the class location cannot be converted to a URI
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
    
    throw new IllegalStateException("Maps directory not found: " + workingDirMaps.toAbsolutePath());
  }
  
  /**
   * Plays one map, preferring {@link CmdUi} and falling back to {@link DebugUi} when the
   * Windows console is not available.
   *
   * @param name map name
   * @param content map string
   * @param isCompleted whether the map was already solved in this run
   * @return how the map was finished
   * @throws IllegalStateException when the interface fails
   */
  private static PlayResult playMap(final String name, final String content,
      final boolean isCompleted) {
    System.out.printf("------------------------------------%n");
    System.out.printf("MAP %s%n", name);
    System.out.printf("------------------------------------%n");
    
    try {
      try {
        final CmdUi ui = new CmdUi(name, content, isCompleted);
        return ui.start();
      } catch (final IncompatibleUIException ex) {
        log.error("Unable to load console interface. Running in fallback mode...", ex);
        final Ui ui = new DebugUi(content);
        return ui.start();
      }
    } catch (final UiException e) {
      throw new IllegalStateException(e);
    }
    
  }
  
}
