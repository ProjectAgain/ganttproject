package net.projectagain.planner.core.ui.event;

import javafx.scene.Scene;
import net.projectagain.planner.core.event.impl.EventBase;

public class MainWindowCreated extends EventBase<Scene> implements UiEvent {
  public MainWindowCreated(Scene scene) {
    super(scene);
  }

  public Scene getScene() {
    return (Scene) getSource();
  }
}
