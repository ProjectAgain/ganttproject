package net.projectagain.planner.core.ui.theme;

import org.springframework.core.io.Resource;

public interface UiTheme {
  interface IDs {
    String MAINFXML = "fxml/main.fxml";
  }

  interface FxIDSelector {
    interface Menu {
      interface Main {
        String MENUBAR = "menu-main-menubar";
        String FILE = "menu-main-file";
        String EDIT = "menu-main-edit";
        String HELP = "menu-main-help";
        interface File {
          String SETTINGS = "menu-main-file-settings";
          String CLOSE = "menu-main-file-close";
        }

      }
    }
  }

  String getName();

  Resource getResource(String id);
}
