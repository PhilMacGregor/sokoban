package cz.side.thing.sokoban.core;

import java.util.HashMap;
import java.util.Map;

import cz.side.thing.sokoban.core.BoardState.Field;
import cz.side.thing.sokoban.core.BoardState.Point;

/**
 * Stateless game engine. Creates the initial board and computes the board that results from a
 * player's move.
 *
 * <p>
 * The processor never mutates a {@link BoardState}; every move produces a new instance, which
 * allows the UI to keep previous states for undo.
 */
public class SokobanProcessor {

  /**
   * Number of crates the player is able to push in a row. With {@code 1} the player can push a
   * single crate, but not a crate standing behind another crate.
   */
  private static final int PLAYER_STRENGTH = 1;
  
  /**
   * Creates the initial board from a map string.
   *
   * @param str map in the format described in {@link BoardState}
   * @return parsed board
   * @throws IllegalArgumentException when the map contains an unknown character
   */
  public BoardState initialize(final String str) {

    return BoardState.fromString(str);
  }
  
  /**
   * Applies one player move.
   *
   * @param initial board before the move
   * @param input direction of the move
   * @return new board after the move, or {@code initial} itself when the move is not possible
   *         (wall, void or a crate that cannot be pushed)
   */
  public BoardState play(final BoardState initial, final Input input) {

    final Field player = initial.getPlayer();
    
    // Validate the whole chain (player + pushed crates) first, so that move() can assume
    // every step succeeds and does not have to roll anything back.
    if (!canMove(initial, player, input.getDirection(), PLAYER_STRENGTH)) {
      return initial;
    }

    final Map<Point, Field> updates = move(initial, player, input.getDirection(), PLAYER_STRENGTH);
    
    return initial.setField(updates.values().toArray(new Field[0]));
    
  }
  
  /**
   * Computes the fields changed by moving {@code origin} one step in {@code direction}.
   *
   * <p>
   * When the target field holds a crate, the crate is moved first (recursively, with the
   * strength decreased by one). The changes of the deeper levels are collected first and then
   * overwritten by the changes of this level, so the field the crate left is correctly occupied
   * by the object pushing it.
   *
   * @param board current board
   * @param origin field holding the object (player or crate) being moved
   * @param direction direction of the move
   * @param strength remaining number of crates that can still be pushed
   * @return changed fields keyed by their coordinates
   */
  private Map<Point, Field> move(final BoardState board, final Field origin, final Point direction,
      final int strength) {
    
    if (strength < 0) {
      return Map.of();
    }
    
    final Field target = board.getNeighbor(origin, direction);
    
    final Map<Point, Field> updates = new HashMap<>();
    
    if (target.type() == FieldType.CRATE || target.type() == FieldType.CRATE_ON_TARGET) {
      updates.putAll(move(board, target, direction, strength - 1));
    }
    
    // The moved object keeps its kind, but takes over the "on target" flag of the new field.
    final FieldType newTarget = switch (origin.type()) {
      case PLAYER, PLAYER_ON_TARGET ->
        target.type().isOnTarget() ? FieldType.PLAYER_ON_TARGET : FieldType.PLAYER;
      case CRATE, CRATE_ON_TARGET ->
        target.type().isOnTarget() ? FieldType.CRATE_ON_TARGET : FieldType.CRATE;
      default -> origin.type();
    };
    
    // The left field becomes empty again; a target underneath is restored.
    final FieldType newOrigin = origin.type().isOnTarget() ? FieldType.TARGET : FieldType.FLOOR;
    
    updates.put(origin.coords(), new Field(newOrigin, origin.coords()));
    updates.put(target.coords(), new Field(newTarget, target.coords()));
    
    return Map.copyOf(updates);
    
  }

  /**
   * Checks whether the object on {@code origin} can move one step in {@code direction}.
   *
   * <p>
   * A move into a crate is possible only if the crate itself can move further and there is
   * enough strength left to push it.
   *
   * @param board current board
   * @param origin field holding the object (player or crate) being moved
   * @param direction direction of the move
   * @param strength remaining number of crates that can still be pushed
   * @return {@code true} when the move is allowed
   */
  private boolean canMove(final BoardState board, final Field origin, final Point direction,
      final int strength) {

    if (strength < 0) {
      return false;
    }

    final Field target = board.getNeighbor(origin, direction);
    
    return switch (target.type()) {
      case WALL, VOID -> false;
      case FLOOR, TARGET -> true;
      case CRATE, CRATE_ON_TARGET -> canMove(board, target, direction, strength - 1);
      default -> false;
    };
    
  }
  
}
