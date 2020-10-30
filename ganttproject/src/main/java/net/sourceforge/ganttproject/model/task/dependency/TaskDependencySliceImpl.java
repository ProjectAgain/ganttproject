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

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import net.sourceforge.ganttproject.model.task.Task;

import java.util.List;

public class TaskDependencySliceImpl implements TaskDependencySlice {
  private final TaskDependencyCollection myDependencyCollection;
  private final Function<Task, TaskDependencySlice> myTargetDepSliceFxn;
  private final Task myTask;

  public TaskDependencySliceImpl(
    Task task, TaskDependencyCollection dependencyCollection, Function<Task, TaskDependencySlice> getTargetSlice
  ) {
    myTask = task;
    myDependencyCollection = dependencyCollection;
    myTargetDepSliceFxn = getTargetSlice;
  }

  @Override
  public void clear() {
    TaskDependency[] deps = toArray();
    for (int i = 0; i < deps.length; i++) {
      deps[i].delete();
    }
  }

  /**
   * Unlinks only tasks that are selected and leaves links to not selected
   * tasks.
   */
  @Override
  public void clear(List<Task> selection) {
    TaskDependency[] deps = toArray();
    for (int i = 0; i < deps.length; i++) {
      if (selection.contains(deps[i].getDependant()) && selection.contains(deps[i].getDependee())) {
        deps[i].delete();
      }
    }
  }

  @Override
  public TaskDependency getDependency(Task target) {
    TaskDependencySlice targetDepSlice = myTargetDepSliceFxn.apply(target);
    SetView<TaskDependency> intersection =
      Sets.intersection(Sets.newHashSet(toArray()), Sets.newHashSet(targetDepSlice.toArray()));
    assert intersection.size() <= 1 :
      "Intersection of dependency sets between two tasks can't contain more than 1 dependency. But we get:" +
      intersection;
    return intersection.isEmpty() ? null : intersection.iterator().next();
  }

  @Override
  public boolean hasLinks(List<Task> selection) {
    TaskDependency[] deps = toArray();
    for (int i = 0; i < deps.length; i++) {
      if (selection.contains(deps[i].getDependant()) && selection.contains(deps[i].getDependee())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public TaskDependency[] toArray() {
    return myDependencyCollection.getDependencies(myTask);
  }

  protected TaskDependencyCollection getDependencyCollection() {
    return myDependencyCollection;
  }

  protected Task getTask() {
    return myTask;
  }
}
