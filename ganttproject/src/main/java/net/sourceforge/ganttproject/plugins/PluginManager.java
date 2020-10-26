/*
Copyright 2003-2012 Dmitry Barashev, GanttProject Team

This file is part of GanttProject, an opensource project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.ganttproject.plugins;

import net.projectagain.ganttplanner.app.App;

import net.sourceforge.ganttproject.chart.Chart;
import net.sourceforge.ganttproject.export.Exporter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Very basic Plugin Manager
 *
 * @author bbaranne
 */
public class PluginManager {
  private static final Logger log = getLogger(PluginManager.class);

  public static <T> List<T> getExtensions(String extensionPointID, Class<T> extensionPointInterface) {
    IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
    log.info("extensionregistry: {}", extensionRegistry);
    ArrayList<T> extensions = new ArrayList<T>();
    IConfigurationElement[] elements = extensionRegistry.getConfigurationElementsFor(extensionPointID);
    log.info("Elements: {}", (Object[]) elements);
    for (IConfigurationElement configElement : elements) {
      try {
        Object nextExtension = configElement.createExecutableExtension("class");
        assert nextExtension != null && extensionPointInterface.isAssignableFrom(nextExtension.getClass());
        extensions.add((T) nextExtension);
      } catch (CoreException e) {
        log.error("Exception", e);
      }
    }
    return extensions;
  }

  public static List<Chart> getCharts() {
    return App.getInstance().getCharts();
  }

  public static List<Exporter> getExporters() {
    return App.getInstance().getExporters();
  }
}
