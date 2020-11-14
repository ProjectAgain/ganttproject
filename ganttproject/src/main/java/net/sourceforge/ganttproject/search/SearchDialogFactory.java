package net.sourceforge.ganttproject.search;

import net.projectagain.ganttplanner.app.LegacyApp;
import org.apache.commons.collections4.Factory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
@Service
public class SearchDialogFactory implements Factory<SearchDialog> {

  final List<SearchService> services;
  private final LegacyApp legacyApp;

  public SearchDialogFactory(
    final LegacyApp legacyApp,
    final List<SearchService> services
  ) {
    this.legacyApp = legacyApp;
    this.services = services;
  }

  @Override
  public SearchDialog create() {
    return new SearchDialog(legacyApp.getMainWindow(), legacyApp.getUiManager().getUIFacade(), services);
  }
}
