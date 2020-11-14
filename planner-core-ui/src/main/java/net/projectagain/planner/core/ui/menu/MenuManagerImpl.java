package net.projectagain.planner.core.ui.menu;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import net.projectagain.planner.core.ui.UiManager;
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
    menus.forEach(topMenu -> {
      String topMenuId = topMenu.getId();
      log.trace("Building top menu {}", topMenuId);
      topMenu.getItems().forEach(menuItem -> {
        if (menuItem instanceof SeparatorMenuItem)
          return;

        final String menuId = menuItem.getId();

        if (menuId == null) {
          log.error("Menu has no id: {}", menuItem.toString());
          return;
        }

        log.trace("Building menu {}", menuId);

        if (!menuId.startsWith(topMenuId)) {
          log.warn("{} does not contain top level menu id [{}] in its id.", topMenuId, menuId);
        }


        AtomicReference<EventHandler<ActionEvent>> action = new AtomicReference<>();

        menuExtensions.forEach(menuExtension -> {
          EventHandler<ActionEvent> actionEvent = menuExtension.getActionEvent(menuId);
          if (actionEvent != null) {
            if (action.get() != null) {
              throw new IllegalStateException(
                "Two actions for same menu " + menuId
                  + " found. Second occurred in " + menuExtension.getClass().getSimpleName()
              );
            }
            log.debug("Got {} from menu extension {}", menuId, menuExtension.getClass().getSimpleName());
            action.set(actionEvent);
          }
        });
        EventHandler<ActionEvent> eventHandler = action.get();
        if (eventHandler != null) {
          menuItem.setOnAction(eventHandler);
          log.info("Set {} to action {}", menuId, eventHandler.getClass().getSimpleName());
        }
      });
      log.trace("Done build menu {}", topMenuId);
    });
  }
}
