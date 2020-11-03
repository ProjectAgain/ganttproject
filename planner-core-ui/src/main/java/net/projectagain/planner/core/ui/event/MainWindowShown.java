package net.projectagain.planner.core.ui.event;

import javafx.scene.Scene;
import net.projectagain.planner.core.event.impl.EventBase;

public class MainWindowShown extends EventBase<Scene> {
  public MainWindowShown(Scene source) {
    super(source);
  }
}
