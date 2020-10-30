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

import net.sourceforge.ganttproject.model.task.Task;

/**
 * Created by IntelliJ IDEA. User: bard Date: 14.02.2004 Time: 15:42:21 To
 * change this template use File | Settings | File Templates.
 */
public interface TaskDependencyCollection extends MutableTaskDependencyCollection {

  boolean canCreateDependency(Task myDependant, Task dependee);

  TaskDependencyCollectionMutator createMutator();

  // void addDependency(TaskDependency dep) throws TaskDependencyException;
  // void removeDependency(TaskDependency dep);
  TaskDependency[] getDependencies();

  TaskDependency[] getDependencies(Task task);

  TaskDependency[] getDependenciesAsDependant(Task dependant);

  TaskDependency[] getDependenciesAsDependee(Task dependee);
}
