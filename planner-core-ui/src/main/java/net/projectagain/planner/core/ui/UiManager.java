package net.projectagain.planner.core.ui;

import javafx.stage.Stage;

import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;

public interface UiManager {
  Stage getMainStage();

  <V> RunnableFuture<V> runInUI(Callable<V> runnableFuture);
}
