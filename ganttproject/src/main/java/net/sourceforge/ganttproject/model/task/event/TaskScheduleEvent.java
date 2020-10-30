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
package net.sourceforge.ganttproject.model.task.event;

import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.model.time.GanttCalendar;

import java.util.EventObject;

/**
 * Created by IntelliJ IDEA. User: bard
 */
public class TaskScheduleEvent extends EventObject {
  private final GanttCalendar myNewFinishDate;
  private final GanttCalendar myNewStartDate;
  private final GanttCalendar myOldFinishDate;
  private final GanttCalendar myOldStartDate;

  public TaskScheduleEvent(
    Task source, GanttCalendar oldStartDate, GanttCalendar oldFinishDate,
    GanttCalendar newStartDate, GanttCalendar newFinishDate
  ) {
    super(source);
    myOldStartDate = oldStartDate;
    myOldFinishDate = oldFinishDate;
    myNewStartDate = newStartDate;
    myNewFinishDate = newFinishDate;
  }

  public GanttCalendar getNewFinishDate() {
    return myNewFinishDate;
  }

  public GanttCalendar getNewStartDate() {
    return myNewStartDate;
  }

  public GanttCalendar getOldFinishDate() {
    return myOldFinishDate;
  }

  public GanttCalendar getOldStartDate() {
    return myOldStartDate;
  }

  public Task getTask() {
    return (Task) getSource();
  }
}
