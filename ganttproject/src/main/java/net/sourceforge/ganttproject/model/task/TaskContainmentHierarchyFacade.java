/*
GanttProject is an opensource project management tool.
Copyright (C) 2011 GanttProject Team

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

import com.google.common.base.Predicate;
import net.sourceforge.ganttproject.util.collect.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author bard
 */
public interface TaskContainmentHierarchyFacade {
  interface Factory {
    TaskContainmentHierarchyFacade createFacade();
  }
  TaskContainmentHierarchyFacade STUB = new TaskContainmentHierarchyFacade() {
    @Override
    public boolean areUnrelated(Task dependant, Task dependee) {
      return false;
    }

    @Override
    public List<Task> breadthFirstSearch(Task root, boolean includeRoot) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void breadthFirstSearch(Task root, Predicate<Pair<Task, Task>> predicate) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int compareDocumentOrder(Task next, Task dependeeTask) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Task task) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Task getContainer(Task nestedTask) {
      return null;
    }

    @Override
    public Task[] getDeepNestedTasks(Task container) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public int getDepth(Task task) {
      return 0;
    }

    @Override
    public Task[] getNestedTasks(Task container) {
      return new Task[0];
    }

    @Override
    public Task getNextSibling(Task nestedTask) {
      return null;
    }

    @Override
    public List<Integer> getOutlinePath(Task task) {
      return Collections.emptyList();
    }

    @Override
    public Task getPreviousSibling(Task nestedTask) {
      return null;
    }

    @Override
    public Task getRootTask() {
      return null;
    }

    @Override
    public int getTaskIndex(Task nestedTask) {
      return 0;
    }

    @Override
    public List<Task> getTasksInDocumentOrder() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasNestedTasks(Task container) {
      return false;
    }

    @Override
    public void move(Task whatMove, Task whereMove) {
    }

    @Override
    public void move(Task whatMove, Task whereMove, int index) {
    }

    @Override
    public void sort(Comparator<Task> comparator) {
    }
  };

  boolean areUnrelated(Task dependant, Task dependee);

  void breadthFirstSearch(Task root, final Predicate<Pair<Task, Task>> predicate);

  List<Task> breadthFirstSearch(Task root, boolean includeRoot);

  int compareDocumentOrder(Task next, Task dependeeTask);

  boolean contains(Task task);

  Task getContainer(Task nestedTask);

  Task[] getDeepNestedTasks(Task container);

  int getDepth(Task task);

  Task[] getNestedTasks(Task container);

  /**
   * @return the next sibling or null if task is the last child of the parent
   * task
   */
  Task getNextSibling(Task task);

  List<Integer> getOutlinePath(Task task);

  /**
   * @return the previous sibling or null if task is the first child of the
   * parent task
   */
  Task getPreviousSibling(Task nestedTask);

  Task getRootTask();

  /**
   * @return the index of the nestedTask with respect of its siblings
   */
  int getTaskIndex(Task nestedTask);

  List<Task> getTasksInDocumentOrder();

  boolean hasNestedTasks(Task container);

  /**
   * Move whatMove to whereMove, added as a child at the end
   */
  void move(Task whatMove, Task whereMove);

  /**
   * Move whatMove to whereMove, added as a child at index
   */
  void move(Task whatMove, Task whereMove, int index);

  void sort(Comparator<Task> comparator);
}
