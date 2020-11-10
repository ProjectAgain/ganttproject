package net.projectagain.planner.core.ui.impl;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.projectagain.planner.core.ui.UiManager;
import net.projectagain.planner.core.ui.event.MainWindowCreated;
import net.projectagain.planner.core.ui.event.internal.PrimaryStageReady;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

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

    @Override
    public <V> RunnableFuture<V> runInUI(Callable<V> runnableFuture) {
        FutureTask<V> futureTask = new FutureTask<>(runnableFuture);
        Platform.runLater(futureTask);
        return futureTask;
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
