package net.projectagain.planner.core.event.impl;

import net.projectagain.planner.core.event.Event;
import org.springframework.context.ApplicationEvent;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
public abstract class EventBase<SOURCE> extends ApplicationEvent implements Event {
  private final String id;

  protected EventBase(SOURCE source) {
    super(source);
    this.id = this.getClass().getSimpleName();
  }

  protected EventBase(String id, SOURCE source) {
    super(source);
    this.id = id;
  }

  @Override
  public String id() {
    return id;
  }
}
