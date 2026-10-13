package cz.side.thing.sokoban.ui.common;

/**
 * User interface able to play a single map.
 */
public interface Ui {
  
  /**
   * Runs the game loop for the map until the player wins or leaves the map.
   *
   * @return how the map was finished
   * @throws UiException when the interface fails; {@link IncompatibleUIException} when it
   *         cannot run in the current environment
   */
  PlayResult start() throws UiException;
  
}