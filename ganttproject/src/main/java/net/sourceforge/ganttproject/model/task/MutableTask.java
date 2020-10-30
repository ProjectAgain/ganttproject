/*
GanttProject is an opensource project management tool.
Copyright (C) 2004-2011 GanttProject Team

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
package net.sourceforge.ganttproject.model.task;

import net.sourceforge.ganttproject.model.time.GanttCalendar;
import net.sourceforge.ganttproject.model.time.TimeDuration;
import net.sourceforge.ganttproject.ui.chart.render.ShapePaint;

import java.awt.*;

/**
 * @author bard
 */
public interface MutableTask {
  void addNotes(String notes);

  void setColor(Color color);

  void setCompletionPercentage(int percentage);

  /**
   * Sets the task as critical or not. The method is used be TaskManager after
   * having run a CriticalPathAlgorithm to set the critical tasks. When painted,
   * the tasks are rendered as critical using Task.isCritical(). So, a task is
   * set as critical only if critical path is displayed.
   *
   * @param critical <code>true</code> if this is critical, <code>false</code>
   *                 otherwise.
   */
  void setCritical(boolean critical);

  void setDuration(TimeDuration length);

  void setEnd(GanttCalendar end);

  void setExpand(boolean expand);

  void setMilestone(boolean isMilestone);

  // void setStartFixed(boolean isFixed);

  // void setFinishFixed(boolean isFixed);

  void setName(String name);

  void setNotes(String notes);

  void setPriority(Task.Priority priority);

  void setProjectTask(boolean projectTask);

  void setShape(ShapePaint shape);

  void setStart(GanttCalendar start);

  void setTaskInfo(TaskInfo taskInfo);

  /**
   * Sets the weblink for the task
   */
  void setWebLink(String webLink);

  void shift(TimeDuration shift);
}
