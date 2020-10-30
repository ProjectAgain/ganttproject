package net.sourceforge.ganttproject.model.task;

import net.sourceforge.ganttproject.model.time.CalendarFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author bard
 */
public class TestTaskActivitiesRecalculation extends TaskTestCase {
    @Test
    public void testRecalculateOnChangingDurationByMutator() {
        Task task = getTaskManager().createTask();
        {
            task.setStart(CalendarFactory.createGanttCalendar(2000, 0, 3));
            task.setDuration(getTaskManager().createLength(1));
            List<TaskActivity> activities = task.getActivities();
            assertEquals(1, activities.size(),
                         "Unexpected length of activities"
            );
            assertEquals(CalendarFactory.createGanttCalendar(2000, 0, 4).getTime(),
                         activities.get(0).getEnd(), "Unexpected end of the las activity"
            );
        }
        //
        {
            TaskMutator mutator = task.createMutator();
            mutator.setDuration(getTaskManager().createLength(2));
            List<TaskActivity> activities = task.getActivities();
            assertEquals(1, activities.size(),
                         "Unexpected length of activities"
            );
            assertEquals(CalendarFactory.createGanttCalendar(2000, 0, 5).getTime(),
                         activities.get(0).getEnd(), "Unexpected end of the last activity"
            );
        }
    }

    @Test
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
            assertEquals(1, activities.size(),
                         "Unexpected length of activities"
            );
            assertEquals(CalendarFactory.createGanttCalendar(2000, 0, 7).getTime(),
                         activities.get(0).getEnd(), "Unexpected end of the last activity"
            );
        }
    }
}
