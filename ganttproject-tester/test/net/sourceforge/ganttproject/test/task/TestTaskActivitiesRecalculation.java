package net.sourceforge.ganttproject.test.task;

import net.sourceforge.ganttproject.model.task.Task;
import net.sourceforge.ganttproject.model.task.TaskActivity;
import net.sourceforge.ganttproject.model.task.TaskMutator;
import net.sourceforge.ganttproject.model.time.CalendarFactory;

import java.util.List;

/**
 * @author bard
 */
public class TestTaskActivitiesRecalculation extends TaskTestCase {
    public void testRecalculateOnChangingDurationByMutator() {
        Task task = getTaskManager().createTask();
        {
            task.setStart(CalendarFactory.createGanttCalendar(2000, 0, 3));
            task.setDuration(getTaskManager().createLength(1));
            List<TaskActivity> activities = task.getActivities();
            assertEquals("Unexpected length of activities", 1,
                    activities.size());
            assertEquals("Unexpected end of the las activity",
                    CalendarFactory.createGanttCalendar(2000, 0, 4).getTime(), activities.get(0).getEnd());
        }
        //
        {
            TaskMutator mutator = task.createMutator();
            mutator.setDuration(getTaskManager().createLength(2));
            List<TaskActivity> activities = task.getActivities();
            assertEquals("Unexpected length of activities", 1,
                    activities.size());
            assertEquals("Unexpected end of the last activity",
                    CalendarFactory.createGanttCalendar(2000, 0, 5).getTime(), activities.get(0).getEnd());
        }
    }

    public void testRecalculateOnChangingStartByFixingDurationMutator() {
        Task task = getTaskManager().createTask();
        {
            task.setStart(CalendarFactory.createGanttCalendar(2000, 0, 3));
            task.setDuration(getTaskManager().createLength(3));
        }
        {
            TaskMutator mutator = task.createMutatorFixingDuration();
            mutator.setStart(CalendarFactory.createGanttCalendar(2000, 0, 4));
            mutator.commit();
            List<TaskActivity> activities = task.getActivities();
            assertEquals("Unexpected length of activities", 1,
                    activities.size());
            assertEquals("Unexpected end of the last activity",
                    CalendarFactory.createGanttCalendar(2000, 0, 7).getTime(), activities.get(0).getEnd());
        }
    }
}
