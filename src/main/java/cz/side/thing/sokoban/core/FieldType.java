package cz.side.thing.sokoban.core;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Type of a single board tile together with the character that represents it in a map file.
 *
 * <p>
 * Tiles that can be entered by the player or by a crate exist in two variants - a plain one and
 * one standing on a target (e.g. {@link #CRATE} and {@link #CRATE_ON_TARGET}). The
 * {@code onTarget} flag lets the game logic restore the target when the player or a crate
 * leaves the tile.
 */
@RequiredArgsConstructor
public enum FieldType {
  
  /** Impassable wall, map char {@code W}. */
  WALL("wall", 'W', false),
  /** Empty walkable floor, map char {@code .}. */
  FLOOR("floor", '.', false),
  /** Space outside the playing area, map char {@code ' '}; also returned for unknown coordinates. */
  VOID("void", ' ', false),
  /** Crate on plain floor, map char {@code C}. */
  CRATE("crate", 'C', false),
  /** Crate placed on a target, map char {@code Q}. */
  CRATE_ON_TARGET("crate on finish", 'Q', true),
  /** Empty target where a crate has to be pushed, map char {@code X}. */
  TARGET("finish", 'X', true),
  /** Player on plain floor, map char {@code P}. */
  PLAYER("player", 'P', false),
  /** Player standing on a target, map char {@code T}. */
  PLAYER_ON_TARGET("player on target", 'T', true);
  
  /** Human readable name of the tile type. */
  @Getter
  final String type;
  
  /** Character representing the tile in a map file. */
  @Getter
  final char code;

  /** Whether the tile is (or contains) a target. */
  @Getter
  final boolean onTarget;

  private int getCodeAsInt() {
    return code;
  }

  /**
   * Builds a lookup table from map characters to tile types used when parsing a map.
   *
   * <p>
   * The key is an {@code int} so that it can be matched directly against
   * {@link String#chars()}.
   *
   * @return immutable map of character codes to tile types
   */
  static Map<Integer, FieldType> getMap() {
    return Arrays.stream(values()).collect(Collectors.collectingAndThen(
        Collectors.toMap(FieldType::getCodeAsInt, Function.identity()), Map::copyOf));
  }
  
}
