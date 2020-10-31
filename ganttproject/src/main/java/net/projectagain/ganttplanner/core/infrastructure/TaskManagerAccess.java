package net.projectagain.ganttplanner.core.infrastructure;

import net.sourceforge.ganttproject.model.task.CustomPropertyManager;
import net.sourceforge.ganttproject.model.task.TaskContainmentHierarchyFacade;
import net.sourceforge.ganttproject.model.task.TaskManager;

/**
 * @author Christoph Graupner <ch.graupner@workingdeveloper.net>
 */
public interface TaskManagerAccess {
  TaskContainmentHierarchyFacade getTaskContainment();

  CustomPropertyManager getTaskCustomColumnManager();

  TaskManager getTaskManager();
}
