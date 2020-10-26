package net.projectagain.ganttplanner.app;

import net.projectagain.ganttplanner.i18n.I18N;
import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.chart.Chart;
import net.sourceforge.ganttproject.export.Exporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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
  @Autowired(required = false)
  private List<Chart> myCharts;
  @Autowired(required = false)
  private List<Exporter> myExporters;

  @Autowired
  public UiLauncher uiLauncher;

  public static App getInstance() {
    return instance;
  }

  public static AtomicReference<GanttProject> getMainWindow() {
    return getInstance().uiLauncher.mainWindow;
  }

  public List<Chart> getCharts() {
    return myCharts == null ? new ArrayList<>() : myCharts;
  }

  public List<Exporter> getExporters() {
    return myExporters == null ? new ArrayList<>() : myExporters;
  }

  @PostConstruct
  private void postConstruction() {
    instance = this;
  }
}
