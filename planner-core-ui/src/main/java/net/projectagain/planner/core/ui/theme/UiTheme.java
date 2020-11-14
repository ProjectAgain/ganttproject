package net.projectagain.planner.core.ui.theme;

import org.springframework.core.io.Resource;

public interface UiTheme {
  interface IDs {
    String MAINFXML = "fxml/main.fxml";
  }

  interface FxIDSelector {
  }

  String getName();

  Resource getResource(String id);
}
