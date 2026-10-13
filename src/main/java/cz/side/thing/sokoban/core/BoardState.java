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

/**
 * Immutable snapshot of the game board.
 *
 * <p>
 * The board is parsed from a map string where each character is one
 * {@link FieldType} code and rows are separated by line breaks or by {@code |}.
 * Coordinates start at {@code [0, 0]} in the top left corner; any coordinate
 * not present in the map is reported as {@link FieldType#VOID}.
 *
 * <p>
 * Positions of crates, targets and the player are derived from the fields when
 * the instance is created. Changes are made via {@link #setField(Field...)},
 * which returns a new instance.
 */
@RequiredArgsConstructor
public class BoardState {
  
  private static final String CRLF = "\r\n";
  
  /** Row separator in a map string - any sequence of CR, LF or {@code |}. */
  private static final String CRLF_REGEX = "[\\r\\n|]+";
  
  private static final Map<Integer, FieldType> CHARS_TO_FIELDS = FieldType.getMap();
  
  /** All fields of the board. */
  @Getter
  private final Fields fields;
  
  private final Collection<Field> crates;
  
  private final Collection<Field> targets;
  
  /** Field the player stands on. */
  @Getter
  private final Field player;
  
  /**
   * Creates a board from the given fields and derives crates, targets and the
   * player position.
   *
   * <p>
   * When the map contains no player, the field at {@code [0, 0]} is used instead;
   * such a board is reported as not playable by {@link #isPlayable()}.
   *
   * @param fields fields of the board
   */
  public BoardState(final Fields fields) {
    this.fields = fields;
    this.crates = searchFor(FieldType.CRATE, FieldType.CRATE_ON_TARGET);
    this.targets = searchFor(FieldType.TARGET, FieldType.CRATE_ON_TARGET);
    final Collection<Field> players = searchFor(FieldType.PLAYER, FieldType.PLAYER_ON_TARGET);
    this.player = players.stream().findFirst().orElseGet(() -> getAt(0, 0));
    
  }
  
  /**
   * Parses a board from a map string.
   *
   * @param stringToParse map string, see the class description for the format
   * @return parsed board
   * @throws IllegalArgumentException when the string contains an unknown
   *                                  character
   * @throws NullPointerException     when the string is {@code null}
   */
  public static BoardState fromString(final String stringToParse) {
    return new BoardState(parse(stringToParse));
  }
  
  /**
   * Returns a new board with the given fields replaced. Fields are matched by
   * their coordinates; this instance is left unchanged.
   *
   * @param changes fields to put on the board
   * @return new board containing the changes
   */
  public BoardState setField(final Field... changes) {
    final Fields newFields = fields.set(changes);
    return new BoardState(newFields);
  }
  
  /**
   * Returns all crates, both on plain floor and on targets.
   *
   * @return immutable copy of the crate fields
   */
  public Collection<Field> getCrates() {
    return List.copyOf(crates);
  }
  
  /**
   * Returns all targets, both empty and covered by a crate.
   *
   * @return immutable copy of the target fields
   */
  public Collection<Field> getTargets() {
    return List.copyOf(targets);
  }
  
  /**
   * Returns the field at the given coordinates.
   *
   * @param coords coordinates of the field
   * @return field at the coordinates, or a {@link FieldType#VOID} field when
   *         outside the map
   */
  public Field getAt(final Point coords) {
    return fields.get(coords);
  }
  
  /**
   * Returns the field at the given coordinates.
   *
   * @param x column index
   * @param y row index
   * @return field at the coordinates, or a {@link FieldType#VOID} field when
   *         outside the map
   */
  public Field getAt(final int x, final int y) {
    return fields.get(x, y);
  }
  
  /**
   * Returns the field adjacent to {@code origin} in the given direction.
   *
   * @param origin    starting field
   * @param direction direction vector, typically the direction of an
   *                  {@link Input}
   * @return neighboring field
   */
  public Field getNeighbor(final Field origin, final Point direction) {
    return getAt(origin.coords().transpose(direction));
  }
  
  /**
   * Finds all fields of the given types.
   *
   * @param types types to search for
   * @return immutable list of matching fields
   */
  public Collection<Field> searchFor(final FieldType... types) {
    return fields.getFieldsByTypes(types);
  }
  
  /**
   * Checks whether the game is won, i.e. every target is covered by a crate.
   *
   * <p>
   * {@link Field} equality includes the type, so the crate and target collections
   * are equal only when all of them are {@link FieldType#CRATE_ON_TARGET} fields.
   *
   * @return {@code true} when all crates are on targets
   */
  public boolean isWon() {
    return Objects.equals(crates, targets);
  }
  
  /**
   * Checks whether the board contains a player.
   *
   * @return {@code true} when a player was found on the board
   */
  public boolean isPlayable() {
    final FieldType playerType = this.getPlayer().type();
    return playerType == FieldType.PLAYER || playerType == FieldType.PLAYER_ON_TARGET;
  }
  
  /**
   * Returns the board encoded with {@link FieldType} code characters, row by row.
   * The result is also used for {@link #equals(Object)} and {@link #hashCode()}.
   */
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
      str.append("|");
    }
    
    return str.toString();
  }
  
  /*
   * private methods...
   */
  
  /**
   * Converts a map string to fields. Each character becomes one field; its
   * position in the row is the x coordinate and the row index is the y
   * coordinate.
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
  
  /**
   * Hash code consistent with {@link #equals(Object)}, computed from
   * {@link #toString()}.
   */
  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }
  
  /**
   * Two boards are equal when their {@link #toString()} representations are
   * equal.
   */
  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    
    return Objects.equals(this.toString(), obj.toString());
  }
  
  /**
   * Single tile of the board.
   *
   * <p>
   * Fields are ordered by their coordinates (row by row, see
   * {@link Point#compareTo(Point)}).
   *
   * @param type   type of the tile
   * @param coords position of the tile
   */
  public record Field(FieldType type, Point coords) implements Comparable<Field> {
    
    @Override
    public int compareTo(final Field other) {
      return Comparator.comparing(Field::coords).compare(this, other);
    }
    
  }
  
  /**
   * Board coordinates or a direction vector.
   *
   * <p>
   * Points are ordered row by row - first by {@code y}, then by {@code x}.
   *
   * @param x column index (grows to the right)
   * @param y row index (grows downwards)
   */
  public record Point(int x, int y) implements Comparable<Point> {
    
    /**
     * Returns this point shifted by the given offset.
     *
     * @param x offset in columns
     * @param y offset in rows
     * @return shifted point
     */
    public Point transpose(final int x, final int y) {
      return new Point(this.x + x, this.y + y);
    }
    
    /**
     * Returns this point shifted by the given vector.
     *
     * @param other offset vector
     * @return shifted point
     */
    public Point transpose(final Point other) {
      return transpose(other.x, other.y);
    }
    
    @Override
    public int compareTo(final Point other) {
      return Comparator.comparing(Point::y).thenComparing(Point::x).compare(this, other);
    }
    
  }
  
  /**
   * Immutable container of board fields, stored as {@code Map<y, Map<x, Field>>}.
   *
   * <p>
   * The map is defensively copied both on creation and when read, so instances
   * can be shared freely. Iteration returns the board row by row, sorted by
   * coordinates.
   *
   * @param fields fields indexed by row and column
   */
  public record Fields(Map<Integer, Map<Integer, Field>> fields) implements Iterable<List<Field>> {
    
    /**
     * Creates the container from a defensive immutable copy of the given map.
     */
    public Fields {
      fields = copyFields(fields);
    }
    
    /**
     * Returns a container without any fields.
     *
     * @return empty container
     */
    public static Fields empty() {
      return new Fields(Map.of());
    }
    
    /**
     * Returns a copy of the underlying map.
     *
     * @return fields indexed by row and column
     */
    public Map<Integer, Map<Integer, Field>> fields() {
      return copyFields(fields);
    }
    
    /**
     * Returns the field at the given coordinates.
     *
     * @param x column index
     * @param y row index
     * @return the stored field, or a new {@link FieldType#VOID} field when there is
     *         none
     */
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
    
    /**
     * Returns the field at the given coordinates.
     *
     * @param coords coordinates of the field
     * @return the stored field, or a new {@link FieldType#VOID} field when there is
     *         none
     */
    public Field get(final Point coords) {
      return get(coords.x(), coords.y());
    }
    
    /**
     * Returns a new container with the given fields added or replaced (matched by
     * their coordinates). This instance is left unchanged.
     *
     * @param fields fields to put into the container
     * @return new container
     */
    public Fields set(final Collection<Field> fields) {
      // Mutable working copy of the current rows.
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
    
    /**
     * Returns a new container with the given fields added or replaced.
     *
     * @param fields fields to put into the container
     * @return new container
     * @see #set(Collection)
     */
    public Fields set(final Field... fields) {
      return set(Arrays.asList(fields));
    }
    
    /**
     * Returns all stored fields in no particular order.
     *
     * @return immutable list of fields
     */
    public List<Field> getAllFields() {
      final Collection<Field> fields = new ArrayList<>();
      for (final Map<Integer, Field> row : fields().values()) {
        fields.addAll(row.values());
      }
      return List.copyOf(fields);
    }
    
    /**
     * Returns all stored fields of the given types in no particular order.
     *
     * @param types types to search for
     * @return immutable list of matching fields
     */
    public List<Field> getFieldsByTypes(final FieldType... types) {
      
      final List<FieldType> typesList = List.of(types);
      
      final Collection<Field> fields = new ArrayList<>();
      
      for (final Map<Integer, Field> row : fields().values()) {
        row.values().stream().filter(f -> typesList.contains(f.type())).forEach(fields::add);
      }
      
      return List.copyOf(fields);
    }
    
    /**
     * Creates a deep immutable copy of a field map (both the outer map and every
     * row).
     *
     * @param original map to copy
     * @return immutable copy
     */
    public static Map<Integer, Map<Integer, Field>> copyFields(
        final Map<Integer, Map<Integer, Field>> original) {
      
      final Map<Integer, Map<Integer, Field>> newMap = new HashMap<>();
      
      for (final Entry<Integer, Map<Integer, Field>> row : original.entrySet()) {
        newMap.put(row.getKey(), Map.copyOf(row.getValue()));
      }
      
      return Map.copyOf(newMap);
    }
    
    /**
     * Iterates over the board row by row, from the top row down. Each row is sorted
     * by the x coordinate; gaps between stored fields are filled with
     * {@link FieldType#VOID} fields.
     */
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
          continue;
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
