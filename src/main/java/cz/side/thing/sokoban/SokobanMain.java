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
 * Sample program for maven Java build. TODO: Change it to your needs.
 *
 * @author maven-archetype-generated
 *
 */
@Slf4j
public final class SokobanMain {
  
  private static final String MAPS_DIR_PROPERTY = "sokoban.maps.dir";
  
  private static final String MAPS_DIR = "maps/original";
  
  private static final List<String> MAPS_NAMES = new ArrayList<>();
  
  private static final Map<String, String> MAPS_RESOLVED = new HashMap<>();
  
  private static final Collection<String> MAPS_COMPLETED = new HashSet<>();
  
  /**
   * Sample main method.
   *
   * @param args program arguments
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
      
      int index = 0;
      
      while (index < MAPS_NAMES.size()) {
        
        final String name = MAPS_NAMES.get(index);
        final String content = MAPS_RESOLVED.get(name);
        
        final PlayResult result = playMap(name, content, MAPS_COMPLETED.contains(name));
        
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
  
  private static String findLowestIncompleteMap() {
    for (final String name : MAPS_NAMES) {
      if (!MAPS_COMPLETED.contains(name)) {
        return name;
      }
    }
    return StringUtils.EMPTY;
  }
  
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
