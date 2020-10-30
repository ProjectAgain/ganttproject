package net.sourceforge.ganttproject.test.task;

import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.model.time.GanttCalendar;

public class TestTaskBounds extends TaskTestCase {
  public void testIssue953() {
    GanttCalendar monday = TestSetupHelper.newMonday();
    GanttCalendar tuesday = TestSetupHelper.newTuesday();
    GanttCalendar wednesday = TestSetupHelper.newWendesday();

    Task supertask = createTask(monday);
    Task childMilestone1 = createTask(monday);
    Task childMilestone2 = createTask(tuesday);
    childMilestone1.setMilestone(true);
    childMilestone2.setMilestone(true);
    childMilestone1.move(supertask);
    childMilestone2.move(supertask);

    getTaskManager().getAlgorithmCollection().getAdjustTaskBoundsAlgorithm().run(supertask);
    assertEquals(monday, supertask.getStart());
    assertEquals(monday, supertask.getDisplayEnd());

    childMilestone1.shift(getTaskManager().createLength("1d"));
    getTaskManager().getAlgorithmCollection().getAdjustTaskBoundsAlgorithm().run(supertask);
    assertEquals(tuesday, supertask.getStart());
    assertEquals(tuesday, supertask.getDisplayEnd());

  }
}
