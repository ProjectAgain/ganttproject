package net.projectagain.ganttplanner.app;

import net.projectagain.ganttplanner.core.i18n.LegacyI18N;
import net.projectagain.ganttplanner.core.plugins.LegacyPluginManager;
import net.projectagain.ganttplanner.core.settings.SettingsManager;
import net.projectagain.ganttplanner.core.ui.UiManager;
import net.sourceforge.ganttproject.ui.GanttProjectUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
@Component
public class LegacyApp {
  private static LegacyApp instance;
  @Autowired
  public ApplicationContext applicationContext;
  @Autowired
  public LegacyI18N legacyI18N;
  @Autowired
  private LegacyPluginManager legacyPluginManager;
  @Autowired
  private SettingsManager settingsManager;
  @Autowired
  private UiManager uiManager;

  public static LegacyApp getInstance() {
    return instance;
  }

  public Resource getResource(String location) {
    return applicationContext.getResource(location);
  }


  public GanttProjectUI getMainWindow() {
    return getInstance().uiManager.getMainWindow().get();
  }

  public LegacyPluginManager getPluginManager() {
    return legacyPluginManager;
  }

  public SettingsManager getSettingsManager() {
    return settingsManager;
  }

  public UiManager getUiManager() {
    return uiManager;
  }

  @PostConstruct
  private void postConstruction() {
    instance = this;
  }
}
