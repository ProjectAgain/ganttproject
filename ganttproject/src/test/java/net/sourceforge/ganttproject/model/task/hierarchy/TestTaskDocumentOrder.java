package net.sourceforge.ganttproject.model.task.hierarchy;

import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.model.task.TaskTestCase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTaskDocumentOrder extends TaskTestCase {
  @Test
  public void testTasksAreInDocumentOrder() {
    Task task1 = getTaskManager().createTask();
    task1.setName("1");
    Task task2 = getTaskManager().createTask();
    task2.setName("2");
    task1.move(getTaskManager().getRootTask());
    task2.move(task1);

    Task task3 = getTaskManager().createTask();
    task3.setName("3");
    Task task4 = getTaskManager().createTask();
    task4.setName("4");
    task4.move(task3);
    task3.move(getTaskManager().getRootTask());

    Task task5 = getTaskManager().createTask();
    task5.setName("5");
    task5.move(task2);

    List<Task> expectedOrder = Arrays.asList(task1, task2, task5, task3, task4);
    List<Task> actualOrder = Arrays.asList(getTaskManager().getTasks());
    assertEquals(expectedOrder, actualOrder, "Unexpected order of tasks returnedby TaskManager.getTasks()");

    assertEquals(expectedOrder, getTaskManager().getTaskHierarchy().getTasksInDocumentOrder());
  }
}