package net.projectagain.planner.core.ui.menu;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import net.projectagain.planner.core.ui.UiManager;
import net.projectagain.planner.core.ui.theme.UiTheme;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class MenuManagerImpl implements MenuManager {

  private static final Logger log = getLogger(MenuManagerImpl.class);

  final UiManager uiManager;
  final List<MenuExtension> menuExtensions;

  public MenuManagerImpl(
    UiManager uiManager,
    List<MenuExtension> menuExtensions
  ) {
    this.uiManager = uiManager;
    this.menuExtensions = menuExtensions;
  }

  @Override
  public void createMainMenu(MenuBar menuBar) {
    ObservableList<Menu> menus = menuBar.getMenus();

    buildFileMenu(menus);
    buildEditMenu(menus);

    buildHelpMenu(menus);
  }

  private void buildFileMenu(ObservableList<Menu> menus) {
    Menu fileMenu = find(menus, UiTheme.FxIDSelector.Menu.Main.FILE);
    if (fileMenu == null) {
      log.error("No FileMenu found");
      return;
    }
    fileMenu.getItems().forEach(menuItem -> {
      if (menuItem instanceof SeparatorMenuItem)
        return;

      AtomicReference<EventHandler<ActionEvent>> action = new AtomicReference<>();
      menuExtensions.forEach(menuExtension -> {

        String menuId = menuItem.getId();

        if (menuId == null) {
          log.error("Menu has no id: {}", menuItem.toString());
          return;
        }
        EventHandler<ActionEvent> actionEvent = menuExtension.getActionEvent(menuId);
        if (action.get() != null && actionEvent != null) {
          throw new IllegalStateException(
            "Two actions for same menu " + menuId
              + " found. Second occurred in " + menuExtension.getClass().getSimpleName()
          );
        }
        action.set(actionEvent);
      });
      if (action.get() != null)
        menuItem.setOnAction(action.get());
    });
    log.info("FileMenu: {}", fileMenu.getId());
  }

  private void buildEditMenu(ObservableList<Menu> menus) {
    Menu editMenu = find(menus, UiTheme.FxIDSelector.Menu.Main.EDIT);
    if (editMenu == null) {
      log.error("No EditMenu found");
      return;
    }
    log.info("EditMenu: {}", editMenu.getId());
  }

  private void buildHelpMenu(ObservableList<Menu> menus) {
    Menu helpMenu = find(menus, UiTheme.FxIDSelector.Menu.Main.HELP);
    if (helpMenu == null) {
      log.error("No HelpMenu found");
      return;
    }
    log.info("HelpMenu: {}", helpMenu.getId());
  }

  private Menu find(final ObservableList<Menu> menus, final String id) {
    FilteredList<Menu> filtered = menus.filtered(menu -> id.equals(menu.getId()));
    return filtered.isEmpty() ? null : filtered.get(0);
  }


}
