package cz.side.thing.sokoban.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoardState {
  
  private static final String CRLF = "\r\n";
  
  private static final String CRLF_REGEX = "[\\r\\n]+";
  
  private static final Map<Integer, FieldType> CHARS_TO_FIELDS = FieldType.getMap();
  
  @Getter
  private final Fields fields;
  
  private final Collection<Field> crates;
  
  private final Collection<Field> targets;
  
  @Getter
  private final Field player;
  
  public BoardState(final Fields fields) {
    this.fields = fields;
    this.crates = searchFor(FieldType.CRATE, FieldType.CRATE_ON_TARGET);
    this.targets = searchFor(FieldType.TARGET, FieldType.CRATE_ON_TARGET);
    final Collection<Field> players = searchFor(FieldType.PLAYER, FieldType.PLAYER_ON_TARGET);
    this.player = players.stream().findFirst().orElseGet(() -> getAt(0, 0));
    
  }
  
  public static BoardState fromString(final String stringToParse) {
    return new BoardState(parse(stringToParse));
  }
  
  public BoardState setField(final Field... changes) {
    final Fields newFields = fields.set(changes);
    return new BoardState(newFields);
  }
  
  public Collection<Field> getCrates() {
    return List.copyOf(crates);
  }
  
  public Collection<Field> getTargets() {
    return List.copyOf(targets);
  }
  
  public Field getAt(final Point coords) {
    return fields.get(coords);
  }
  
  public Field getAt(final int x, final int y) {
    return fields.get(x, y);
  }
  
  public Collection<Field> searchFor(final FieldType... types) {
    return fields.getFieldsByTypes(types);
  }
  
  public boolean isWon() {
    return Objects.equals(crates, targets);
  }
  
  public boolean isPlayable() {
    final FieldType playerType = this.getPlayer().type();
    return playerType == FieldType.PLAYER || playerType == FieldType.PLAYER_ON_TARGET;
  }
  
  @Override
  public String toString() {
    
    final TreeMap<Integer, Map<Integer, Field>> sorted = new TreeMap<>(fields.fields());
    
    final StringBuilder str = new StringBuilder();
    
    for (int y = sorted.firstKey(); y <= sorted.lastKey(); y++) {
      Collection<Field> row;
      if (!sorted.containsKey(y)) {
        str.append(CRLF);
        continue;
      }
      row = sorted.get(y).entrySet().stream().sorted(Comparator.comparing(Entry::getKey))
          .map(Entry::getValue).toList();
      
      str.append(row.stream().map(Field::type).map(FieldType::getCode).map(String::valueOf)
          .collect(Collectors.joining()));
    }
    
    return str.toString();
  }
  
  /*
   * private methods...
   */
  
  private static Fields parse(final String stringToParse) {
    Objects.requireNonNull(stringToParse);
    final String[] rowsToParse = stringToParse.split(CRLF_REGEX);
    
    final Collection<Field> fields = new ArrayList<>();

    for (int y = 0; y < rowsToParse.length; y++) {
      final String rowToParse = rowsToParse[y];
      
      int x = 0;
      for (final int it : rowToParse.chars().toArray()) {
        
        if (!CHARS_TO_FIELDS.containsKey(it)) {
          throw new IllegalArgumentException(
              "Invalid char in map string - %d - %c".formatted(it, (char) it));
        }
        
        fields.add(new Field(CHARS_TO_FIELDS.get(it), new Point(x++, y)));
      }

    }
    
    return Fields.empty().set(fields);
  }
  
  public record Field(FieldType type, Point coords) implements Comparable<Field> {
    
    @Override
    public int compareTo(final Field other) {
      return Comparator.comparing(Field::coords).compare(this, other);
    }
    
  }
  
  public record Point(int x, int y) implements Comparable<Point> {
    
    public Point transpose(final int x, final int y) {
      return new Point(this.x + x, this.y + y);
    }
    
    public Point transpose(final Point other) {
      return transpose(this.x + other.x, this.y + other.y);
    }
    
    @Override
    public int compareTo(final Point other) {
      return Comparator.comparing(Point::y).thenComparing(Point::x).compare(this, other);
    }
    
  }
  
  public record Fields(Map<Integer, Map<Integer, Field>> fields) implements Iterable<List<Field>> {
    
    public Fields {
      fields = copyFields(fields);
    }
    
    public static Fields empty() {
      return new Fields(Map.of());
    }
    
    public Field get(final int x, final int y) {
      if (!fields.containsKey(y)) {
        return new Field(FieldType.VOID, new Point(x, y));
      }
      final Map<Integer, Field> row = fields.get(y);
      if (!row.containsKey(x)) {
        return new Field(FieldType.VOID, new Point(x, y));
      }
      return row.get(x);
    }
    
    public Field get(final Point coords) {
      return get(coords.x(), coords.y());
    }
    
    public Fields set(final Collection<Field> fields) {
      final Map<Integer, Map<Integer, Field>> newFields = new HashMap<>();
      
      for (final Entry<Integer, Map<Integer, Field>> row : fields().entrySet()) {
        newFields.put(row.getKey(), new HashMap<>(row.getValue()));
      }
      
      final Map<Integer, List<Field>> rowsToEdit = fields.stream()
          .collect(Collectors.groupingBy(f -> f.coords().y()));
      
      for (final Entry<Integer, List<Field>> e : rowsToEdit.entrySet()) {
        final int y = e.getKey();
        
        final Map<Integer, Field> row;
        if (!newFields.containsKey(y)) {
          row = new HashMap<>();
        } else {
          row = newFields.get(y);
        }
        
        e.getValue().forEach(it -> row.put(it.coords().x(), it));
        
        newFields.put(y, row);
        
      }
      
      return new Fields(newFields);
    }
    
    public Fields set(final Field... fields) {
      return set(Arrays.asList(fields));
    }
    
    public List<Field> getAllFields() {
      final Collection<Field> fields = new ArrayList<>();
      for (final Map<Integer, Field> row : fields().values()) {
        fields.addAll(row.values());
      }
      return List.copyOf(fields);
    }
    
    public List<Field> getFieldsByTypes(final FieldType... types) {
      
      final List<FieldType> typesList = List.of(types);
      
      final Collection<Field> fields = new ArrayList<>();
      
      for (final Map<Integer, Field> row : fields().values()) {
        row.values().stream().filter(f -> typesList.contains(f.type())).forEach(fields::add);
      }
      
      return List.copyOf(fields);
    }
    
    public static Map<Integer, Map<Integer, Field>> copyFields(
        final Map<Integer, Map<Integer, Field>> original) {

      final Map<Integer, Map<Integer, Field>> newMap = new HashMap<>();

      for (final Entry<Integer, Map<Integer, Field>> row : original.entrySet()) {
        newMap.put(row.getKey(), Map.copyOf(row.getValue()));
      }
      
      return Map.copyOf(newMap);
    }
    
    @Override
    public Iterator<List<Field>> iterator() {
      final List<List<Field>> ret = new ArrayList<>();

      final TreeMap<Integer, Map<Integer, Field>> sortedY = new TreeMap<>(fields());
      if (sortedY.isEmpty()) {
        return List.copyOf(ret).iterator();
      }

      for (int y = sortedY.firstKey(); y <= sortedY.lastKey(); y++) {

        if (!sortedY.containsKey(y)) {
          ret.add(List.of());
        }
        
        final TreeMap<Integer, Field> sortedX = new TreeMap<>(sortedY.get(y));
        if (sortedX.isEmpty()) {
          ret.add(List.of());
          continue;
        }

        final List<Field> row = new ArrayList<>();
        
        for (int x = sortedX.firstKey(); x <= sortedX.lastKey(); x++) {

          if (!sortedX.containsKey(x)) {
            row.add(new Field(FieldType.VOID, new Point(x, y)));
          }

          row.add(sortedX.get(x));

        }
        
        ret.add(List.copyOf(row));

      }

      return List.copyOf(ret).iterator();
    }
    
  }
  
}
