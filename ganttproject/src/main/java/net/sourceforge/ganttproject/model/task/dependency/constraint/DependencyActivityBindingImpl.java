/*
 * Created on 24.10.2004
 */
package net.sourceforge.ganttproject.model.task.dependency.constraint;

import net.sourceforge.ganttproject.model.task.TaskActivity;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependency;

import java.util.Date;

/**
 * @author bard
 */
class DependencyActivityBindingImpl implements TaskDependency.ActivityBinding {

  private final Date[] myAlignedBounds;
  private final TaskActivity myDependantActivity;
  private final TaskActivity myDependeeActivity;

  DependencyActivityBindingImpl(TaskActivity dependantActivity, TaskActivity dependeeActivity, Date[] alignedBounds) {
    myDependantActivity = dependantActivity;
    myDependeeActivity = dependeeActivity;
    myAlignedBounds = alignedBounds;
  }

  @Override
  public Date[] getAlignedBounds() {
    return myAlignedBounds;
  }

  @Override
  public TaskActivity getDependantActivity() {
    return myDependantActivity;
  }

  @Override
  public TaskActivity getDependeeActivity() {
    return myDependeeActivity;
  }
}
