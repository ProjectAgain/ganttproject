/*
 * Created on 24.10.2004
 */
package net.sourceforge.ganttproject.model.task.dependency.constraint;

import java.util.Date;

import net.sourceforge.ganttproject.model.task.TaskActivity;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependency;

/**
 * @author bard
 */
class DependencyActivityBindingImpl implements TaskDependency.ActivityBinding {

  private final TaskActivity myDependeeActivity;

  private final TaskActivity myDependantActivity;

  private final Date[] myAlignedBounds;

  DependencyActivityBindingImpl(TaskActivity dependantActivity, TaskActivity dependeeActivity, Date[] alignedBounds) {
    myDependantActivity = dependantActivity;
    myDependeeActivity = dependeeActivity;
    myAlignedBounds = alignedBounds;

  }

  @Override
  public TaskActivity getDependantActivity() {
    return myDependantActivity;
  }

  @Override
  public TaskActivity getDependeeActivity() {
    return myDependeeActivity;
  }

  @Override
  public Date[] getAlignedBounds() {
    return myAlignedBounds;
  }

}
