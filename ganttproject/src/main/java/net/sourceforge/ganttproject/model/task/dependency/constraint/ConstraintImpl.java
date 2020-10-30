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

import net.sourceforge.ganttproject.model.task.dependency.TaskDependency;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependencyConstraint;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependencyConstraint.Type;
import net.sourceforge.ganttproject.model.time.GanttCalendar;

import java.util.Date;

/**
 * @author bard
 */
public abstract class ConstraintImpl implements Cloneable {
  private final String myName;
  private final Type myType;
  private TaskDependency myDependency;

  public ConstraintImpl(TaskDependencyConstraint.Type type, String myName) {
    myType = type;
    this.myName = myName;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public TaskDependencyConstraint.Collision getBackwardCollision(Date dependantStart) {
    return null;
  }

  public abstract TaskDependencyConstraint.Collision getCollision();

  public String getName() {
    return myName;
  }

  public Type getType() {
    return myType;
  }

  public void setTaskDependency(TaskDependency dependency) {
    myDependency = dependency;
  }

  @Override
  public String toString() {
    return getName();
  }

  protected void addDelay(GanttCalendar calendar) {
    shift(calendar, myDependency.getDifference());
  }

  protected TaskDependency getDependency() {
    return myDependency;
  }

  protected Date shift(Date date, int shift) {
    if (shift == 0) {
      // No shifting is required
      return date;
    }
    return myDependency.getDependant().getManager().getCalendar().shiftDate(
      date,
      myDependency.getDependant().getManager().createLength(shift)
    );
  }

  protected void shift(GanttCalendar calendar, int shift) {
    if (shift != 0) {
      Date shifted = shift(calendar.getTime(), shift);
      calendar.setTime(shifted);
    }
  }
}
