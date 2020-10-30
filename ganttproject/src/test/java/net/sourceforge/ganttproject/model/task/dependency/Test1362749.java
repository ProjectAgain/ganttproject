package net.sourceforge.ganttproject.model.task.dependency;

import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.model.task.TaskMutator;
import net.sourceforge.ganttproject.model.task.TaskTestCase;
import net.sourceforge.ganttproject.model.task.dependency.constraint.FinishFinishConstraintImpl;
import net.sourceforge.ganttproject.model.task.dependency.constraint.FinishStartConstraintImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Test1362749 extends TaskTestCase {
  @Test
  public void testBugReport1362749() throws Exception {
    Task t1 = getTaskManager().createTask();
    Task t2 = getTaskManager().createTask();
    Task t3 = getTaskManager().createTask();
    Task t4 = getTaskManager().createTask();
    getTaskManager().getDependencyCollection().createDependency(t4, t3, new FinishStartConstraintImpl());
    getTaskManager().getDependencyCollection().createDependency(t1, t3, new FinishFinishConstraintImpl());
    getTaskManager().getDependencyCollection().createDependency(t2, t4, new FinishFinishConstraintImpl());
    TaskMutator mutator = t3.createMutator();
    mutator.setDuration(getTaskManager().createLength(3));
    mutator.commit();
    getTaskManager().getAlgorithmCollection().getRecalculateTaskScheduleAlgorithm().run(t3);

    assertEquals(t3.getEnd(), t4.getStart(), "Task=" + t4 + " is expected to start when task=" + t3 + " finishes");
    assertEquals(t1.getEnd(), t3.getEnd(), "Task=" + t1 + " is expected to finish together with task=" + t3);
    assertEquals(t2.getEnd(), t4.getEnd(), "Task=" + t2 + " is expected to finish together with task=" + t4);
    assertTrue(
      t1.getStart().compareTo(t2.getStart()) < 0,
      "It is expected that start date of task=" + t1 + " is less than start date of task=" + t2
    );
  }
}
