package cz.side.thing.sokoban.core;

public class SokobanProcessor {
  
  public BoardState initialize(final String str) {

    return BoardState.fromString(str);
  }
  
  public BoardState play(final BoardState initial, final Input input) {
    
    return initial;

  }
  
}
