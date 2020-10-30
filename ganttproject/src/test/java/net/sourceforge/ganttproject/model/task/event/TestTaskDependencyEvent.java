/*
 LICENSE:

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 Copyright (C) 2004, GanttProject Development Team
 */
package net.sourceforge.ganttproject.model.task.event;

import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.model.task.TaskManager;
import net.sourceforge.ganttproject.model.task.TaskTestCase;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependency;
import net.sourceforge.ganttproject.model.task.dependency.TaskDependencyException;
import net.sourceforge.ganttproject.model.task.dependency.constraint.FinishStartConstraintImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by IntelliJ IDEA. User: bard
 */
public class TestTaskDependencyEvent extends TaskTestCase {
    private static class TaskListenerImpl extends TaskListenerAdapter {
        private boolean hasBeenCalled;

        protected void setHasBeenCalled(boolean called) {
            hasBeenCalled = called;
        }

        boolean hasBeenCalled() {
            return hasBeenCalled;
        }
    }

    @Test
    public void testDependencyEventIsSentOnDependencyCreation()
        throws TaskDependencyException {
        TaskManager taskManager = getTaskManager();
        TaskListenerImpl listener = new TaskListenerImpl() {
            @Override
            public void dependencyAdded(TaskDependencyEvent e) {
                setHasBeenCalled(true);
            }
        };
        taskManager.addTaskListener(listener);
        Task task1 = taskManager.createTask();
        Task task2 = taskManager.createTask();
        //
        taskManager.getDependencyCollection().createDependency(task2, task1,
                                                               new FinishStartConstraintImpl()
        );
        assertTrue(
            listener.hasBeenCalled(),
            "Listener is expected to be called when dependency is added"
        );
    }

    @Test
    public void testDependencyEventIsSentOnDependencyRemoval()
        throws TaskDependencyException {
        TaskManager taskManager = getTaskManager();
        TaskListenerImpl listener = new TaskListenerImpl() {
            @Override
            public void dependencyRemoved(TaskDependencyEvent e) {
                setHasBeenCalled(true);
            }
        };
        taskManager.addTaskListener(listener);
        Task task1 = taskManager.createTask();
        Task task2 = taskManager.createTask();
        //
        TaskDependency dep = taskManager
            .getDependencyCollection()
            .createDependency(task2, task1, new FinishStartConstraintImpl());
        dep.delete();
        assertTrue(
            listener.hasBeenCalled(),
            "Listener is expected to be called when dependency is deleted"
        );
    }
}
