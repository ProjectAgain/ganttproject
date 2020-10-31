package net.projectagain.ganttplanner.core.infrastructure;

import net.sourceforge.ganttproject.model.resource.HumanResourceManager;
import net.sourceforge.ganttproject.model.task.CustomPropertyManager;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
public interface HumanResourceManagerAccess {
  HumanResourceManager getHumanResourceManager();

  CustomPropertyManager getResourceCustomPropertyManager();
}
