/*
GanttProject is an opensource project management tool.
Copyright (C) 2002-2010 Alexandre Thomas, Dmitry Barashev

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

import net.sourceforge.ganttproject.model.document.Document;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependencySlice;
import net.sourceforge.ganttproject.model.time.GanttCalendar;
import net.sourceforge.ganttproject.model.time.TimeDuration;
import net.sourceforge.ganttproject.ui.chart.render.ShapePaint;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Project task definition
 *
 * @author bard
 */
public interface Task extends MutableTask {
  /**
   * Available task priorities
   */
  enum Priority {
    LOWEST("3"), LOW("0"), NORMAL("1"), HIGH("2"), HIGHEST("4");

    private final String myPersistentValue;

    Priority(String persistentValue) {
      myPersistentValue = persistentValue;
    }

    public static Priority fromPersistentValue(String priority) {
      for (Priority p: values()) {
        if (p.getPersistentValue().equals(priority)) {
          return p;
        }
      }
      return Priority.NORMAL;
    }

    /**
     * @return the Priority value for the given integer value, or
     * DEFAULT_PRIORITY if unknown
     */
    public static Priority getPriority(int value) {
      for (Task.Priority p: Task.Priority.values()) {
        if (p.ordinal() == value) {
          return p;
        }
      }
      return DEFAULT_PRIORITY;
    }

    /**
     * @return the key to get the I18n value for the priority
     */
    public String getI18nKey() {
      return "priority." + getLowerString();
    }

    /**
     * @return the path to the icon representing the priority
     */
    public String getIconPath() {
      return "/icons/task_" + getLowerString() + ".gif";
    }

    /**
     * @return the priority as a lower-case String
     */
    public String getLowerString() {
      return this.toString().toLowerCase();
    }

    public String getPersistentValue() {
      return myPersistentValue;
    }
  }

  interface Cost {
    BigDecimal getCalculatedValue();

    BigDecimal getManualValue();

    BigDecimal getValue();

    void setValue(Cost copy);

    void setValue(BigDecimal value);

    boolean isCalculated();

    void setCalculated(boolean calculated);
  }
  /**
   * Default priority (for new tasks)
   */
  Priority DEFAULT_PRIORITY = Priority.NORMAL;

  void applyThirdDateConstraint();

  TaskMutator createMutator();

  TaskMutator createMutatorFixingDuration();

  void delete();

  List<TaskActivity> getActivities();

  ResourceAssignmentCollection getAssignmentCollection();

  // HumanResource[] getAssignedHumanResources();
  ResourceAssignment[] getAssignments();

  List<Document> getAttachments();

  /**
   * @return a color representing this Task (could be a custom color, milestone
   * color, super task color or default color)
   */
  Color getColor();

  int getCompletionPercentage();

  Cost getCost();

  CustomColumnsValues getCustomValues();

  TaskDependencySlice getDependencies();

  TaskDependencySlice getDependenciesAsDependant();

  TaskDependencySlice getDependenciesAsDependee();

  GanttCalendar getDisplayEnd();

  TimeDuration getDuration();

  GanttCalendar getEnd();

  boolean getExpand();

  TaskManager getManager();

  String getName();

  Task[] getNestedTasks();

  String getNotes();

  Priority getPriority();

  ShapePaint getShape();

  GanttCalendar getStart();

  //
  Task getSupertask();

  // main properties
  int getTaskID();

  TaskInfo getTaskInfo();

  GanttCalendar getThird();

  int getThirdDateConstraint();

  void setThirdDateConstraint(int dateConstraint);

  boolean isCritical();

  boolean isMilestone();

  boolean isProjectTask();

  boolean isSupertask();

  void move(Task targetSupertask);

  void move(Task targetSupertask, int position);

  void setThirdDate(GanttCalendar thirdDate);

  TimeDuration translateDuration(TimeDuration duration);

  Task unpluggedClone();
}
