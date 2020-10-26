package net.projectagain.ganttplanner.app;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.document.DocumentCreator;
import net.sourceforge.ganttproject.language.GanttLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static biz.ganttproject.app.SplashKt.SPLASH_HEIGHT;
import static biz.ganttproject.app.SplashKt.SPLASH_WIDTH;

@Service
@DependsOn("pluginManager")
public class UiLauncher {
  final AtomicReference<GanttProject> mainWindow = new AtomicReference<>();
  private final Logger log = LoggerFactory.getLogger(getClass());

  public void startUiApp(Function<GanttProject, GanttProject> configure) {
    configureApp();
    Runnable autosaveCleanup = DocumentCreator.createAutosaveCleanup();

//    CompletableFuture<Runnable> splashCloser = showAsync();

    SwingUtilities.invokeLater(
      () -> {
        try {
          GanttProject ganttFrame = new GanttProject(false);
          configure.apply(ganttFrame);
          System.err.println("Main frame created");
          mainWindow.set(ganttFrame);
          mainWindow.get().doShow();
//          ganttFrame.addWindowListener(new WindowAdapter() {
//
//            public void windowOpened(WindowEvent e) {
//              try {
//                splashCloser.get().run();
//              } catch (Exception ex) {
//                ex.printStackTrace();
//              }
//            }
//          });
        } catch (Exception e) {
          log.error("Failure when launching application: {}, {}", new Object[0], new HashMap<>(), e);
        } finally {
          Thread.currentThread().setUncaughtExceptionHandler((t, e) -> log.error("Uncaught Exception:", e));
        }
      }
    );
//    SwingUtilities.invokeLater(() -> mainWindow.get().doShow());
//    SwingUtilities.invokeLater(() -> {
//      mainWindow.get().doOpenStartupDocument(args);
//    });
//    if (autosaveCleanup != null) {
//      ourExecutor.submit(autosaveCleanup)
//    }

  }

  private void configureApp() {
    GanttLanguage.getInstance();
  }

  private CompletableFuture<Runnable> showAsync() {
    new JFXPanel();
    CompletableFuture<Runnable> result = new CompletableFuture<>();
    Platform.runLater(() -> {
      ImageView splash1 =
        new ImageView(new Image(GanttProject.class.getResourceAsStream("/resources/icons/splash.png")));
      VBox splashLayout = new VBox();
      splashLayout.getChildren().addAll(splash1);
      splashLayout.setEffect(new DropShadow());
      Scene splashScene = new Scene(splashLayout);
      splashScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
      Stage stage = new Stage(StageStyle.TRANSPARENT);
      stage.setAlwaysOnTop(true);
      stage.setScene(splashScene);
      Rectangle2D bounds = Screen.getPrimary().getBounds();
      stage.setX((bounds.getMinX() + (bounds.getWidth() / 2)) - (SPLASH_WIDTH / 2));
      stage.setY((bounds.getMinY() + (bounds.getHeight() / 2)) - (SPLASH_HEIGHT / 2));
      stage.show();
      result.complete(() -> Platform.runLater(stage::hide));
    });

    return result;
  }
}
