/*
 * Created on 18.10.2004
 */
package net.sourceforge.ganttproject.model.task;

import net.sourceforge.ganttproject.model.time.TimeDuration;

import java.util.Date;
import java.util.List;

/**
 * @author bard
 */
class TaskActivityImpl implements TaskActivity {

  private final TimeDuration myDuration;
  private final Date myEndDate;
  private final Date myStartDate;
  private final Task myTask;
  private final float myIntensity;

  TaskActivityImpl(Task task, Date startDate, Date endDate) {
    this(task, startDate, endDate, 1.0f);
  }

  TaskActivityImpl(Task task, Date startDate, Date endDate, float intensity) {
    myStartDate = startDate;
    myEndDate = endDate;
    myDuration = task.getManager().createLength(task.getDuration().getTimeUnit(), startDate, endDate);
    myIntensity = intensity;
    myTask = task;
  }

  public TimeDuration getDuration() {
    return myDuration;
  }

  public Date getEnd() {
    return myEndDate;
  }

  @Override
  public float getIntensity() {
    return myIntensity;
  }

  public Task getOwner() {
    return myTask;
  }

  public Date getStart() {
    return myStartDate;
  }

  @Override
  public boolean isFirst() {
    return this == getOwner().getActivities().get(0);
  }

  @Override
  public boolean isLast() {
    List<TaskActivity> all = getOwner().getActivities();
    return this == all.get(all.size() - 1);
  }

  @Override
  public String toString() {
    return myTask.toString() + "[" + getStart() + ", " + getEnd() + "]";
  }
}
