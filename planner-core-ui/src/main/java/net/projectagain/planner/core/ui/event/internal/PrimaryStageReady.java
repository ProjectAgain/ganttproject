package net.projectagain.planner.core.ui.event.internal;

import javafx.stage.Stage;
import org.springframework.context.ApplicationEvent;

public class PrimaryStageReady extends ApplicationEvent {
  /**
   * Create a new {@code ApplicationEvent}.
   *
   * @param source the object on which the event initially occurred or with
   *               which the event is associated (never {@code null})
   */
  public PrimaryStageReady(Stage source) {
    super(source);
  }

  public Stage getPrimaryStage() {
    return (Stage) getSource();
  }
}
