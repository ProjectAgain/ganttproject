package net.projectagain.planner.core.ui.impl;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.projectagain.planner.core.ui.event.MainWindowCreated;
import net.projectagain.planner.core.ui.event.MainWindowShown;
import net.projectagain.planner.core.ui.event.internal.PrimaryStageReady;
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
  private final Logger log = getLogger(getClass());
  private final Resource fxml;

  public UiCreator(
    @Value("${spring.application.name}") String applicationTitle,
    @Value("${classpath:main.fxml}") Resource fxmlResource,
    ApplicationContext appContext
  ) {
    this.applicationTitle = applicationTitle;
    fxml = fxmlResource;
    this.appContext = appContext;
  }

  private void createScene(Stage primaryStage) {
    try {
      URL url = fxml.getURL();
      FXMLLoader fxmlLoader = new FXMLLoader(url);
      fxmlLoader.setControllerFactory(appContext::getBean);
      Parent root = fxmlLoader.load();
      Scene scene = new Scene(root, 600, 600);
      primaryStage.setScene(scene);
      primaryStage.setTitle(applicationTitle);
      appContext.publishEvent(new MainWindowCreated(scene));

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
      createScene(event.getPrimaryStage());
    }
  }
}
