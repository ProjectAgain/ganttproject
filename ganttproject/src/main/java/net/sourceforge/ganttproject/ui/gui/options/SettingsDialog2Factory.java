package net.sourceforge.ganttproject.ui.gui.options;

import net.projectagain.ganttplanner.app.LegacyApp;
import net.sourceforge.ganttproject.ui.GanttProjectUI;
import net.sourceforge.ganttproject.ui.gui.options.model.OptionPageProvider;
import org.apache.commons.collections4.Factory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class SettingsDialog2Factory implements Factory<SettingsDialog2> {
  private final Logger log = getLogger(getClass());

  private final List<OptionPageProvider> optionPageProviders;

  private final LegacyApp legacyApp;

  public SettingsDialog2Factory(final LegacyApp aLegacyApp, List<OptionPageProvider> optionPageProviders) {
    legacyApp = aLegacyApp;

    this.optionPageProviders = optionPageProviders;
  }

  @Override
  public SettingsDialog2 create() {
    GanttProjectUI project = legacyApp.getMainWindow();
    return new SettingsDialog2(project, project.getUIFacade(), "settings.app.pageOrder", optionPageProviders);
  }
}
