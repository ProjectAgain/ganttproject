/*
 LICENSE:

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 3 of the License, or
 (at your option) any later version.

 Copyright (C) 2004, GanttProject Development Team
 */
package net.sourceforge.ganttproject.model.task.event;

import net.sourceforge.ganttproject.model.task.dependency.TaskDependency;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependencyCollection;

import java.util.EventObject;

/**
 * Created by IntelliJ IDEA. User: bard
 */
public class TaskDependencyEvent extends EventObject {
  private final TaskDependency myDependency;

  public TaskDependencyEvent(TaskDependencyCollection source, TaskDependency dependency) {
    super(source);
    myDependency = dependency;
  }

  public TaskDependency getDependency() {
    return myDependency;
  }
}