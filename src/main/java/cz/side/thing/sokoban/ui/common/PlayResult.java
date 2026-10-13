package cz.side.thing.sokoban.ui.common;

/**
 * Outcome of playing a map, used by the main loop to pick the next map.
 */
public enum PlayResult {
  /** The player skipped to the next map without solving the current one. */
  NEXT_MAP,
  /** The player went back to the previous map. */
  PREVIOUS_MAP,
  /** The map was solved. */
  VICTORY;
}
