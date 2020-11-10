package net.projectagain.planner.core.ui.impl;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.stage.Stage;
import net.projectagain.planner.core.ui.event.MainWindowCreated;
import net.projectagain.planner.core.ui.event.MainWindowShown;
import net.projectagain.planner.core.ui.event.internal.PrimaryStageReady;
import net.projectagain.planner.core.ui.menu.MenuManager;
import net.projectagain.planner.core.ui.theme.UiTheme;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

import static org.slf4j.LoggerFactory.getLogger;

@Component
public class UiCreator {
  final String applicationTitle;
  final ApplicationContext appContext;
  private final MenuManager menuManager;
  private final Logger log = getLogger(getClass());
  private final UiTheme theme;

  public UiCreator(
    @Value("${spring.application.name}") String applicationTitle,
    MenuManager menuManager,
    UiTheme currentTheme,
    ApplicationContext appContext
  ) {
    this.applicationTitle = applicationTitle;
    this.menuManager = menuManager;
    this.appContext = appContext;
    this.theme = currentTheme;
  }

  private void createMainScene(Stage primaryStage) {
    try {
      Resource fxml = theme.getResource(UiTheme.IDs.MAINFXML);
      URL url = fxml.getURL();
      FXMLLoader fxmlLoader = new FXMLLoader(url);
      fxmlLoader.setControllerFactory(appContext::getBean);
      Parent root = fxmlLoader.load();
      Scene scene = new Scene(root, 600, 600);
      primaryStage.setScene(scene);
      primaryStage.setTitle(applicationTitle);
      appContext.publishEvent(new MainWindowCreated(scene));
      MenuBar menuBar = (MenuBar) root.lookup("#" + UiTheme.FxIDSelector.Menu.Main.MENUBAR);
      assert menuBar != null;
      menuManager.createMainMenu(menuBar);

      primaryStage.show();
      appContext.publishEvent(new MainWindowShown(scene));
    } catch (IOException e) {
      log.error("Exception", e);
    }
  }

  @Component
  class ReadyListener implements ApplicationListener<PrimaryStageReady> {

    @Override
    public void onApplicationEvent(PrimaryStageReady event) {
      createMainScene(event.getPrimaryStage());
    }
  }
}
