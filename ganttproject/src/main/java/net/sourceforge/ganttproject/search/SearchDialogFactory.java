package net.sourceforge.ganttproject.search;

import net.projectagain.ganttplanner.app.App;
import org.apache.commons.collections4.Factory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
@Service
public class SearchDialogFactory implements Factory<SearchDialog> {

  private final App app;
  final List<SearchService> services;

  public SearchDialogFactory(
    final App app,
    final List<SearchService> services
  ) {
    this.app = app;
    this.services = services;
  }

  @Override
  public SearchDialog create() {
    return new SearchDialog(app.getMainWindow(), app.getUiManager().getUIFacade(), services);
  }
}
