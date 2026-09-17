package cz.side.thing.sokoban.core;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FieldType {

  WALL("wall", 'W'), FLOOR("floor", '.'), VOID("void", ' '), CRATE("crate", 'C'),
  CRATE_ON_TARGET("crate on finish", 'Q'), TARGET("finish", 'X'), PLAYER("player", 'P'),
  PLAYER_ON_TARGET("player on target", 'T');

  @Getter
  final String type;

  @Getter
  final char code;
  
  private int getCodeAsInt() {
    return code;
  }
  
  static Map<Integer, FieldType> getMap() {
    return Arrays.stream(values()).collect(Collectors.collectingAndThen(
        Collectors.toMap(FieldType::getCodeAsInt, Function.identity()), Map::copyOf));
  }

}
