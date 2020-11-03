package net.projectagain.planner.core.ui.impl;

import javafx.scene.Scene;
import javafx.stage.Stage;
import net.projectagain.planner.core.ui.UiManager;
import net.projectagain.planner.core.ui.event.MainWindowCreated;
import net.projectagain.planner.core.ui.event.internal.PrimaryStageReady;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class UiManagerImpl implements UiManager {

  private Scene mainWindow = null;
  private Stage primaryStage = null;

  public Scene getMainWindow() {
    return mainWindow;
  }

  @Override
  public Stage getMainStage() {
    return primaryStage;
  }

  @Component
  class ReadyListener implements ApplicationListener<PrimaryStageReady> {

    @Override
    public void onApplicationEvent(PrimaryStageReady event) {
      primaryStage = event.getPrimaryStage();
    }
  }

  @Component
  class MainWindowCreatedListener implements ApplicationListener<MainWindowCreated> {

    @Override
    public void onApplicationEvent(MainWindowCreated event) {
      mainWindow = event.getScene();
    }
  }
}
