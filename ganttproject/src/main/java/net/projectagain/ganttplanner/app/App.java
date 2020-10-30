package net.projectagain.ganttplanner.app;

import net.projectagain.ganttplanner.core.i18n.I18N;
import net.projectagain.ganttplanner.core.plugins.PluginManager;
import net.projectagain.ganttplanner.core.settings.SettingsManager;
import net.projectagain.ganttplanner.core.ui.UiManager;
import net.sourceforge.ganttproject.ui.GanttProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
@Component
public class App {
  private static App instance;
  @Autowired
  public ApplicationContext applicationContext;
  @Autowired
  public I18N i18n;
  @Autowired
  private PluginManager pluginManager;
  @Autowired
  private SettingsManager settingsManager;
  @Autowired
  private UiManager uiManager;

  public static App getInstance() {
    return instance;
  }

  public Resource getResource(String location) {
    return applicationContext.getResource(location);
  }


  public GanttProject getMainWindow() {
    return getInstance().uiManager.getMainWindow().get();
  }

  public PluginManager getPluginManager() {
    return pluginManager;
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
