package net.projectagain.planner.legacy.adapter.menu;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import net.projectagain.planner.core.annotations.ExtensionComponent;
import net.projectagain.planner.core.annotations.LegacyAdapter;
import net.projectagain.planner.core.ui.menu.MenuExtension;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.Map;

@ExtensionComponent
@LegacyAdapter
public class LegacyMenuActionAdapterExtension implements MenuExtension {

  @Override
  public boolean hasInterest(@NonNull String menuId) {

    switch (menuId) {
      case MenuExtension.Menu.Main.FILE:
      case MenuExtension.Menu.Main.EDIT:
      case MenuExtension.Menu.Main.Help.ABOUT:
        return true;
    }
    return false;
  }

  @NonNull
  @Override
  public Map<InsertRelation, MenuItem> getMenusToAppend(@NonNull String menuId) {
    return Collections.emptyMap();
  }

  @Override
  public EventHandler<ActionEvent> getActionEvent(@NonNull String menuId) {

    switch (menuId) {
      case MenuExtension.Menu.Main.FILE:
      case MenuExtension.Menu.Main.EDIT:
      case MenuExtension.Menu.Main.Help.ABOUT:
        return null;
    }
    return null;

  }
}
