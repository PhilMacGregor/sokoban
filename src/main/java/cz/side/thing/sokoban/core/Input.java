package cz.side.thing.sokoban.core;

import cz.side.thing.sokoban.core.BoardState.Point;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Movement command of the player.
 *
 * <p>
 * Each value carries a unit direction vector. The y axis grows downwards (row 0 is the top row
 * of the map), therefore {@link #UP} has {@code y = -1}.
 */
@RequiredArgsConstructor
@Getter
public enum Input {
  
  /** One row up. */
  UP(0, -1),
  /** One row down. */
  DOWN(0, 1),
  /** One column to the left. */
  LEFT(-1, 0),
  /** One column to the right. */
  RIGHT(1, 0);
  
  /** Unit vector added to the player's coordinates when moving. */
  final Point direction;

  Input(final int x, final int y) {
    this(new Point(x, y));
  }

}
