package net.projectagain.planner.core.ui.menu;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import net.projectagain.planner.core.Extension;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

public interface MenuExtension extends Extension {
  enum InsertRelation {
    BEFORE, AFTER
  }

  interface Menu {
    interface Main {
      interface File {
        String SETTINGS = "menu-main-file-settings";
        String CLOSE = "menu-main-file-close";
      }

      interface Help {
        String ABOUT = "menu-main-help-about";
      }
      String MENUBAR = "menu-main-menubar";
      String FILE = "menu-main-file";
      String EDIT = "menu-main-edit";
      String HELP = "menu-main-help";

    }
  }

  boolean hasInterest(@NonNull final String menuId);

  @NonNull
  Map<InsertRelation, MenuItem> getMenusToAppend(@NonNull final String menuId);

  @Nullable
  EventHandler<ActionEvent> getActionEvent(@NonNull final String menuId);
}
