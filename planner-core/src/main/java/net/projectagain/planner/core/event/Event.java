package net.projectagain.planner.core.event;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
public interface Event {
  default String id() {
    return this.getClass().getSimpleName();
  }
}
