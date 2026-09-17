package cz.side.thing.sokoban.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

import lombok.Getter;

public class BoardState {
  
  private static final String CRLF = "\r\n";
  
  private static final String CRLF_REGEX = "[\\r\\n]+";
  
  private static final Map<Integer, FieldType> CHARS_TO_FIELDS = FieldType.getMap();
  
  private final List<List<Field>> fields;
  
  private final Collection<Field> crates;
  
  private final Collection<Field> targets;

  @Getter
  private final Field player;
  
  public BoardState(final List<List<Field>> fields) {
    this.fields = copyList(fields);
    this.crates = searchFor(FieldType.CRATE, FieldType.CRATE_ON_TARGET);
    this.targets = searchFor(FieldType.TARGET, FieldType.CRATE_ON_TARGET);
    final Collection<Field> players = searchFor(FieldType.PLAYER, FieldType.PLAYER_ON_TARGET);
    this.player = players.stream().findFirst()
        .orElseGet(() -> new Field(FieldType.PLAYER, new Point(0, 0)));
    
  }

  public static BoardState fromString(final String stringToParse) {
    return new BoardState(parse(stringToParse));
  }

  public List<List<Field>> getFields() {
    return copyList(fields);
  }
  
  public Collection<Field> getCrates() {
    return List.copyOf(crates);
  }
  
  public Collection<Field> getTargets() {
    return List.copyOf(targets);
  }

  public Field getAt(final Point coords) {
    if (fields.size() < coords.y) {
      return new Field(FieldType.VOID, coords);
    }
    
    final List<Field> row = fields.get(coords.y);
    if (row.size() < coords.x) {
      return new Field(FieldType.VOID, coords);
    }
    
    return row.get(coords.x);

  }

  public Collection<Field> searchFor(final FieldType... types) {

    final Collection<Field> ret = new TreeSet<>();

    final List<FieldType> typesList = List.of(types);

    for (int y = 0; y < fields.size(); y++) {
      final List<Field> row = fields.get(y);

      for (int x = 0; x < row.size(); x++) {
        final Field field = row.get(x);
        if (typesList.contains(field.type())) {
          ret.add(field);
        }
      }

    }

    return List.copyOf(ret);

  }
  
  public boolean isWon() {
    return Objects.equals(crates, targets);
  }
  
  @Override
  public String toString() {
    return fields.stream().map(it -> it.stream().map(Field::type).map(FieldType::getCode)
        .map(String::valueOf).collect(Collectors.joining())).collect(Collectors.joining(CRLF));
  }
  
  /*
   * private methods...
   */

  private static List<List<Field>> parse(final String stringToParse) {
    Objects.requireNonNull(stringToParse);
    final String[] rowsToParse = stringToParse.split(CRLF_REGEX);
    
    final List<List<Field>> fields = new ArrayList<>();
    
    for (int y = 0; y < rowsToParse.length; y++) {
      final String rowToParse = rowsToParse[y];
      
      final List<Field> row = new ArrayList<>();
      
      int x = 0;
      for (final int it : rowToParse.chars().toArray()) {

        if (!CHARS_TO_FIELDS.containsKey(it)) {
          throw new IllegalArgumentException(
              "Invalid char in map string - %d - %c".formatted(it, (char) it));
        }
        
        row.add(new Field(CHARS_TO_FIELDS.get(it), new Point(x++, y)));
      }
      
      fields.add(List.copyOf(row));
    }
    
    return copyList(fields);
  }
  
  private static List<List<Field>> copyList(final List<List<Field>> original) {
    final List<List<Field>> newList = new ArrayList<>();
    
    for (final List<Field> row : original) {
      newList.add(List.copyOf(row));
    }
    
    return List.copyOf(newList);
  }
  
  public record Field(FieldType type, Point coords) implements Comparable<Field> {
    
    @Override
    public int compareTo(final Field other) {
      return Comparator.comparing(Field::coords).compare(this, other);
    }
    
  }
  
  public record Point(int x, int y) implements Comparable<Point> {

    @Override
    public int compareTo(final Point other) {
      return Comparator.comparing(Point::x).thenComparing(Point::y).compare(this, other);
    }
    
  }
  
}
