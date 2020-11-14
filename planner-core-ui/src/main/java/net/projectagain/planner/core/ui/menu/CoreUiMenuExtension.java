package net.projectagain.planner.core.ui.menu;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import net.projectagain.planner.core.annotations.ExtensionComponent;
import net.projectagain.planner.core.ui.theme.UiTheme;
import org.slf4j.Logger;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

@ExtensionComponent
public class CoreUiMenuExtension implements MenuExtension {
  private static final Logger log = getLogger(CoreUiMenuExtension.class);

  @Override
  public boolean hasInterest(@NonNull final String menuId) {
    switch (menuId) {
      case UiTheme.FxIDSelector.Menu.Main.File.CLOSE:
        return true;
    }
    return false;
  }

  @NonNull
  @Override
  public Map<InsertRelation, MenuItem> getMenusToAppend(@NonNull final String menuId) {
    return Collections.emptyMap();
  }

  @Override
  public EventHandler<ActionEvent> getActionEvent(@NonNull final String menuId) {
    switch (menuId) {
      case UiTheme.FxIDSelector.Menu.Main.File.CLOSE:
        return event -> {
          log.info("Closing action triggered");
          Platform.exit();
        };
    }

    return null;
  }
}
