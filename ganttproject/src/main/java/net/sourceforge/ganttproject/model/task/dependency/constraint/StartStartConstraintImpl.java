/*
GanttProject is an opensource project management tool.
Copyright (C) 2011 GanttProject team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.model.task.dependency.constraint;

import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.model.task.TaskActivity;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependency;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependency.ActivityBinding;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependencyConstraint;
import net.sourceforge.ganttproject.model.time.CalendarFactory;
import net.sourceforge.ganttproject.model.time.GanttCalendar;
import org.slf4j.Logger;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Dependent task starts not earlier than dependee starts
 *
 * @author bard
 */
public class StartStartConstraintImpl extends ConstraintImpl implements TaskDependencyConstraint {
  private final Logger log = getLogger(getClass());

  public StartStartConstraintImpl() {
    super(TaskDependencyConstraint.Type.startstart, GanttLanguage.getInstance().getText("startstart"));
  }

  @Override
  public ActivityBinding getActivityBinding() {
    List<TaskActivity> dependantActivities = getDependency().getDependant().getActivities();
    List<TaskActivity> dependeeActivities = getDependency().getDependee().getActivities();
    if (dependantActivities.isEmpty()) {
      log.info("Task {} has no activities", getDependency().getDependant());
      return null;
    }
    if (dependeeActivities.isEmpty()) {
      log.info("Task {} has no activities", getDependency().getDependee());
      return null;
    }
    TaskActivity theDependant = dependantActivities.get(0);
    TaskActivity theDependee = dependeeActivities.get(0);
    return new DependencyActivityBindingImpl(theDependant, theDependee, new Date[]{theDependant.getStart(),
                                                                                   theDependee.getStart()});
  }

  @Override
  public Collision getBackwardCollision(Date dependantStart) {
    Task dependee = getDependency().getDependee();
    GanttCalendar dependeeEnd = dependee.getEnd().clone();

    Date barrier = shift(dependantStart, dependee.getDuration().getLength() - getDependency().getDifference());
    boolean isActive =
      getDependency().getHardness() == TaskDependency.Hardness.RUBBER ? dependeeEnd.getTime().compareTo(
        barrier) > 0
                                                                      : dependeeEnd.getTime().compareTo(barrier) != 0;

    return new TaskDependencyConstraint.DefaultCollision(CalendarFactory.createGanttCalendar(barrier),
                                                         TaskDependencyConstraint.Collision.START_EARLIER_VARIATION,
                                                         isActive
    );
  }

  @Override
  public TaskDependencyConstraint.Collision getCollision() {
    TaskDependencyConstraint.Collision result = null;
    Task dependee = getDependency().getDependee();
    Task dependant = getDependency().getDependant();
    GanttCalendar dependeeStart = dependee.getStart();
    GanttCalendar dependantStart = dependant.getStart();

    int difference = getDependency().getDifference();
    GanttCalendar comparisonDate = dependeeStart.clone();
    comparisonDate.add(Calendar.DATE, difference);

    boolean isActive =
      getDependency().getHardness() == TaskDependency.Hardness.RUBBER ? dependantStart.compareTo(comparisonDate) < 0
                                                                      : dependantStart.compareTo(comparisonDate) != 0;
    // GanttCalendar acceptableStart = dependee.getStart();
    GanttCalendar acceptableStart = dependee.getStart().clone();
    addDelay(acceptableStart);
    result = new TaskDependencyConstraint.DefaultCollision(acceptableStart,
                                                           TaskDependencyConstraint.Collision.START_LATER_VARIATION,
                                                           isActive
    );
    return result;
  }
}
