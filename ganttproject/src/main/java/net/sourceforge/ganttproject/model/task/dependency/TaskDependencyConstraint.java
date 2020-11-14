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
package net.sourceforge.ganttproject.model.task.dependency;

import net.sourceforge.ganttproject.model.time.GanttCalendar;

import java.util.Date;

/**
 * Created by IntelliJ IDEA. User: bard Date: 14.02.2004 Time: 2:35:20 To change
 * this template use File | Settings | File Templates.
 */
public interface TaskDependencyConstraint extends Cloneable {
  class DefaultCollision implements Collision {
    private final boolean isActive;
    private final GanttCalendar myAcceptableStart;
    private final int myVariation;

    public DefaultCollision(GanttCalendar myAcceptableStart, int myVariation, boolean isActive) {
      this.myAcceptableStart = myAcceptableStart;
      this.myVariation = myVariation;
      this.isActive = isActive;
    }

    @Override
    public GanttCalendar getAcceptableStart() {
      return myAcceptableStart;
    }

    @Override
    public int getVariation() {
      return myVariation;
    }

    @Override
    public boolean isActive() {
      return isActive;
    }
  }

  enum Type {
    startstart, finishstart, finishfinish, startfinish;
    private static final String[] PERSISTENT_VALUES = new String[]{
      "SS", "FS", "FF", "SF"
    };

    public static Type fromPersistentValue(String dependencyTypeAsString) {
      return Type.values()[Integer.parseInt(dependencyTypeAsString) - 1];
    }

    public static Type fromReadablePersistentValue(String str) {
      for (int i = 0; i < PERSISTENT_VALUES.length; i++) {
        if (PERSISTENT_VALUES[i].equals(str)) {
          return Type.values()[i];
        }
      }
      throw new IllegalArgumentException("Can't find constraint by persistent value=" + str);
    }

    public String getPersistentValue() {
      return String.valueOf(ordinal() + 1);
    }

    public String getReadablePersistentValue() {
      return PERSISTENT_VALUES[ordinal()];
    }
  }

  interface Collision {
    int NO_VARIATION = 0;
    int START_EARLIER_VARIATION = -1;
    int START_LATER_VARIATION = 1;

    GanttCalendar getAcceptableStart();

    int getVariation();

    boolean isActive();
  }

  Object clone() throws CloneNotSupportedException;

  TaskDependency.ActivityBinding getActivityBinding();

  Collision getBackwardCollision(Date depedantStart);

  // boolean isFulfilled();
  // void fulfil();
  Collision getCollision();

  String getName();

  Type getType();

  void setTaskDependency(TaskDependency dependency);
}