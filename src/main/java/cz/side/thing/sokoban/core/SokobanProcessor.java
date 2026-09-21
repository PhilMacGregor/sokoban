package cz.side.thing.sokoban.core;

import java.util.HashMap;
import java.util.Map;

import cz.side.thing.sokoban.core.BoardState.Field;
import cz.side.thing.sokoban.core.BoardState.Point;

public class SokobanProcessor {

  private static final int PLAYER_STRENGTH = 1;
  
  public BoardState initialize(final String str) {

    return BoardState.fromString(str);
  }
  
  public BoardState play(final BoardState initial, final Input input) {

    final Field player = initial.getPlayer();
    
    if (!canMove(initial, player, input.getDirection(), PLAYER_STRENGTH)) {
      return initial;
    }

    final Map<Point, Field> updates = move(initial, player, input.getDirection(), PLAYER_STRENGTH);
    
    return initial.setField(updates.values().toArray(new Field[0]));
    
  }
  
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
    
    final FieldType newTarget = switch (origin.type()) {
      case PLAYER, PLAYER_ON_TARGET ->
        target.type().isOnTarget() ? FieldType.PLAYER_ON_TARGET : FieldType.PLAYER;
      case CRATE, CRATE_ON_TARGET ->
        target.type().isOnTarget() ? FieldType.CRATE_ON_TARGET : FieldType.CRATE;
      default -> origin.type();
    };
    
    final FieldType newOrigin = origin.type().isOnTarget() ? FieldType.TARGET : FieldType.FLOOR;
    
    updates.put(origin.coords(), new Field(newOrigin, origin.coords()));
    updates.put(target.coords(), new Field(newTarget, target.coords()));
    
    return Map.copyOf(updates);
    
  }

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
