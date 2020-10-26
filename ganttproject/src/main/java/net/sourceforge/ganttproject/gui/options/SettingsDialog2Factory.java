package net.sourceforge.ganttproject.gui.options;

import net.projectagain.ganttplanner.app.App;
import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.gui.options.model.OptionPageProvider;
import org.apache.commons.collections4.Factory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SettingsDialog2Factory implements Factory<SettingsDialog2> {
  private final Logger log = getLogger(getClass());

  private final List<OptionPageProvider> optionPageProviders;

  private final App app;

  public SettingsDialog2Factory(final App aApp, List<OptionPageProvider> optionPageProviders) {
    app = aApp;

    this.optionPageProviders = optionPageProviders;
  }

  @Override
  public SettingsDialog2 create() {
    GanttProject project = app.getMainWindow().get();
    return new SettingsDialog2(project, project.getUIFacade(), "settings.app.pageOrder", optionPageProviders);
  }
}
