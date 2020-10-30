package net.sourceforge.ganttproject.model.task.event;

import net.sourceforge.ganttproject.model.task.TaskManager;
import net.sourceforge.ganttproject.model.task.TaskTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTaskHierarchyEvent extends TaskTestCase {
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
  public void testEventIsSentOnCreatingNewTask() {
    TaskManager taskManager = getTaskManager();
    TaskListenerImpl listener = new TaskListenerImpl() {
      @Override
      public void taskAdded(TaskHierarchyEvent e) {
        setHasBeenCalled(true);
      }
    };
    taskManager.addTaskListener(listener);
    taskManager.createTask();
    assertTrue(
      listener.hasBeenCalled(),
      "Event taskAdded() is expected to be sent"
    );
  }
}
