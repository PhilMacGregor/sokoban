package cz.side.thing.sokoban.core;

import cz.side.thing.sokoban.core.BoardState.Point;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Input {

  UP(0, -1), DOWN(0, 1), LEFT(1, 0), RIGHT(-1, 0);

  final Point direction;
  
  Input(final int x, final int y) {
    this(new Point(x, y));
  }
  
}
