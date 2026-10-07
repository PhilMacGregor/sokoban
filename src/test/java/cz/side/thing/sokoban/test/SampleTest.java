package cz.side.thing.sokoban.test;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

/**
 * Sample testing class.
 *
 * @author maven-archetype-generated
 *
 */
@Slf4j
public class SampleTest {
  
  private static final Pattern MAP_DELIMITER_REGEX = Pattern.compile("(\\r\\n){2,}");
  
  @Test
  public void createMaps() {
    
    // final Path mapsPath = Paths.get("src/test/resources/maps.txt");
    // try {
    // final String mapsContent = Files.readString(mapsPath,
    // StandardCharsets.UTF_8);
    
    // int i = 0;
    // final String[] maps = MAP_DELIMITER_REGEX.split(mapsContent);
    // for (final String map : maps) {
    
    // final Path mapFile = Paths.get("src/main/resources/maps/original",
    // "%d.map".formatted(++i));
    // Files.writeString(mapFile, map);
    
    // log.info("Map {}:\n{}", i, map);
    // }
    
    // } catch (final IOException e) {
    // log.error("IO error while reading maps file: {}", e.getMessage(), e);
    // }
    
  }
  
}
