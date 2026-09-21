package cz.side.thing.sokoban.core;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FieldType {
  
  WALL("wall", 'W', false), FLOOR("floor", '.', false), VOID("void", ' ', false),
  CRATE("crate", 'C', false), CRATE_ON_TARGET("crate on finish", 'Q', true),
  TARGET("finish", 'X', true), PLAYER("player", 'P', false),
  PLAYER_ON_TARGET("player on target", 'T', true);
  
  @Getter
  final String type;
  
  @Getter
  final char code;

  @Getter
  final boolean onTarget;

  private int getCodeAsInt() {
    return code;
  }

  static Map<Integer, FieldType> getMap() {
    return Arrays.stream(values()).collect(Collectors.collectingAndThen(
        Collectors.toMap(FieldType::getCodeAsInt, Function.identity()), Map::copyOf));
  }
  
}
